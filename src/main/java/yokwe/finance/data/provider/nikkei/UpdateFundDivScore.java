package yokwe.finance.data.provider.nikkei;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.data.fund.jp.StorageJP;
import yokwe.finance.data.type.FundDivScore;
import yokwe.finance.data.type.FundInfoJP;
import yokwe.util.FileUtil;
import yokwe.util.Makefile;
import yokwe.util.ScrapeUtil;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.update.UpdateBase;

public class UpdateFundDivScore extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
//		input(StorageJITA.FundInfo).
		output(StorageNikkei.FundDivScore).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		var list = getList();
		logger.info("list  {}", list.size());

		for(int retry = 1; retry < 10; retry++) {
			logger.info("  retry  {}", retry);
			var taskList = getTaskList(list);
			logger.info("  task   {}", taskList.size());
			if (taskList.isEmpty()) break;
			
			downloadFile(taskList);
			
			sleep(Duration.ofSeconds(1));
		}
		
		updateFile(list);
	}
	
	List<FundInfoJP> getList() {
		var ret = StorageJP.FundInfo.getList();
		ret.removeIf(o -> o.fundType.equals(FundInfoJP.FUND_TYPE_CEF));
		ret.removeIf(o -> o.name.contains("マネー・リザーブ・ファンド"));
		ret.removeIf(o -> o.name.contains("月号"));
		ret.removeIf(o -> o.name.contains("回公社債投資信託"));
		ret.removeIf(o -> o.name.contains("追加型・公社債証券投資信託"));
		ret.removeIf(o -> o.name.contains("財形給付金ファンド"));
		ret.removeIf(o -> o.name.contains("楽天・マネーファンド"));
		return ret;
	}
	List<FundInfoJP> getTaskList(List<FundInfoJP> list) {
		var ret = new ArrayList<FundInfoJP>();
		
		for(var e: list) {
			// skip if file exists
			if (StorageNikkei.WebPage.getFile(e.fundCode).canRead()) continue;
			ret.add(e);
			logger.info("XX  {}  {}", e.fundCode, e.name);
		}
		
		return ret;
	}
	void downloadFile(List<FundInfoJP> list) {
		var urlFormat = "https://www.nikkei.com/nkd/fund/dividend/?fcode=%s";
		
		Collections.shuffle(list); // shuffle
		
		int count = 0;
		for(var fundInfo: list) {
			if ((count++ % 10) == 0) logger.info("dowonloadFile  {}  /  {}", count, list.size());
			
			var fundCode = fundInfo.fundCode;
			var file = StorageNikkei.WebPage.getFile(fundCode);
			if (file.canRead()) continue;
						
			try {
				var url = String.format(urlFormat, fundCode);
				var string = HttpUtil.getInstance().downloadString(url);
				StorageNikkei.WebPage.save(fundCode, string);
			} catch (UnexpectedException e) {
				logger.warn("failed to download  {}  {}", fundInfo.fundCode, fundInfo.name);
				sleep(Duration.ofSeconds(10));
			}
		}
	}
	void sleep(Duration duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e1) {
			//
		}
	}
	
	void updateFile(List<FundInfoJP> fundInfoList) {
		var map = StorageNikkei.FundDivScore.getList().stream().collect(Collectors.toMap(o -> o.isinCode, Function.identity()));
		
		int count  = 0;
		int countA = 0;
		int countB = 0;
		int countC = 0;
		int countD = 0;
		
		for(var fundInfo: fundInfoList) {
			if ((count++ % 200) == 0) logger.info("updateFile  {}  /  {}", count, fundInfoList.size());
			
			var fundCode = fundInfo.fundCode;
			var isinCode = fundInfo.isinCode;
			if (map.containsKey(isinCode)) {
				countA++;
			} else {
				var file = StorageNikkei.WebPage.getFile(fundCode);
				if (file.canRead()) {
					var page = FileUtil.read().file(file);
					var divScoreInfo = DivScoreInfo.getInstance(page);
					if (divScoreInfo == null) {
						logger.info("skip  divScoreInfo is null  {}  {}", fundCode, fundInfo.name);
						countB++;
						continue;
					} else {
						BigDecimal score1Y  = fromPercentString(divScoreInfo.score1Y);
						BigDecimal score3Y  = fromPercentString(divScoreInfo.score3Y);
						BigDecimal score5Y  = fromPercentString(divScoreInfo.score5Y);
						BigDecimal score10Y = fromPercentString(divScoreInfo.score10Y);
						
						FundDivScore fundDivScore = new FundDivScore(isinCode, score1Y, score3Y, score5Y, score10Y);
						map.put(isinCode, fundDivScore);
						countC++;
					}
				} else {
					countD++;
				}				
			}
		}
		
		logger.info("count   {}", count);
		logger.info("countA  {}", countA);
		logger.info("countB  {}", countB);
		logger.info("countC  {}", countC);
		logger.info("countD  {}", countD);
		
		StorageNikkei.FundDivScore.save(map.values());
	}
	private static BigDecimal fromPercentString(String string) {
		String numericString = string.trim().replace("%", "");
		return numericString.compareTo("--") == 0 ? FundDivScore.NO_VALUE : new BigDecimal(numericString).movePointLeft(2);
	}
	
	
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
}
