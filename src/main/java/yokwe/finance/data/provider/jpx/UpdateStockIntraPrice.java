package yokwe.finance.data.provider.jpx;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.core5.http2.HttpVersionPolicy;

import yokwe.finance.data.type.StockCodeJP;
import yokwe.finance.tool.Makefile;
import yokwe.util.FileUtil;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.FileTask;
import yokwe.util.http.HttpUtil;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.Task;
import yokwe.util.json.JSON;
import yokwe.util.update.UpdateComplexTask;

public class UpdateStockIntraPrice extends UpdateComplexTask<StockCodeName> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
			input(StorageJPX.StockCodeName).
			output(StorageJPX.StockIntraPrice).
			build();
		
	public static void main(String[] args) {
		callUpdate();
	}
	
	
	@Override
	protected List<StockCodeName> getList() {
		return StorageJPX.StockCodeName.getList();
	}


	@Override
	protected void delistUnknownFile(List<StockCodeName> stockList) {
		var validNameList = stockList.stream().map(o -> o.stockCode).toList();
		StorageJPX.StockIntraPrice.delistUnknownFile(validNameList);
		
		// delete all JSON files
		FileUtil.deleteFile(StorageJPX.StockIntraPriceJSON.getDir(), o -> o.getName().endsWith(".json"));
	}


	@Override
	protected List<Task> getTaskList(List<StockCodeName> stockList) {
		var taskList = new ArrayList<Task>(stockList.size());
		
		for(var stock: stockList) {
			var stockCode  = stock.stockCode;
			var uriString  = String.format(URL_FORMAT, StockCodeJP.toStockCode4(stockCode));
			var file = StorageJPX.StockIntraPriceJSON.getFile(stockCode);
			
			if (!file.exists()) {
				var task = FileTask.get(uriString, file);
				taskList.add(task);
			}
		}
		
		return taskList;
	}
	private static final String URL_FORMAT = "https://quote.jpx.co.jp/jpxhp/chartapi/jcgi/qjsonp.cgi?F=json/ja_stk_hist_i&quote=%s/T";
	private static final String REFERER    = "https://quote.jpx.co.jp/jpxhp/main/index.aspx?f=stock_detail&disptype=information&qcode=%s";
	
	
	@Override
	protected void downloadFile(List<Task> taskList) {
		if (taskList.isEmpty()) return;
		
		Download download = new DownloadSync();
		initialize(download);
		download.setReferer(REFERER);
		
		for(var task: taskList) {
			download.addTask(task);
		}
		
		logger.info("BEFORE RUN");
		download.startAndWait();
		logger.info("AFTER  RUN");
	}
	private static void initialize(Download download) {
		int threadCount       = 20;
		int maxPerRoute       = 50;
		int maxTotal          = 100;
		int soTimeout         = 30;
		int connectionTimeout = 30;
		int progressInterval  = 500;
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
	protected void updateFile(List<StockCodeName> stockList) {
		int count = 0;
		for(var stock: stockList) {
			if ((++count % 1000) == 1) logger.info("{}  /  {}  {}", count, stockList.size(), stock.stockCode);
			
			var string = StorageJPX.StockIntraPriceJSON.load(stock.stockCode);
			var result = JSON.unmarshal(StockIntraPrice.class, string);
			if (result.section1.data == null) {
				logger.warn("result.section1.data is null  {}  {}", stock.stockCode, stock.name);
				continue;
			}
			
			var list = new ArrayList<OHLCVDateTime>();
			
			for(var data: result.section1.data.values()) {
				var stockCode = StockCodeJP.toStockCode5(data.TTCODE2);
				
				update(list, stockCode, data.HISTMDATE1, data.HISTMIN1);
				update(list, stockCode, data.HISTMDATE2, data.HISTMIN2);
				update(list, stockCode, data.HISTMDATE3, data.HISTMIN3);
				update(list, stockCode, data.HISTMDATE4, data.HISTMIN4);
				update(list, stockCode, data.HISTMDATE5, data.HISTMIN5);
				update(list, stockCode, data.HISTMDATE6, data.HISTMIN6);
				
				update(list, stockCode, data.HISTMDATE7,  data.HISTMIN7);
				update(list, stockCode, data.HISTMDATE8,  data.HISTMIN8);
				update(list, stockCode, data.HISTMDATE9,  data.HISTMIN9);
				update(list, stockCode, data.HISTMDATE10, data.HISTMIN10);
				
				StorageJPX.StockIntraPrice.save(stockCode, list);
			}
		}
		
		StorageJPX.StockIntraPrice.touch();
	}
	private void update(List<OHLCVDateTime> list, String stockCode, String histmdate, String histmin) {
		if (histmin.isEmpty()) return;
		
		var date = LocalDate.parse(histmdate.replace('/', '-'));
		
		for(var data: histmin.split("\\\\n")) {
			var e = data.split(",");
			// sanity check
			if (e.length != 7) {
				logger.error("Unexpected data");
				logger.error("  e  {}!", ToString.withoutFieldName(e));
				throw new UnexpectedException("Unexpected data");
			}
			update(list, stockCode, date, e);
		}
	}
	private void update(List<OHLCVDateTime> list, String stockCode, String histmdate, String[][] histmin) {
		if (histmdate.isEmpty()) return;
		
		var date = LocalDate.parse(histmdate.replace('/', '-'));
		
		for(var e: histmin) {
			update(list, stockCode, date, e);
		}
	}
	private void update(List<OHLCVDateTime> list, String stockCode, LocalDate date, String[] e) {
		// sanity check
		if (e.length != 7) {
			logger.error("Unexpected data");
			logger.error("  e  {}!", ToString.withoutFieldName(e));
			throw new UnexpectedException("Unexpected data");
		}
		var time   = LocalTime.parse(e[0]);
		var open   = new BigDecimal(e[1]);
		var close  = new BigDecimal(e[2]);
		var high   = new BigDecimal(e[3]);
		var low    = new BigDecimal(e[4]);
		var volume = Long.valueOf(e[6]);
		
		list.add(new OHLCVDateTime(LocalDateTime.of(date, time), open, high, low, close, volume));
	}
}
