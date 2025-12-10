package yokwe.finance.report.fund.jp;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.data.type.DailyValue;
import yokwe.finance.data.type.FundInfoJP;
import yokwe.finance.report.stats.MonthlyStats;
import yokwe.util.DoubleUtil;
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
					yokwe.finance.data.fund.jp.StorageJP.FundInfo,
					yokwe.finance.data.fund.jp.StorageJP.FundDiv,
					yokwe.finance.data.fund.jp.StorageJP.FundPrice,
					yokwe.finance.data.fund.jp.StorageJP.NISAInfo,
					yokwe.finance.data.provider.sony.StorageSony.TradingFundJP,
					yokwe.finance.data.provider.smtb.StorageSMTB.TradingFundJP,
					yokwe.finance.data.provider.rakuten.StorageRakuten.TradingFundJP
				).
			output(StorageJP.Report).
			build();

	public static void main(String[] args) {
		callUpdate();
	}
	
	private static final String     URL_TEMPLATE  = StringUtil.toURLString(new File("data/form/FUND_STATS.ods"));
	private static final LocalDate  LAST_DATE_OF_LAST_MONTH = LocalDate.now().withDayOfMonth(1).minusDays(1);
	private static final LocalDate  NO_DATE = LocalDate.of(2099, 12, 31);
	private static final BigDecimal CONSUMPTION_TAX_RATE    = new BigDecimal("1.1"); // 10 percent

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
		var nisaInfoMap  = yokwe.finance.data.fund.jp.StorageJP.NISAInfo.getList().stream().collect(Collectors.toMap(o -> o.isinCode, Function.identity()));
		var fundInfoList = yokwe.finance.data.fund.jp.StorageJP.FundInfo.getList();
		var sonyMap      = yokwe.finance.data.provider.sony.StorageSony.TradingFundJP.getList().stream().collect(Collectors.toMap(o -> o.isinCode, Function.identity()));
		var smtbMap      = yokwe.finance.data.provider.smtb.StorageSMTB.TradingFundJP.getList().stream().collect(Collectors.toMap(o -> o.isinCode, Function.identity()));
		var rakutenMap   = yokwe.finance.data.provider.rakuten.StorageRakuten.TradingFundJP.getList().stream().collect(Collectors.toMap(o -> o.isinCode, Function.identity()));
		
		int countNoPrice = 0;
		int count        = 0;
		
		for(var fundInfo: fundInfoList) {
			var isinCode  = fundInfo.isinCode;
			
			if ((++count % 500) == 1) logger.info("{} / {}  {}", count, fundInfoList.size(), isinCode);
			
			MonthlyStats  monthlyStats;
			BigDecimal    nav;
			{
				var fundPriceList = yokwe.finance.data.fund.jp.StorageJP.FundPrice.getList(isinCode);
				if (fundPriceList.isEmpty()) {
					countNoPrice++;
					continue;
				}

				var priceList = fundPriceList.stream().map(o -> new DailyValue(o.date, o.price)).collect(Collectors.toList());
				var divList   = MonthlyStats.getDivList(priceList, yokwe.finance.data.fund.jp.StorageJP.FundDiv.getList(isinCode));
				
				// use last element for nav
				nav = fundPriceList.get(fundPriceList.size() - 1).nav;
				
				monthlyStats = MonthlyStats.getInstance(isinCode, priceList, divList);
			}

			ReportForm report = new ReportForm();
			report.isinCode  = fundInfo.isinCode;
			report.fundCode  = fundInfo.fundCode;
			report.stockCode = fundInfo.stockCode;
			
			report.inception  = fundInfo.inceptionDate;
			report.redemption = fundInfo.redemptionDate;
			report.age        = durationInYearMonth(fundInfo.inceptionDate, LAST_DATE_OF_LAST_MONTH);
			
			// Use toushin category
			report.investingAsset = fundInfo.investingAsset;
			report.investingArea  = fundInfo.investingArea;
			report.indexFundType  = fundInfo.indexFundType.replace("該当なし", "アクティブ型").replace("型", "");
			
			report.expenseRatio = fundInfo.expenseRatio.multiply(CONSUMPTION_TAX_RATE);
			report.buyFeeMax    = fundInfo.buyFeeMax.multiply(CONSUMPTION_TAX_RATE);
			report.nav          = nav;
			report.divc         = fundInfo.divFreq;

			{
				int nMonth  = 1;
				int nOffset = 0;
				
				report.rsi14   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.rsi(nMonth, nOffset, 14));
				report.rsi7    = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.rsi(nMonth, nOffset, 7));
			}
			
			// 1 year
			{
				int nMonth  = 12;
				int nOffset = 0;
				
				report.sd1Y    = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.risk(nMonth, nOffset));
				report.div1Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.dividend(nMonth, nOffset));
				report.yield1Y = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.yield(nMonth, nOffset));
				report.ror1Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.rateOfReturn(nMonth, nOffset));
			}
			// 3 year
			{
				int nMonth = 36;
				int nOffset = 0;
				
				report.sd3Y    = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.risk(nMonth, nOffset));
				report.div3Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.dividend(nMonth, nOffset));
				report.yield3Y = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.yield(nMonth, nOffset));
				report.ror3Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.rateOfReturn(nMonth, nOffset));
			}
			// 5 year
			{
				int nMonth = 60;
				int nOffset = 0;
				
				report.sd5Y    = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.risk(nMonth, nOffset));
				report.div5Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.dividend(nMonth, nOffset));
				report.yield5Y = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.yield(nMonth, nOffset));
				report.ror5Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.rateOfReturn(nMonth, nOffset));
			}
			// 10 year
			{
				int nMonth = 120;
				int nOffset = 0;
				
				report.sd10Y    = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.risk(nMonth, nOffset));
				report.div10Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.dividend(nMonth, nOffset));
				report.yield10Y = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.yield(nMonth, nOffset));
				report.ror10Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.rateOfReturn(nMonth, nOffset));
			}
			
