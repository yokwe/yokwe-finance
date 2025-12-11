package yokwe.finance.data.provider.nikkei;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.data.fund.jp.StorageJP;
import yokwe.finance.data.type.FundDivScore;
import yokwe.finance.data.type.FundInfoJP;
import yokwe.util.FileUtil;
import yokwe.util.Makefile;
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
}
