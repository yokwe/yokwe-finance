package yokwe.finance.report.stock.jp;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.report.stats.StockStats;
import yokwe.finance.tool.Makefile;
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
					yokwe.finance.data.fund.jp.StorageJP.NISAInfo,
					yokwe.finance.data.stock.jp.StorageJP.StockValue,
					yokwe.finance.data.stock.jp.StorageJP.StockInfo,
					yokwe.finance.data.stock.jp.StorageJP.StockPriceOHLCV,
					yokwe.finance.data.stock.jp.StorageJP.StockDiv
				).
			output(StorageJP.ReportCSV).
			build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		var list = getReportList();		
		generateReport(list);
		// save
		save(list, StorageJP.ReportCSV);
	}
	private List<ReportForm> getReportList() {
		var dateStop  = MarketHoliday.JP.getLastTradingDate();
		logger.info("dateStop  {}", dateStop);
		
		var list = new ArrayList<ReportForm>();
		{
			var nisaMap        = yokwe.finance.data.fund.jp.StorageJP.NISAInfo.getList().stream().collect(Collectors.toMap(o -> o.isinCode, Function.identity()));
			var stockValueMap  = yokwe.finance.data.stock.jp.StorageJP.StockValue.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));

			for(var stockInfo: yokwe.finance.data.stock.jp.StorageJP.StockInfo.getList()) {
				var stockCode = stockInfo.stockCode;
				var priceList = yokwe.finance.data.stock.jp.StorageJP.StockPriceOHLCV.getList(stockCode);
				var divList   = yokwe.finance.data.stock.jp.StorageJP.StockDiv.getList(stockCode);
				var stockValue = stockValueMap.get(stockCode);
				
				if (priceList.size() < 10) {
					logger.info("skip  {}  {}  {}", priceList.size(), stockCode, stockInfo.name);
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
				report.marketCap = BigDecimal.valueOf(stockValue.issued).multiply(priceList.get(priceList.size() - 1).close).longValue();
				
				{
					StockStats stockStats = StockStats.getInstance(stockCode, dateStop, priceList, divList);
					
					report.price     = stockStats.price;
					report.pricec    = priceList.size();
					report.invest    = (int)(stockStats.price * stockInfo.tradeUnit);
					report.last      = stockStats.last;
					
					report.rorNoReinvested = stockStats.rorNoReinvested;
					
					report.sd        = stockStats.sd;
					report.hv        = stockStats.hv;
					report.rsi14     = stockStats.rsi14;
					report.rsi7      = stockStats.rsi7;
					
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

//					stats.vol       = (double)stockStats.vol / stockInfo.issued.doubleValue();
//					stats.vol5      = (double)stockStats.vol5 / stockInfo.issued.doubleValue();
//					stats.vol21     = (double)stockStats.vol21 / stockInfo.issued.doubleValue();
					report.vol       = (double)stockStats.vol   * report.price;
					report.vol5      = (double)stockStats.vol5  * report.price;
					report.vol21     = (double)stockStats.vol21 * report.price;
				}
				
				if (stockInfo.type.isETF()) {
					if (nisaMap.containsKey(stockInfo.isinCode)) {
						var nisaInfo = nisaMap.get(stockInfo.isinCode);
						report.nisa = nisaInfo.tsumitate ? "1" : "0";
					} else {
						report.nisa = "";
					}
				} else {
					report.nisa = "0";
				}
				
				list.add(report);
			}
		}

		return list;
	}
	private void generateReport(List<ReportForm> reportList) {
		String urlReport;
		{
			var timestamp  = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
			var file       = StorageJP.Report.getFile(timestamp);
			
			urlReport  = StringUtil.toURLString(file);
		}

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
	private static final String URL_TEMPLATE  = StringUtil.toURLString(new File("data/form/STOCK_STATS_JP.ods"));
}