//			report.divScore1Y  = (nikkei == null || !DivScoreType.hasValue(nikkei.score1Y))  ? null : nikkei.score1Y;
//			report.divScore3Y  = (nikkei == null || !DivScoreType.hasValue(nikkei.score3Y))  ? null : nikkei.score3Y;
//			report.divScore5Y  = (nikkei == null || !DivScoreType.hasValue(nikkei.score5Y))  ? null : nikkei.score5Y;
//			report.divScore10Y = (nikkei == null || !DivScoreType.hasValue(nikkei.score10Y)) ? null : nikkei.score10Y;
			report.divScore1Y  = null;
			report.divScore3Y  = null;
			report.divScore5Y  = null;
			report.divScore10Y = null;
			
			report.name     = fundInfo.name;
			
			if (nisaInfoMap.containsKey(isinCode)) {
				var nisaInfo = nisaInfoMap.get(isinCode);
				report.nisa = nisaInfo.tsumitate ? BigDecimal.ONE : BigDecimal.ZERO;
			} else {
				report.nisa = null;
			}

			if (report.stockCode.isEmpty()) {
				// FUND
				report.nikko   = null;
				report.prestia = null;
				//
//				report.nikko   = !nikkoMap.containsKey(fundInfo.isinCode)   ? null: nikkoMap.get(fundInfo.isinCode).salesFee;
				report.rakuten = !rakutenMap.containsKey(fundInfo.isinCode) ? null: rakutenMap.get(fundInfo.isinCode).salesFee;
				report.sony    = !sonyMap.containsKey(fundInfo.isinCode)    ? null: sonyMap.get(fundInfo.isinCode).salesFee;
//				report.prestia = !prestiaMap.containsKey(fundInfo.isinCode) ? null: prestiaMap.get(fundInfo.isinCode).salesFee;
				report.smtb    = !smtbMap.containsKey(fundInfo.isinCode)    ? null: smtbMap.get(fundInfo.isinCode).salesFee;
			} else {
				// ETF
				report.nikko   = BigDecimal.ZERO;
				report.rakuten = BigDecimal.ZERO;
				report.sony    = null;
				report.prestia = null;
				report.smtb    = null;
			}
			
			// special case
			if (fundInfo.redemptionDate.toString().compareTo(FundInfoJP.NO_REDEMPTION_DATE_STRING) == 0) report.redemption = NO_DATE;

			if (report.div1Y  != null && report.div1Y.compareTo(BigDecimal.ZERO) == 0)  report.yield1Y  = report.divScore1Y  = null;
			if (report.div3Y  != null && report.div3Y.compareTo(BigDecimal.ZERO) == 0)  report.yield3Y  = report.divScore3Y  = null;
			if (report.div5Y  != null && report.div5Y.compareTo(BigDecimal.ZERO) == 0)  report.yield5Y  = report.divScore5Y  = null;
			if (report.div10Y != null && report.div10Y.compareTo(BigDecimal.ZERO) == 0) report.yield10Y = report.divScore10Y = null;
			
			list.add(report);
		}
		
		logger.info("fundList       {}", fundInfoList.size());
//		logger.info("nikkeiMap      {}", nikkeiMap.size());
		logger.info("countNoPrice   {}", countNoPrice);
//		logger.info("countNoNikkei  {}", countNoNikkei);

		return list;
	}
	private void generateReport(List<ReportForm> reportList) {
		var urlReport = StringUtil.toURLString(StorageJP.Report.getFile());
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
			var destFile = StorageJP.Report.getFile(newName);
			
			logger.info("copy {} to {}", StorageJP.Report.getFile(), destFile);
			
			FileUtil.copy(StorageJP.Report.getFile(), destFile);
		}
	}

	private static BigDecimal durationInYearMonth(LocalDate startDate, LocalDate endDate) {
		// startDate and endDate is inclusive
		if (startDate.isAfter(endDate)) {
			return new BigDecimal("0.00");
		} else {
			LocalDate endDatePlusOne = endDate.plusDays(1);		
			Period    period         = startDate.until(endDatePlusOne);
			return new BigDecimal(String.format("%d.%02d", period.getYears(), period.getMonths()));
		}
	}
}
