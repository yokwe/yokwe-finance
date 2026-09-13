package yokwe.finance.report.stock.us;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

				report.name      = stockInfo.name;

				{
					StockStats stockStats = StockStats.getInstance(stockCode, dateStop, priceList, divList);

					report.price     = BigDecimal.valueOf(stockStats.price);
					report.pricec    = BigDecimal.valueOf(priceList.size());
					report.last      = BigDecimal.valueOf(stockStats.last);

					report.rorReinvested   = BigDecimal.valueOf(stockStats.rorReinvested).setScale(5, RoundingMode.HALF_EVEN);
					report.rorNoReinvested = BigDecimal.valueOf(stockStats.rorNoReinvested).setScale(5, RoundingMode.HALF_EVEN);

					report.sd        = BigDecimal.valueOf(stockStats.sd).setScale(5, RoundingMode.HALF_EVEN);
					report.hv        = BigDecimal.valueOf(stockStats.hv).setScale(5, RoundingMode.HALF_EVEN);
					report.rsi       = BigDecimal.valueOf(stockStats.rsi14).setScale(1, RoundingMode.HALF_EVEN);

					report.min       = BigDecimal.valueOf(stockStats.min).setScale(5, RoundingMode.HALF_EVEN);
					report.max       = BigDecimal.valueOf(stockStats.max).setScale(5, RoundingMode.HALF_EVEN);
					report.minY3     = BigDecimal.valueOf(stockStats.minY3).setScale(5, RoundingMode.HALF_EVEN);
					report.maxY3     = BigDecimal.valueOf(stockStats.maxY3).setScale(5, RoundingMode.HALF_EVEN);

//					if (stats.divc == -1) {
//						stats.divc          = stockStats.divc;
//					}
					report.divc          = BigDecimal.valueOf(stockStats.divc);
					report.lastDiv       = BigDecimal.valueOf(stockStats.lastDiv);
					report.forwardYield  = BigDecimal.valueOf(stockStats.forwardYield).setScale(5, RoundingMode.HALF_EVEN);
					report.annualDiv     = BigDecimal.valueOf(stockStats.annualDiv);
					report.trailingYield = BigDecimal.valueOf(stockStats.trailingYield).setScale(5, RoundingMode.HALF_EVEN);

					// stockStatsUS
					report.rorPrice      = stockStatsUS.fiftyTwoWeek;
					report.divc          = BigDecimal.valueOf(stockStatsUS.divInt);
					report.trailingYield = stockStatsUS.divYield;

//					stats.vol       = (double)stockStats.vol / stockInfo.issued.doubleValue();
//					stats.vol5      = (double)stockStats.vol5 / stockInfo.issued.doubleValue();
//					stats.vol21     = (double)stockStats.vol21 / stockInfo.issued.doubleValue();
					report.vol       = BigDecimal.valueOf(stockStats.vol).setScale(0, RoundingMode.HALF_EVEN);
					report.vol5      = BigDecimal.valueOf(stockStats.vol5).setScale(0, RoundingMode.HALF_EVEN);
					report.vol21     = BigDecimal.valueOf(stockStats.vol21).setScale(0, RoundingMode.HALF_EVEN);
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
