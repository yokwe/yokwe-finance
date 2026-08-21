package yokwe.finance.report.stock.us;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.rakuten.StorageRakuten;
import yokwe.finance.data.stock.us.StorageStockUS;
import yokwe.finance.report.stats.StockStats;
import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;
import yokwe.util.Makefile;
import yokwe.util.MarketHoliday;
import yokwe.util.StringUtil;
import yokwe.util.libreoffice.LibreOffice;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;
import yokwe.util.update.UpdateBase;

public class UpdateReport extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static Makefile MAKEFILE = Makefile.builder().
			input(
					StorageStockUS.StockInfoUS,
					StorageStockUS.StockPriceOHLCV,
					StorageStockUS.StockDiv,
					StorageRakuten.TradingStockUSRakuten
				).
			output(StorageReportStockUS.ReportODS).
			build();

	public static void main(String[] args) {
		callUpdate();
	}

	@Override
	public void update() {
		var list = getReportList();
		// save ods
		generateReport(list);
		// save csv
		{
			var file = StorageReportStockUS.ReportCSV.getFile();
			logger.info("save  {}  {}", list.size(), file.getPath());
			CSVUtil.write(ReportForm.class).file(file, list);
		}
		// copy files
		{
			var oldFile = StorageReportStockUS.ReportODS.getFile();
			var newFile = StorageReportStockUS.ReportODS.getFile(LocalDateTime.now());
			logger.info("copy {} to {}", oldFile, newFile);
			FileUtil.copy(oldFile, newFile);
		}
	}
	private List<ReportForm> getReportList() {
		var dateStop  = MarketHoliday.JP.getLastTradingDate();
		logger.info("dateStop  {}", dateStop);

		var rakutenSet = StorageRakuten.TradingStockUSRakuten.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
		var stockStatsMap = StorageStockUS.StockStatsUS.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));

		var list = new ArrayList<ReportForm>();
		{
			for(var stockInfo: StorageStockUS.StockInfoUS.getList()) {
				var stockCode = stockInfo.stockCode;
				var priceList = StorageStockUS.StockPriceOHLCV.getList(stockCode);
				var divList   = StorageStockUS.StockDiv.getList(stockCode);

				if (priceList.size() < 10) {
					logger.info("too small  {}  {}  {}", priceList.size(), stockCode, stockInfo.name);
					continue;
				}

				var stockStatsUS = stockStatsMap.get(stockCode);
				if (stockStatsUS == null) {
					logger.info("no stockStats {}  {}", stockCode, stockInfo.name);
					continue;
				}

				ReportForm report = new ReportForm();
				report.stockCode = stockCode;
				report.type      = stockInfo.type.simpleType.toString();
				report.sector    = stockInfo.sector;
				report.industry  = stockInfo.industry;

				report.divc     = -1;
				// set sector and industry

				report.name      = stockInfo.name;

				{
					StockStats stockStats = StockStats.getInstance(stockCode, dateStop, priceList, divList);

					report.price     = stockStats.price;
					report.pricec    = priceList.size();
					report.last      = stockStats.last;

					report.rorNoReinvested = stockStats.rorNoReinvested;

					report.sd        = stockStats.sd;
					report.hv        = stockStats.hv;
					report.rsi       = stockStats.rsi14;
//					report.rsi14     = stockStats.rsi14;
//					report.rsi7      = stockStats.rsi7;

					report.min       = stockStats.min;
					report.max       = stockStats.max;
					report.minY3     = stockStats.minY3;
					report.maxY3     = stockStats.maxY3;

//					if (stats.divc == -1) {
//						stats.divc          = stockStats.divc;
//					}
					report.divc          = stockStats.divc;
					report.lastDiv       = stockStats.lastDiv;
					report.forwardYield  = stockStats.forwardYield;
					report.annualDiv     = stockStats.annualDiv;
					report.trailingYield = stockStats.trailingYield;

					// stockStatsUS
					report.rorPrice      = stockStatsUS.fiftyTwoWeek.doubleValue();
					report.divc          = stockStatsUS.divInt;
					report.trailingYield = stockStatsUS.divYield.doubleValue();

//					stats.vol       = (double)stockStats.vol / stockInfo.issued.doubleValue();
//					stats.vol5      = (double)stockStats.vol5 / stockInfo.issued.doubleValue();
//					stats.vol21     = (double)stockStats.vol21 / stockInfo.issued.doubleValue();
					report.vol       = (long)(stockStats.vol   * report.price);
					report.vol5      = (long)(stockStats.vol5  * report.price);
					report.vol21     = (long)(stockStats.vol21 * report.price);
				}

				// FXIME
				report.nisa    = "";
				report.rakuten = rakutenSet.contains(stockCode) ? "1" : "";
				report.nikko   = "";

				list.add(report);
			}
		}

		return list;
	}
	private void generateReport(List<ReportForm> reportList) {
		String urlReport = StringUtil.toURLString(StorageReportStockUS.ReportODS.getFile());
		logger.info("urlReport {}", urlReport);
		logger.info("docLoad   {}", URL_TEMPLATE);
		try {
			// start LibreOffice process
			LibreOffice.initialize();

			SpreadSheet docLoad = new SpreadSheet(URL_TEMPLATE, true);
			SpreadSheet docSave = new SpreadSheet();

			String sheetName = Sheet.getSheetName(ReportForm.class);
			logger.info("sheet     {}", sheetName);
			docSave.importSheet(docLoad, sheetName, docSave.getSheetCount());
			Sheet.fillSheet(docSave, reportList);

			// remove first sheet
			docSave.removeSheet(docSave.getSheetName(0));

			docSave.store(urlReport);
			logger.info("output    {}", urlReport);

			docLoad.close();
			logger.info("close     docLoad");
			docSave.close();
			logger.info("close     docSave");
		} finally {
			// stop LibreOffice process
			LibreOffice.terminate();
		}
	}
	private static final String URL_TEMPLATE  = StringUtil.toURLString(new File("data/form/STOCK_STATS_US.ods"));
}
