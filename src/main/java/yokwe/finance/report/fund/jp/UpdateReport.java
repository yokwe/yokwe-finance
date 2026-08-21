package yokwe.finance.report.fund.jp;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.data.analysis.StorageAnalysis;
import yokwe.finance.data.fund.jp.StorageFundJP;
import yokwe.finance.data.provider.click.StorageClick;
import yokwe.finance.data.provider.nikkei.StorageNikkei;
import yokwe.finance.data.provider.nikko.StorageNikko;
import yokwe.finance.data.provider.rakuten.StorageRakuten;
import yokwe.finance.data.provider.smtb.StorageSMTB;
import yokwe.finance.data.provider.sony.StorageSony;
import yokwe.finance.data.type.DailyValue;
import yokwe.finance.data.type.FundDivScore;
import yokwe.finance.data.type.FundInfoJP;
import yokwe.finance.report.stats.MonthlyStats;
import yokwe.finance.report.stats.online.BigDecimalSMA;
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
					StorageAnalysis.TaxAdjustment,
					StorageFundJP.FundInfo,
					StorageFundJP.FundDiv,
					StorageFundJP.FundPrice,
					StorageFundJP.NISAInfo,
					StorageNikko.TradingFundJPNikko,
					StorageRakuten.TradingFundJPRakuten,
					StorageSMTB.TradingFundJPSMTB,
					StorageSony.TradingFundJPSony,
					StorageClick.TradingFundJPClick,
					StorageNikkei.FundDivScore
				).
			output(StorageReportFundJP.ReportODS).
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
		// save ods
		generateReport(list);
		// save csv
		{
			var file = StorageReportFundJP.ReportCSV.getFile();
			logger.info("save  {}  {}", list.size(), file.getPath());
			CSVUtil.write(ReportForm.class).file(file, list);
		}
		// copy files
		{
			var oldFile = StorageReportFundJP.ReportODS.getFile();
			var newFile = StorageReportFundJP.ReportODS.getFile(LocalDateTime.now());
			logger.info("copy {} to {}", oldFile, newFile);
			FileUtil.copy(oldFile, newFile);
		}
	}
	private List<ReportForm> getReportList() {
		var dateStop  = MarketHoliday.JP.getLastTradingDate();
		logger.info("dateStop  {}", dateStop);

		var list = new ArrayList<ReportForm>();
		var nisaInfoMap  = StorageFundJP.NISAInfo.getList().stream().collect(Collectors.toMap(o -> o.isinCode, Function.identity()));
		var fundInfoList = StorageFundJP.FundInfo.getList();
		var nikkoMap     = StorageNikko.TradingFundJPNikko.getList().stream().collect(Collectors.toMap(o -> o.isinCode, Function.identity()));
		var rakutenMap   = StorageRakuten.TradingFundJPRakuten.getList().stream().collect(Collectors.toMap(o -> o.isinCode, Function.identity()));
		var smtbMap      = StorageSMTB.TradingFundJPSMTB.getList().stream().collect(Collectors.toMap(o -> o.isinCode, Function.identity()));
		var sonyMap      = StorageSony.TradingFundJPSony.getList().stream().collect(Collectors.toMap(o -> o.isinCode, Function.identity()));
		var clickMap     = StorageClick.TradingFundJPClick.getList().stream().collect(Collectors.toMap(o -> o.isinCode, Function.identity()));
		var divScoreMap  = StorageNikkei.FundDivScore.getList().stream().collect(Collectors.toMap(o -> o.isinCode, Function.identity()));
		var taxMap       = StorageAnalysis.TaxAdjustment.getList().stream().filter(o -> o.hasValue()).collect(Collectors.toMap(o -> o.isinCode, Function.identity()));

		int countNoPrice    = 0;
		int countNoDivScore = 0;
		int count           = 0;

		var smallDecimal = new BigDecimal("0.00000001");

		for(var fundInfo: fundInfoList) {
			var isinCode  = fundInfo.isinCode;

			if ((++count % 500) == 1) {
				logger.info("{} / {}  {}", count, fundInfoList.size(), isinCode);
			}

			MonthlyStats  monthlyStats;
			BigDecimal    nav;
			{
				var fundPriceList = StorageFundJP.FundPrice.getList(isinCode);
				if (fundPriceList.isEmpty()) {
					countNoPrice++;
					continue;
				}

				var priceList = fundPriceList.stream().map(o -> new DailyValue(o.date, o.price)).collect(Collectors.toList());
				var divList   = MonthlyStats.getDivList(priceList, StorageFundJP.FundDiv.getList(isinCode));

				// FIXME create moving average of priceList
				{
					var size = 7;
					var sma = new BigDecimalSMA(size);

					for(int index = 0; index < priceList.size(); index++) {
						var e = priceList.get(index);
						e.value = sma.apply(e.value);
					}
				}

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
			report.taxAdjuettment = taxMap.containsKey(fundInfo.isinCode) ? "1" : "0";
			report.indexFundType  = fundInfo.indexFundType.replace("該当なし", "アクティブ型").replace("型", "");

			report.expenseRatio = fundInfo.expenseRatio.multiply(CONSUMPTION_TAX_RATE);
			report.buyFeeMax    = fundInfo.buyFeeMax.multiply(CONSUMPTION_TAX_RATE);
			report.nav          = nav;
			report.divc         = fundInfo.divFreq;


			if (monthlyStats != null) {
				{
					int nMonth  = 1;
					int nOffset = 0;

					if (monthlyStats.contains(nMonth, nOffset)) {
						report.rsi14 = BigDecimal.valueOf(monthlyStats.rsi(nMonth, nOffset, 14));
						report.rsi7  = BigDecimal.valueOf(monthlyStats.rsi(nMonth, nOffset,  7));
					}
				}

				// 1 year
				{
					int nMonth  = 12;
					int nOffset = 0;

					if (monthlyStats.contains(nMonth, nOffset)) {
						report.sd1Y    = BigDecimal.valueOf(monthlyStats.risk(nMonth, nOffset));
						report.div1Y   = BigDecimal.valueOf(monthlyStats.dividend(nMonth, nOffset));
						report.yield1Y = BigDecimal.valueOf(monthlyStats.yield(nMonth, nOffset));
						report.ror1Y   = BigDecimal.valueOf(monthlyStats.rateOfReturn(nMonth, nOffset));
					}
				}
				// 3 year
				{
					int nMonth = 36;
					int nOffset = 0;

					if (monthlyStats.contains(nMonth, nOffset)) {
						report.sd3Y    = BigDecimal.valueOf(monthlyStats.risk(nMonth, nOffset));
						report.div3Y   = BigDecimal.valueOf(monthlyStats.dividend(nMonth, nOffset));
						report.yield3Y = BigDecimal.valueOf(monthlyStats.yield(nMonth, nOffset));
						report.ror3Y   = BigDecimal.valueOf(monthlyStats.rateOfReturn(nMonth, nOffset));
					}
				}
				// 5 year
				{
					int nMonth = 60;
					int nOffset = 0;

					if (monthlyStats.contains(nMonth, nOffset)) {
						report.sd5Y    = BigDecimal.valueOf(monthlyStats.risk(nMonth, nOffset));
						report.div5Y   = BigDecimal.valueOf(monthlyStats.dividend(nMonth, nOffset));
						report.yield5Y = BigDecimal.valueOf(monthlyStats.yield(nMonth, nOffset));
						report.ror5Y   = BigDecimal.valueOf(monthlyStats.rateOfReturn(nMonth, nOffset));
					}
				}
				// 10 year
				{
					int nMonth = 120;
					int nOffset = 0;

					if (monthlyStats.contains(nMonth, nOffset)) {
						report.sd10Y    = BigDecimal.valueOf(monthlyStats.risk(nMonth, nOffset));
						report.div10Y   = BigDecimal.valueOf(monthlyStats.dividend(nMonth, nOffset));
						report.yield10Y = BigDecimal.valueOf(monthlyStats.yield(nMonth, nOffset));
						report.ror10Y   = BigDecimal.valueOf(monthlyStats.rateOfReturn(nMonth, nOffset));
					}
				}

			}

			{
				var divScore = divScoreMap.getOrDefault(isinCode, null);
				if (divScore == null) {
					countNoDivScore++;
				} else {
					if (FundDivScore.isValid(divScore.score1Y)) {
						report.divScore1Y = divScore.score1Y;
					}
					if (FundDivScore.isValid(divScore.score3Y)) {
						report.divScore3Y = divScore.score3Y;
					}
					if (FundDivScore.isValid(divScore.score5Y)) {
						report.divScore5Y = divScore.score5Y;
					}
					if (FundDivScore.isValid(divScore.score10Y)) {
						report.divScore10Y = divScore.score10Y;
					}
				}
			}

			report.name     = fundInfo.name;

			if (nisaInfoMap.containsKey(isinCode)) {
				var nisaInfo = nisaInfoMap.get(isinCode);
				report.nisa = nisaInfo.tsumitate ? "1" : "0";
			} else {
				report.nisa = "";
			}

			if (report.stockCode.isEmpty()) {
				// FUND
				report.prestia = BigDecimal.ZERO;
				//
				{
					var tradingFund = nikkoMap.getOrDefault(isinCode, null);
					if (tradingFund != null) {
						report.nikko = tradingFund.salesFee.add(smallDecimal);
					}
				}
				{
					var tradingFund = rakutenMap.getOrDefault(isinCode, null);
					if (tradingFund != null) {
						report.rakuten = tradingFund.salesFee.add(smallDecimal);
					}
				}
				{
					var tradingFund = smtbMap.getOrDefault(isinCode, null);
					if (tradingFund != null) {
						report.smtb = tradingFund.salesFee.add(smallDecimal);
					}
				}
				{
					var tradingFund = sonyMap.getOrDefault(isinCode, null);
					if (tradingFund != null) {
						report.sony = tradingFund.salesFee.add(smallDecimal);
					}
				}
				{
					var tradingFund = clickMap.getOrDefault(isinCode, null);
					if (tradingFund != null) {
						report.click = tradingFund.salesFee.add(smallDecimal);
					}
				}
			} else {
				// ETF
				report.nikko   = smallDecimal;
				report.rakuten = smallDecimal;
				report.prestia = BigDecimal.ZERO;
				report.smtb    = BigDecimal.ZERO;
				report.sony    = BigDecimal.ZERO;
				report.click   = BigDecimal.ZERO;
			}

			// special case
			if (fundInfo.redemptionDate.toString().compareTo(FundInfoJP.NO_REDEMPTION_DATE_STRING) == 0) {
				report.redemption = NO_DATE;
			}

			if (report.div1Y.equals(BigDecimal.ZERO)) {
				report.yield1Y = BigDecimal.ZERO;
			}
			if (report.div3Y.equals(BigDecimal.ZERO)) {
				report.yield3Y = BigDecimal.ZERO;
			}
			if (report.div5Y.equals(BigDecimal.ZERO)) {
				report.yield5Y = BigDecimal.ZERO;
			}
			if (report.div10Y.equals(BigDecimal.ZERO)) {
				report.yield10Y = BigDecimal.ZERO;
			}

			list.add(report);
		}

		logger.info("fundList        {}", fundInfoList.size());
		logger.info("divScoreMap     {}", divScoreMap.size());
		logger.info("countNoPrice    {}", countNoPrice);
		logger.info("countNoDivScore {}", countNoDivScore);

		return list;
	}
	private void generateReport(List<ReportForm> reportList) {
		var urlReport = StringUtil.toURLString(StorageReportFundJP.ReportODS.getFile());
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

	private static String durationInYearMonth(LocalDate startDate, LocalDate endDate) {
		// startDate and endDate is inclusive
		if (startDate.isAfter(endDate)) {
			return "0.00";
		} else {
			LocalDate endDatePlusOne = endDate.plusDays(1);
			Period    period         = startDate.until(endDatePlusOne);
			return String.format("%d.%02d", period.getYears(), period.getMonths());
		}
	}
}
