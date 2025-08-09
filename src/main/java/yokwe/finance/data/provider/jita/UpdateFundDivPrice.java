package yokwe.finance.data.provider.jita;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.core5.http2.HttpVersionPolicy;

import yokwe.finance.data.type.DailyValue;
import yokwe.finance.data.type.FundInfoJP;
import yokwe.finance.data.type.FundPriceJP;
import yokwe.util.CSVUtil;
import yokwe.util.Makefile;
import yokwe.util.UnexpectedException;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.FileTask;
import yokwe.util.http.HttpUtil;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.Task;
import yokwe.util.update.UpdateComplexTask;

public class UpdateFundDivPrice extends UpdateComplexTask<FundInfoJP> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input(StorageJITA.FundInfo).
		output(StorageJITA.FundDiv, StorageJITA.FundPrice).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	protected List<FundInfoJP> getList() {
		return StorageJITA.FundInfo.getList();
	}
	
	@Override
	protected void delistUnknownFile(List<FundInfoJP> fundInfoList) {
		var validNameList = fundInfoList.stream().map(o -> o.isinCode).toList();
		
		StorageJITA.FundDivPrice.delistUnknownFile(validNameList);
		StorageJITA.FundDiv.delistUnknownFile(validNameList);
		StorageJITA.FundPrice.delistUnknownFile(validNameList);
	}
	
	private static final Charset CHARSET = Charset.forName("SHIFT_JIS");
	
	@Override
	protected List<Task> getTaskList(List<FundInfoJP> fundInfoList) {
		var list = new ArrayList<Task>();
		
		for(var fund: fundInfoList) {
			var isinCode = fund.isinCode;
			var file     = StorageJITA.FundDivPrice.getFile(isinCode);
			if (file.length() == 0 || needsUpdate(file)) {
				var url = getURL(fund);
				
				list.add(FileTask.get(url, file, CHARSET));
			}
		}
		
		return list;
	}
	
	private static String URL_FORMAT = "https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000/csv-file-download?isinCd=%s&associFundCd=%s";
	private String getURL(FundInfoJP fundInfo) {
		return String.format(URL_FORMAT, fundInfo.isinCode, fundInfo.fundCode);
	}
	
	@Override
	protected void downloadFile(List<Task> taskList) {
		logger.info("updateFile");
		if (taskList.isEmpty()) return;
		
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
	protected void updateFile(List<FundInfoJP> fundInfoList) {
		logger.info("updateFile");
		int count = 0;
		for(var fundInfo: fundInfoList) {
			var isinCode = fundInfo.isinCode;
			
			if ((++count % 500) == 1) logger.info("{}  /  {}  {}", count, fundInfoList.size(), isinCode);
			
			List<FundDivPrice> divPriceList;
			{
				var file = StorageJITA.FundDivPrice.getFile(isinCode);
				divPriceList = CSVUtil.read(FundDivPrice.class).file(file);
				if (divPriceList == null) {
					logger.error("Unexpected null");
					logger.error("  file  {}", file.getPath());
					throw new UnexpectedException("Unexpected null");
				}
			}
			
			// build divList and priceList
			var divList   = new ArrayList<DailyValue>();
			var priceList = new ArrayList<FundPriceJP>();
			
			for(var divPrice: divPriceList) {
				// sanity check
				if (divPrice.price.isEmpty() || divPrice.nav.isEmpty()) {
					logger.warn("Skip unexpected divPrice  {}  {}", isinCode, divPrice);
					continue;
				}
				LocalDate  date   = toLocalDate(divPrice.date);
				BigDecimal price  = new BigDecimal(divPrice.price);
				BigDecimal nav    = new BigDecimal(divPrice.nav).scaleByPowerOfTen(6); // 純資産総額（百万円）
				String     div    = divPrice.div.trim();
				
				if (!div.isEmpty()) divList.add(new DailyValue(date, new BigDecimal(div)));
				
				priceList.add(new FundPriceJP(date, nav, price));
			}
			
			// save divList and priceList
			StorageJITA.FundDiv.save(isinCode, divList);
			StorageJITA.FundPrice.save(isinCode, priceList);
		}
		
		// touch file
		StorageJITA.FundDiv.touch();
		StorageJITA.FundPrice.touch();
	}
	
	private LocalDate toLocalDate(String dateString) {
		// 2000年01月01日
		// 01234 567 890
		if (dateString.length() == 11 && dateString.charAt(4) == '年' && dateString.charAt(7) == '月' && dateString.charAt(10) == '日') {
			int yyyy = Integer.parseInt(dateString.substring(0, 4));
			int mm   = Integer.parseInt(dateString.substring(5, 7));
			int dd   = Integer.parseInt(dateString.substring(8, 10));
			return LocalDate.of(yyyy, mm, dd);
		} else {
			logger.error("Unexpected date");
			logger.error("  dateString {}  !{}!", dateString.length(), dateString);
			throw new UnexpectedException("Unexpected date");
		}
	}
}
