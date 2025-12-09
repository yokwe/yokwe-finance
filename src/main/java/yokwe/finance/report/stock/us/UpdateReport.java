package yokwe.finance.report.stock.us;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import yokwe.finance.report.stats.StockStats;
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
					yokwe.finance.data.stock.us.StorageUS.StockInfo,
					yokwe.finance.data.stock.us.StorageUS.StockPriceOHLCV,
					yokwe.finance.data.stock.us.StorageUS.StockDiv
				).
			output(StorageUS.Report).
			build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		var list = getReportList();		
		generateReport(list);
		// save
		// generateReport saves file
	}
	private List<ReportForm> getReportList() {
		var dateStop  = MarketHoliday.JP.getLastTradingDate();
		logger.info("dateStop  {}", dateStop);
		
		var list = new ArrayList<ReportForm>();
		{
			for(var stockInfo: yokwe.finance.data.stock.us.StorageUS.StockInfo.getList()) {
				var stockCode = stockInfo.stockCode;
				var priceList = yokwe.finance.data.stock.us.StorageUS.StockPriceOHLCV.getList(stockCode);
				var divList   = yokwe.finance.data.stock.us.StorageUS.StockDiv.getList(stockCode);
				
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

//					stats.vol       = (double)stockStats.vol / stockInfo.issued.doubleValue();
//					stats.vol5      = (double)stockStats.vol5 / stockInfo.issued.doubleValue();
//					stats.vol21     = (double)stockStats.vol21 / stockInfo.issued.doubleValue();
					report.vol       = (long)(stockStats.vol   * report.price);
					report.vol5      = (long)(stockStats.vol5  * report.price);
					report.vol21     = (long)(stockStats.vol21 * report.price);
				}
				
				report.nisa    = "";
				report.rakuten = "";
				report.nikko   = "";
								
				list.add(report);
			}
		}

		return list;
	}
	private void generateReport(List<ReportForm> reportList) {
		String urlReport = StringUtil.toURLString(StorageUS.Report.getFile());
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
		{
			var timestamp  = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
			var newName = "report-" + timestamp + ".ods";
			var destFile = StorageUS.Report.getFile(newName);
			
			logger.info("copy {} to {}", StorageUS.Report.getFile(), destFile);
			
			FileUtil.copy(StorageUS.Report.getFile(), destFile);
		}
	}
	private static final String URL_TEMPLATE  = StringUtil.toURLString(new File("data/form/STOCK_STATS_US.ods"));
}
