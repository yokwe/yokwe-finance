package yokwe.finance.data.stock.us;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.hc.core5.http2.HttpVersionPolicy;

import yokwe.finance.data.provider.nasdaq.api.API;
import yokwe.finance.data.provider.nasdaq.api.AssetClass;
import yokwe.finance.data.provider.nasdaq.api.Historical;
import yokwe.finance.data.type.OHLCV;
import yokwe.finance.data.type.StockInfoUS;
import yokwe.util.Makefile;
import yokwe.util.MarketHoliday;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.HttpUtil;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.StringTask;
import yokwe.util.http.Task;
import yokwe.util.json.JSON;
import yokwe.util.update.UpdateComplexTask;

public class UpdateStockPriceOHLCV extends UpdateComplexTask<StockInfoUS> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	protected static Makefile MAKEFILE = Makefile.builder().
		input(StorageUS.StockInfo).
		output(StorageUS.StockPriceOHLCV).
		build();
	
	public static void main(String[] args) throws IOException {
		callUpdate();
	}
	

	private final LocalDate lastTradingDate = MarketHoliday.US.getLastTradingDate();
	
	@Override
	protected List<StockInfoUS> getList() {
		return StorageUS.StockInfo.getList();
	}
	
	@Override
	protected void delistUnknownFile(List<StockInfoUS> stockInfoList) {
		Set<String> validNameSet = stockInfoList.stream().map(o -> o.stockCode).collect(Collectors.toSet());
		StorageUS.StockPriceOHLCV.delistUnknownFile(validNameSet);
	}
	
	// Historical returns 10 years data maximum
	private static final LocalDate EPOCH_DATE = LocalDate.of(2010, 1, 1);

	@Override
	protected List<Task> getTaskList(List<StockInfoUS> stockInfoList) {
		int countA = 0;
		int countB = 0;
		int countC = 0;
		int countD = 0;
		
		var list = new ArrayList<Task>();
		
		for(var stockInfo: stockInfoList) {
			var stockCode = stockInfo.stockCode;
			var file      = StorageUS.StockPriceOHLCV.getFile(stockCode);
			if (needsUpdate(file)) {
				final LocalDate fromDate;
				final LocalDate toDate = lastTradingDate;
				
				// If fromDate equals toDate, Historical.getInstance() returns no data.
				// So temporary decrease fromDate
				var priceList = StorageUS.StockPriceOHLCV.getList(stockCode);
				if (priceList.isEmpty()) {
					fromDate = EPOCH_DATE;
					countA++;
				} else {
					var lastDate = priceList.getLast().date;
					if (lastDate.equals(lastTradingDate)) {
						// already processed, skip this entry
						countB++;
						continue;
					} else {
						fromDate = lastDate;
						countC++;
					}
				}
				var url = getURL(stockInfo, fromDate, toDate);
				list.add(StringTask.get(url, new MyConsumer(stockInfo, priceList)));
			} else {
				countD++;
			}
		}

		logger.info("countA  {}", countA);
		logger.info("countB  {}", countB);
		logger.info("countC  {}", countC);
		logger.info("countD  {}", countD);
		return list;
	}
	
	private String getURL(StockInfoUS stockInfo, LocalDate fromDate, LocalDate toDate) {
		var symbol     = stockInfo.stockCode;
		var assetClass = stockInfo.type.isETF() ? AssetClass.ETF : AssetClass.STOCK;
		return Historical.getURL(symbol, assetClass, fromDate, toDate, 99999);
	}
	
	private static class MyConsumer implements Consumer<String> {
		public final StockInfoUS  stockInfo;
		public final List<OHLCV>  list;
		
		public MyConsumer(StockInfoUS stockInfo, List<OHLCV> list) {
			this.stockInfo = stockInfo;
			this.list      = list;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s}", stockInfo.stockCode, stockInfo.name);
		}
		
		private OHLCV toOHLCV(Historical.Values values) {
			LocalDate  date   = LocalDate.parse(API.convertDate(values.date));
			BigDecimal open   = new BigDecimal(values.open.replace(",", "").replace("$", ""));
			BigDecimal high   = new BigDecimal(values.high.replace(",", "").replace("$", ""));
			BigDecimal low    = new BigDecimal(values.low.replace(",", "").replace("$", ""));
			BigDecimal close  = new BigDecimal(values.close.replace(",", "").replace("$", ""));
			long       volume = Long.parseLong(values.volume.replace(",", "").replace(API.NOT_AVAILABLE, "0"));
			
			return new OHLCV(date, open, high, low, close, volume);
		}
		
		@Override
		public void accept(String string) {
			if (string.contains("<HTML>")) {
				logger.warn("string is HTML  {}  {}", stockInfo.stockCode, stockInfo.name);
				return;
			}

			var historical = JSON.unmarshal(Historical.class, string);
			if (historical == null || historical.data == null || historical.data.tradesTable == null || historical.data.tradesTable.rows == null) {
				// Update last modified time of file to stop process again
				// FIXME Is this correct?
				logger.warn("no data  {}  {}", stockInfo.stockCode, stockInfo.name);
				StorageUS.StockPriceOHLCV.save(stockInfo.stockCode, list);
				return;
			}
			
			// map of existing data
			var map = list.stream().collect(Collectors.toMap(o -> o.date, Function.identity()));
			
			for(var row: historical.data.tradesTable.rows) {
				var newPrice = toOHLCV(row);
				
				if (map.containsKey(newPrice.date)) {
					var oldPrice = map.get(newPrice.date);
					if (oldPrice.equals(newPrice)) {
						// expected
					} else {
						// not expected
						logger.warn("Unexpected price");
						logger.warn("  stock     {}  {}", stockInfo.stockCode, stockInfo.name);
						logger.warn("  oldPrice  {}", oldPrice);
						logger.warn("  newPrice  {}", newPrice);
						// replace with new value
						map.put(newPrice.date, newPrice);
					}
				} else {
					// no existing date
					map.put(newPrice.date, newPrice);
				}
			}
			StorageUS.StockPriceOHLCV.save(stockInfo.stockCode, map.values());
		}
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
		StorageUS.StockPriceOHLCV.touch();
	}
}
