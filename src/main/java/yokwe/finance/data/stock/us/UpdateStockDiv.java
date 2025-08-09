package yokwe.finance.data.stock.us;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.hc.core5.http2.HttpVersionPolicy;

import yokwe.finance.data.provider.nasdaq.api.API;
import yokwe.finance.data.provider.nasdaq.api.AssetClass;
import yokwe.finance.data.provider.nasdaq.api.Dividends;
import yokwe.finance.data.type.DailyValue;
import yokwe.finance.data.type.StockInfoUS;
import yokwe.util.Makefile;
import yokwe.util.UnexpectedException;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.HttpUtil;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.StringTask;
import yokwe.util.http.Task;
import yokwe.util.json.JSON;
import yokwe.util.update.UpdateComplexTask;

public class UpdateStockDiv extends UpdateComplexTask<StockInfoUS> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	protected static Makefile MAKEFILE = Makefile.builder().
		input(StorageUS.StockInfo).
		output(StorageUS.StockDiv).
		build();

	// It will takes 16 minutes for update.
	// 961 stock has dividend
	// 3549 stock has no dividend
	
	public static void main(String[] args) throws IOException {
		callUpdate();
	}
	
	@Override
	protected List<StockInfoUS> getList() {
		return StorageUS.StockInfo.getList();
	}
	
	@Override
	public void delistUnknownFile(List<StockInfoUS> stockInfoList) {
		Set<String> validNameSet = stockInfoList.stream().map(o -> o.stockCode).collect(Collectors.toSet());
		StorageUS.StockDiv.delistUnknownFile(validNameSet);
	}
	
	private static class MyConsumer implements Consumer<String> {
		public final StockInfoUS  stockInfo;
		public final List<DailyValue> list;
		
		public MyConsumer(StockInfoUS stockInfo, List<DailyValue> list) {
			this.stockInfo = stockInfo;
			this.list      = list;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s}", stockInfo.stockCode, stockInfo.name);
		}
		
		@Override
		public void accept(String string) {
			if (string.contains("<HTML>")) return;
			
			Dividends div = JSON.unmarshal(Dividends.class, string);			
			if (div == null || div.data == null || div.data.dividends == null || div.data.dividends.rows == null) {
				if (list.isEmpty()) {
					// Update last modified time of file
					StorageUS.StockDiv.save(stockInfo.stockCode, list);
				}
				return;
			}
			
			var newMap = new HashMap<LocalDate, DailyValue>();
			// build myMap  --  summaries data by date
			{
				for(var row: div.data.dividends.rows) {
					// Skip if exOfEffDate is N/A
					if (row.exOrEffDate.equals(API.NOT_AVAILABLE)) continue;
					
					LocalDate  date  = toLocalDate(row.exOrEffDate);
					BigDecimal value = toBigDecimal(row.amount);
					
					if (newMap.containsKey(date)) {
						DailyValue old = newMap.get(date);
						old.value = old.value.add(value);
					} else {
						newMap.put(date, new DailyValue(date, value));
					}
				}
			}
			
			// create map of existing data
			var oldMap = list.stream().collect(Collectors.toMap(o -> o.date, Function.identity()));
			// update map using mayMap
			for(var entry: newMap.entrySet()) {
				var date    = entry.getKey();
				var newValue = entry.getValue();
				
				if (oldMap.containsKey(date)) {
					var oldValue = oldMap.get(date);
					if (newValue.equals(oldValue)) {
						//
					} else {
						logger.warn("Unexpected oldValue");
						logger.warn("  oldValue  {}", oldValue);
						logger.warn("  newValue  {}", newValue);
						
						oldMap.put(date, newValue);
					}
				} else {
					oldMap.put(date, newValue);
				}
			}
			
			StorageUS.StockDiv.save(stockInfo.stockCode, oldMap.values());
		}
	}
	
	@Override
	protected List<Task> getTaskList(List<StockInfoUS> stockInfoList) {
		int countA = 0;
		int countB = 0;
		
		var list = new ArrayList<Task>();
		
		for(var stockInfo: stockInfoList) {
			var stockCode = stockInfo.stockCode;
			var file      = StorageUS.StockDiv.getFile(stockCode);
			if (needsUpdate(file)) {
				var divList = StorageUS.StockDiv.getList(stockCode);
				var limit   = divList.size() == 0 ? 99999 : 2;
				var url     = getURL(stockInfo, limit);
				list.add(StringTask.get(url, new MyConsumer(stockInfo, divList)));
				countA++;
			} else {
				countB++;
			}
		}
		
		logger.info("countA  {}", countA);
		logger.info("countB  {}", countB);
		return list;
	}
	
	private String getURL(StockInfoUS stockInfo, int limit) {
		var symbol     = stockInfo.stockCode;
		var assetClass = stockInfo.type.isETF() ? AssetClass.ETF : AssetClass.STOCK;
		return Dividends.getURL(symbol, assetClass, limit);
	}
	
	private static LocalDate toLocalDate(String string) {
		// 03/22/2019
		// 0123456789
		if (string.length() == 10 && string.charAt(2) == '/' && string.charAt(5) == '/') {
			int m = Integer.parseInt(string.substring(0, 2), 10);
			int d = Integer.parseInt(string.substring(3, 5), 10);
			int y = Integer.parseInt(string.substring(6), 10);
			return LocalDate.of(y, m, d);
		} else {
			logger.error("Unexpected string");
			logger.error("  string !{}!", string);
			throw new UnexpectedException("Unexpected string");
		}
	}
	private static BigDecimal toBigDecimal(String string) {
		return new BigDecimal(string.replace(",", "").replace("$", ""));
	}
	
	@Override
	protected void downloadFile(List<Task> taskList) {
		if (taskList.isEmpty()) return;
		
		Collections.shuffle(taskList);
		
		Download download = new DownloadSync();
		initialize(download);
		
		for(var task: taskList) {
			download.addTask(task);
		}
		
		logger.info("BEFORE RUN");
		download.startAndWait();
		logger.info("AFTER  RUN");
	}
	private static void initialize(Download download) {
		int threadCount       = 2; // 10
		int maxPerRoute       = 50;
		int maxTotal          = 100;
		int soTimeout         = 30;
		int connectionTimeout = 30;
		int progressInterval  = 100;
		logger.info("threadCount       {}", threadCount);
		logger.info("maxPerRoute       {}", maxPerRoute);
		logger.info("maxTotal          {}", maxTotal);
		logger.info("soTimeout         {}", soTimeout);
		logger.info("connectionTimeout {}", connectionTimeout);
		logger.info("progressInterval  {}", progressInterval);
		
		RequesterBuilder requesterBuilder = RequesterBuilder.custom()
				.setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
				.setSoTimeout(soTimeout)
				.setMaxTotal(maxTotal)
				.setDefaultMaxPerRoute(maxPerRoute);

		download.setRequesterBuilder(requesterBuilder);
		
		// Configure custom header
		download.setUserAgent(HttpUtil.DEFAULT_USER_AGENT);
		
		// Configure thread count
		download.setThreadCount(threadCount);
		
		// connection timeout in second
		download.setConnectionTimeout(connectionTimeout);
		
		// progress interval
		download.setProgressInterval(progressInterval);
	}

	@Override
	protected void updateFile(List<StockInfoUS> list) {
		// touch file
		StorageUS.StockDiv.touch();
	}
}
