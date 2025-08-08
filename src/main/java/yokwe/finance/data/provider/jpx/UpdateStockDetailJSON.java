package yokwe.finance.data.provider.jpx;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.hc.core5.http2.HttpVersionPolicy;

import yokwe.finance.data.type.CodeName;
import yokwe.finance.data.type.StockCodeJP;
import yokwe.finance.tool.Makefile;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.FileTask;
import yokwe.util.http.HttpUtil;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.Task;
import yokwe.util.update.UpdateComplexTask;

public class UpdateStockDetailJSON extends UpdateComplexTask<CodeName> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input(StorageJPX.StockList).
		output(StorageJPX.StockDetailJSON).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	public static List<File> getJSONFileList() {
		var array = StorageJPX.StockDetailJSON.getDir().listFiles(o -> o.isFile() && o.getName().endsWith(".json"));
		var list  = new ArrayList<File>(Arrays.asList(array));
		Collections.sort(list);
		return list;
	}

	
	@Override
	protected List<CodeName> getList() {
		return StorageJPX.StockList.getList();
	}
	
	@Override
	protected void delistUnknownFile(List<CodeName> stockList) {
		var validNameList = stockList.stream().map(o -> o.code).toList();
		StorageJPX.StockDetailJSON.delistUnknownFile(validNameList);
	}
	
	@Override
	protected List<Task> getTaskList(List<CodeName> stockList) {
		var list = new ArrayList<Task>();
		
		for(var stock: stockList) {
			var code = stock.code;
			var file = StorageJPX.StockDetailJSON.getFile(code);
			if (file.length() == 0 || needsUpdate(file)) {
				var url = getURL(code);
				list.add(FileTask.get(url, file));
			}
		}
		
		return list;
	}
	
	private static final String URL_FORMAT = "https://quote.jpx.co.jp/jpxhp/jcgi/wrap/qjsonp.aspx?F=ctl/stock_detail&qcode=%s";
	private String getURL(String stockCode) {
		var stockCode4 = StockCodeJP.toStockCode4(stockCode);
		return String.format(URL_FORMAT, stockCode4);
	}
	
	private static final String REFERER = "https://quote.jpx.co.jp/jpxhp/main/index.aspx";
	
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
		int threadCount       = 10;
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
	protected void updateFile(List<CodeName> stockList) {
		// touch file
		StorageJPX.StockDetailJSON.touch();
	}
}
