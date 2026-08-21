package yokwe.finance.data.stock.us;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import yokwe.finance.data.type.StockInfoUS;
import yokwe.util.FileUtil;
import yokwe.util.Makefile;
import yokwe.util.ThreadUtil;
import yokwe.util.http.HttpUtil;
import yokwe.util.update.UpdateComplexGeneric;

public class UpdateQuotes extends UpdateComplexGeneric<StockInfoUS, StockInfoUS> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	protected static Makefile MAKEFILE = Makefile.builder().
		input(StorageStockUS.StockInfoUS).
		output(StorageStockUS.Quotes).
		build();

	public static void main(String[] args) {
		callUpdate();
	}

	Duration durationBetweenDownload = Duration.ofMillis(200); // 1000
	Duration durationDownloadFailed  = Duration.ofSeconds(60);

	@Override
	protected void initialize() {
		gracePeriod = Duration.ofHours(18);

		logger.info("durationBetweenDownload  {} millis", durationBetweenDownload.toMillis());
		logger.info("durationDownloadFailed   {} millis", durationDownloadFailed.toMillis());
	}

	@Override
	protected List<StockInfoUS> getList() {
		return StorageStockUS.StockInfoUS.getList();
	}

	@Override
	protected void delistUnknownFile(List<StockInfoUS> stockInfoList) {
		Set<String> validNameSet = stockInfoList.stream().map(o -> o.stockCode).collect(Collectors.toSet());
		StorageStockUS.Quotes.delistUnknownFile(validNameSet);
	}

	@Override
	protected List<StockInfoUS> getTaskList(List<StockInfoUS> stockInfoList) {
		int countA = 0;
		int countB = 0;
		int countC = 0;

		var list = new ArrayList<StockInfoUS>();

		// remove garbage file
//		for(var stockInfo: stockInfoList) {
//			var symbol = stockInfo.stockCode;
//			var file   = StorageUS.Quotes.getFile(symbol);
//			if (file.length() < 1000) {
//				FileUtil.delete(file);
//			}
//		}
		for(var stockInfo: stockInfoList) {
			var symbol = stockInfo.stockCode;
			var file   = StorageStockUS.Quotes.getFile(symbol);
			if (!file.exists()) {
				list.add(stockInfo);
				countA++;
			} else if (needsUpdate(file)) {
				list.add(stockInfo);
				countB++;
			} else {
				countC++;
			}
		}

		logger.info("countA  {}", countA);
		logger.info("countB  {}", countB);
		logger.info("countC  {}", countC);
		return list;
	}
	private String getURL(String symbol) {
		return String.format("https://www.nyse.com/api/nyseservice/v1/quotes?symbol=%s", symbol);
	}

	@Override
	protected void downloadFile(List<StockInfoUS> taskList) {
		if (taskList.isEmpty()) {
			return;
		}

		Collections.shuffle(taskList);

		var http = HttpUtil.getInstance();

		int count = 0;
		for(var task: taskList) {
			if ((++count % 50) == 1) {
				logger.info("{}  /  {}  {}", count - 1, taskList.size(), task.name);
			} else {
//				logger.info("{}  /  {}  {}", count - 1, taskList.size(), task.name);
			}

			var symbol = task.stockCode;
			var url    = getURL(StockInfoUS.toNasdaqSymbol(symbol));
			var file   = StorageStockUS.Quotes.getFile(symbol);

			var string = http.withRawData(false).downloadString(url);
			if (string.startsWith("{") && string.endsWith("}")) {
				FileUtil.write().file(file, string);
			} else {
				logger.warn("download failed");
				logger.warn("  symbol  {}", symbol);
				logger.warn("  string  {}!", string);
				continue;
			}

			ThreadUtil.sleep(durationBetweenDownload);
		}
	}

	@Override
	protected void updateFile(List<StockInfoUS> list) {
		// touch if no needs to update;
		boolean touchFlag = true;
		for(var e: list) {
			var symbol = e.stockCode;
			var file = StorageStockUS.Quotes.getFile(symbol);
			if (needsUpdate(file)) {
				touchFlag = false;
				break;
			}
		}
		if (touchFlag) {
			StorageStockUS.Quotes.touch();
		}
	}

}
