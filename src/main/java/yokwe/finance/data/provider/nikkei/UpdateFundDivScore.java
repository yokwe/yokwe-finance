package yokwe.finance.data.provider.nikkei;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.jita.StorageJITA;
import yokwe.finance.data.type.FundDivScore;
import yokwe.util.FileUtil;
import yokwe.util.Makefile;
import yokwe.util.ScrapeUtil;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.update.UpdateBase;

public class UpdateFundDivScore extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static Makefile MAKEFILE = Makefile.builder().
		input(StorageNikkei.Webpage).
		output(StorageNikkei.FundDivScore).
		build();

	public static void main(String[] args) {
		callUpdate();
	}

	@Override
	public void update() {
		updateFile();
	}

	void updateFile() {
		var set = Arrays.asList(StorageNikkei.Webpage.getDir().list()).stream().filter(o -> o.endsWith(".html")).map(o -> o.replace(".html", "")).collect(Collectors.toSet());
		logger.info("set           {}", set.size());

		var fundInfoList = StorageJITA.FundInfoJITA.getList();
		logger.info("fundInfoList  {}", fundInfoList.size());
		fundInfoList.removeIf(o -> !set.contains(o.fundCode));
		logger.info("fundInfoList  {}", fundInfoList.size());

		// read existing file
		var list = new ArrayList<FundDivScore>(set.size());

		int count  = 0;
		int countA = 0;
		int countB = 0;
		int countC = 0;
		int countD = 0;

		for(var fundInfo: fundInfoList) {
			if ((count++ % 1000) == 0) {
				logger.info("updateFile  {}  /  {}", count, fundInfoList.size());
			}

			var fundCode  = fundInfo.fundCode;
			var isinCode  = fundInfo.isinCode;
			var stockCode = fundInfo.stockCode;
			var name      = fundInfo.name;

			var page = FileUtil.read().file(StorageNikkei.Webpage.getFile(fundCode));

			var divScoreInfo = DivScoreInfo.getInstance(page);
			if (divScoreInfo == null) {
				logger.error("divScoreInfo is null");
				logger.error("  {}  {}  {}  {}", isinCode, fundCode, stockCode, name);
				throw new UnexpectedException("divScoreInfo is null");
			}
			var divValueInfo = DivValueInfo.getInstance(page);
			if (divValueInfo == null) {
				logger.error("divValueInfo is null");
				logger.error("  {}  {}  {}  {}", isinCode, fundCode, stockCode, name);
				throw new UnexpectedException("divScoreInfo is null");
			}


			BigDecimal score1Y;
			BigDecimal score3Y;
			BigDecimal score5Y;
			BigDecimal score10Y;
			if (stockCode.isEmpty()) {
				// fund
				score1Y  = fromPercentString(divScoreInfo.score1Y);
				score3Y  = fromPercentString(divScoreInfo.score3Y);
				score5Y  = fromPercentString(divScoreInfo.score5Y);
				score10Y = fromPercentString(divScoreInfo.score10Y);
			} else {
				// ETF
				score1Y  = BigDecimal.ONE;
				score3Y  = BigDecimal.ONE;
				score5Y  = BigDecimal.ONE;
				score10Y = BigDecimal.ONE;
			}

			var divDate  = fromDateString(divValueInfo.divDate);
			var divValue = fromNumericString(divValueInfo.divValue);
			var divYield = fromPercentString(divValueInfo.divYield);
			var divPrice = fromNumericString(divValueInfo.divPrice);

			FundDivScore fundDivScore = new FundDivScore(
				isinCode, fundCode, stockCode,
				score1Y, score3Y, score5Y, score10Y,
				divDate, divValue, divPrice, divYield,
				name);
			list.add(fundDivScore);

			if (!FundDivScore.isValid(divDate)) {
//				countA++;
			}
			if (!FundDivScore.isValid(divValue)) {
				countB++;
			}
			if (!FundDivScore.isValid(divPrice)) {
				countC++;
			}
			if (!FundDivScore.isValid(divYield)) {
				countD++;
			}

			if (FundDivScore.isValid(divDate) && !FundDivScore.isValid(divYield)) {
				// not past one year
				logger.info("XX  {}  {}  {}  {}  {}", isinCode, fundInfo.inceptionDate, divDate, divYield, name);
				countA++;
			}
		}

		logger.info("count   {}", count);
		logger.info("countA  {}", countA);
		logger.info("countB  {}", countB);
		logger.info("countC  {}", countC);
		logger.info("countD  {}", countD);

		StorageNikkei.FundDivScore.save(list);
	}
	private static BigDecimal fromPercentString(String percentString) {
		String string = percentString.trim().replace("%", "");
		return string.compareTo("--") == 0 ? FundDivScore.NO_VALUE : new BigDecimal(string).movePointLeft(2);
	}
	private static BigDecimal fromNumericString(String numericString) {
		String string = numericString.replace(",", "");
		return string.compareTo("--") == 0 ? FundDivScore.NO_VALUE : new BigDecimal(string);
	}
	private static LocalDate fromDateString(String dateString) {
		return dateString.compareTo("--") == 0 ? FundDivScore.NO_DATE : LocalDate.parse(dateString, DATE_FORMAT);
	}
	private static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy年M月d日");


	public static class DivScoreInfo {
		/*
		//<!-- ▼ QP-BUNPAISD：分配金健全度 ▼ -->
		//<div class="m-articleFrame a-w100p">
		//		    <div class="m-headline">
		//		        <h2 class="m-headline_text">分配金健全度<a href="//www.nikkei.com/help/contents/markets/fund/#qf13" target="_blank" class="m-iconQ">（解説）</a></h2>
		//		    </div>
		//		    <div class="m-tableType01 a-mb40">
		//		        <div class="m-tableType01_table">
		//		            <table class="w668">
		//		                <thead>
		//		                <tr>
		//		                    <th class="a-taC a-w25p">1年</th>
		//		                    <th class="a-taC a-w25p">3年</th>
		//		                    <th class="a-taC a-w25p">5年</th>
		//		                    <th class="a-taC a-w25p">10年</th>
		//		                </tr>
		//		                </thead>
		//		                <tbody>
		//		                <tr>
		//		                    <td class="a-taR">0.00%</td>
		//		                    <td class="a-taR">100.00%</td>
		//		                    <td class="a-taR">100.00%</td>
		//		                    <td class="a-taR">100.00%</td>
		//		                </tr>
		//		                </tbody>
		//		            </table>
		//		        </div>
		//		    </div>
		//</div>
		//<!-- ▲ QP-BUNPAISD：分配金健全度 ▲ -->
		*/

		public static final String HEADER = "<!-- ▼ QP-BUNPAISD：分配金健全度 ▼ -->";
		public static final Pattern PAT = Pattern.compile(
			HEADER + "\\s+" +
			"<div .+?>\\s+" +
			"<div .+?>\\s+" +
			"<h2 .+?>分配金健全度.+?</h2>\\s+" +
			"</div>\\s+" +
			"<div .+?>\\s+" +
			"<div .+?>\\s+" +
			"<table .+?>\\s+" +
			"<thead>\\s+" +
			"<tr>\\s+" +
			"<th .+?>1年</th>\\s+" +
			"<th .+?>3年</th>\\s+" +
			"<th .+?>5年</th>\\s+" +
			"<th .+?>10年</th>\\s+" +
			"</tr>\\s+" +
			"</thead>\\s+" +
			"<tbody>\\s+" +
			"<tr>\\s+" +
			"<td .+?>(?<score1Y>.+?)</td>\\s+" +
			"<td .+?>(?<score3Y>.+?)</td>\\s+" +
			"<td .+?>(?<score5Y>.+?)</td>\\s+" +
			"<td .+?>(?<score10Y>.+?)</td>\\s+" +
			"</tr>\\s+" +
			"</tbody>\\s+" +
			"</table>\\s+" +
			"</div>\\s+" +
			"</div>\\s+" +
			"</div>\\s+" +
			""
		);
		public static DivScoreInfo getInstance(String page) {
			return ScrapeUtil.get(DivScoreInfo.class, PAT, page);
		}

		public String score1Y;
		public String score3Y;
		public String score5Y;
		public String score10Y;

		public DivScoreInfo(
			String score1Y,
			String score3Y,
			String score5Y,
			String score10Y
		) {
			this.score1Y = score1Y;
			this.score3Y = score3Y;
			this.score5Y = score5Y;
			this.score10Y = score10Y;
		}
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}

	public static class DivValueInfo {
//		<!-- ▼ QP-YIELD：分配金利回り ▼ -->
//		<div class="m-articleFrame a-w100p">
//        <div class="m-headline">
//            <h2 class="m-headline_text">分配金利回り<a href="//www.nikkei.com/help/contents/markets/fund/#qf12" target="_blank" class="m-iconQ">（解説）</a></h2>
//        </div>
//        <div class="m-tableType01 a-mb40">
//            <div class="m-tableType01_table">
//                <table class="w668 rsp_table">
//                    <tbody>
//                    <tr>
//                        <th>直近決算日</th>
//                        <td class="a-taR">2026年7月10日</td>
//                    </tr>
//                    <tr class="bgcGray">
//                        <th>分配金</th>
//                        <td class="a-taR">794円</td>
//                    </tr>
//                    <tr>
//                        <th>分配金利回り(1年)</th>
//                        <td class="a-taR">1.81%</td>
//                    </tr>
//                    <tr class="bgcGray">
//                        <th>決算日の基準価格</th>
//                        <td class="a-taR">42,508円</td>
//                    </tr>
//                    </tbody>
//                </table>
//            </div>
//        </div>
//    </div>

		public static final String HEADER = "<!-- ▼ QP-YIELD：分配金利回り ▼ -->";
		public static final Pattern PAT = Pattern.compile(
			HEADER + "\\s+" +
			"<div .+?>\\s+" +
			"<div .+?>\\s+" +
			"<h2 .+?>分配金利回り.+?</h2>\\s+" +
			"</div>\\s+" +
			"<div .+?>\\s+" +
			"<div .+?>\\s+" +
			"<table .+?>\\s+" +
			"<tbody>\\s+" +

			"<tr.*?>\\s+" +
			"<th>直近決算日</th>\\s+" +
			"<td .+?>(?<divDate>.+?)</td>\\s+" + // <td class="a-taR">2026年7月10日</td>
			"</tr>\\s+" +

			"<tr.*?>\\s+" +
			"<th>分配金</th>\\s+" +
			"<td .+?>(?<divValue>.+?)円</td>\\s+" + // <td class="a-taR">794円</td>
			"</tr>\\s+" +

			"<tr.*?>\\s+" +
			"<th>分配金利回り\\(1年\\)</th>\\s+" +
			"<td .+?>(?<divYield>.+?)%</td>\\s+" + //  <td class="a-taR">1.81%</td>
			"</tr>\\s+" +

			"<tr.*?>\\s+" +
			"<th>決算日の基準価格</th>\\s+" +
			"<td .+?>(?<divPrice>.+?)円</td>\\s+" + //  <td class="a-taR">42,508円</td>
			"</tr>\\s+" +

			"</tbody>\\s+" +
			"</table>\\s+" +
			"</div>\\s+" +
			"</div>\\s+" +
			"</div>\\s+" +

			""
		);
		public static DivValueInfo getInstance(String page) {
			return ScrapeUtil.get(DivValueInfo.class, PAT, page);
		}

		public String  divDate;
		public String divValue;
		public String divYield;
		public String divPrice;

		public DivValueInfo(
			String divDate,
			String divValue,
			String divYield,
			String divPrice
		) {
			this.divDate  = divDate;
			this.divValue = divValue;
			this.divYield = divYield;
			this.divPrice = divPrice;
		}
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}

	}

}
