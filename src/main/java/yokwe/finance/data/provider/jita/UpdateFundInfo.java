package yokwe.finance.data.provider.jita;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hc.core5.http2.HttpVersionPolicy;

import yokwe.finance.data.provider.jpx.StockCodeName;
import yokwe.finance.data.type.FundInfoJP;
import yokwe.util.UnexpectedException;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.HttpUtil;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.StringTask;
import yokwe.util.http.Task;
import yokwe.util.json.JSON;
import yokwe.util.makefile.Makefile;
import yokwe.util.update.UpdateComplexTask;

public class UpdateFundInfo extends UpdateComplexTask<FundInfoJP> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input().
		output(StorageJITA.FundInfo).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	private MyConsumer consumer = new MyConsumer();
	
	private static class MyConsumer implements Consumer<String> {
		public List<FundInfoJP> fundList  = new ArrayList<>();
		public int              allPageNo = -1;
		public int              pageSize  = -1;
		
		@Override
		public void accept(String string) {
			var data = JSON.unmarshal(FundDataSearch.class, string);
			if (data == null) {
				logger.error("JSON unmarshal failed");
				logger.error("  string {}", string);
				throw new UnexpectedException("JSON unmarshal failed");
			} else {
				allPageNo = data.allPageNo;
				pageSize  = data.pageSize;
				
				for(var e: data.resultInfoArray) {
					fundList.add(toFund(e));
				}
			}
		}
	}
	
	
	@Override
	protected List<FundInfoJP> getList() {
		return new ArrayList<FundInfoJP>();
	}
	@Override
	protected void delistUnknownFile(List<FundInfoJP> dummy) {
		// do nothing
	}
	
	@Override
	protected List<Task> getTaskList(List<FundInfoJP> dummy) {
		var list = new ArrayList<Task>();
		
		// build list only first time 
		if (consumer.allPageNo == -1) {
			// startNo = 0
			{
				var string = HttpUtil.getInstance().withPost(getBody(0), CONTENT_TYPE).downloadString(URL);
				consumer.accept(string);
			}
			
			int pageNo  = 0;
			int startNo = 0;
			for(;;) {
				pageNo  += 1;
				startNo += consumer.pageSize;
				if (consumer.allPageNo <= pageNo) break;
				
				String content = getBody(startNo);
				list.add(StringTask.post(URL, consumer, content, CONTENT_TYPE));
			}
		}
		
		return list;
	}
	private static final String URL          = "https://toushin-lib.fwg.ne.jp/FdsWeb/FDST999900/fundDataSearch";
	private static final String CONTENT_TYPE = "application/x-www-form-urlencoded;charset=UTF-8";
	private static String getBody(int startNo) {
		return String.format(
			"t_keyword=&t_kensakuKbn=&t_fundCategory=&s_keyword=&s_kensakuKbn=1&" +
			"s_supplementKindCd=1&s_standardPriceCond1=0&s_standardPriceCond2=0&" +
			"s_riskCond1=0&s_riskCond2=0&s_sharpCond1=0&s_sharpCond2=0&s_buyFee=1&" +
			"s_trustReward=1&s_monthlyCancelCreateVal=1&s_instCd=&salesInstDiv=&" +
			"s_fdsInstCd=&startNo=%d&draw=0&searchBtnClickFlg=false", startNo);
	}
	
	@Override
	protected void downloadFile(List<Task> taskList) {
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
		int progressInterval  = 30;
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
	protected void updateFile(List<FundInfoJP> dummy) {
		checkDuplicateKey(consumer.fundList, o -> o.isinCode);
		save(consumer.fundList, StorageJITA.FundInfo); // to force update use save

	}
	
	
	private static final Pattern PAT_ESTABLISHED_DATE = Pattern.compile("(?<yyyy>[12][09][0-9][0-9])-(?<mm>[01]?[0-9])-(?<dd>[0123]?[0-9]) 00:00:00");
	private static final Pattern PAT_REDEMPTION_DATE = Pattern.compile("(?<yyyy>[12][09][0-9][0-9])(?<mm>[01]?[0-9])(?<dd>[0123]?[0-9])");
	private static FundInfoJP toFund(FundDataSearch.ResultInfo resultInfo) {
		String    isinCode       = resultInfo.isinCd;
		String    fundCode       = resultInfo.associFundCd;
		String    stockCode      = StockCodeName.getStockCode(isinCode, "");
		
		LocalDate listingDate;
		{
			// 2016-07-29 00:00:00
			Matcher m = PAT_ESTABLISHED_DATE.matcher(resultInfo.establishedDate);
			if (m.find()) {
				int yyyy = Integer.parseInt(m.group("yyyy"));
				int mm   = Integer.parseInt(m.group("mm"));
				int dd   = Integer.parseInt(m.group("dd"));
				listingDate = LocalDate.of(yyyy, mm, dd);
			} else {
				logger.error("Unexpected establishedDate");
				logger.error("  isinCode        {}", isinCode);
				logger.error("  establishedDate {}", resultInfo.establishedDate);
				throw new UnexpectedException("Unexpected establishedDate");
			}
		}
		
		LocalDate redemptionDate;
		{
			if (resultInfo.redemptionDate.equals(FundInfoJP.NO_REDEMPTION_DATE_STRING)) {
				redemptionDate = FundInfoJP.NO_REDEMPTION_DATE;
			} else {
				Matcher m = PAT_REDEMPTION_DATE.matcher(resultInfo.redemptionDate);
				if (m.find()) {
					int yyyy = Integer.parseInt(m.group("yyyy"));
					int mm   = Integer.parseInt(m.group("mm"));
					int dd   = Integer.parseInt(m.group("dd"));
					redemptionDate = LocalDate.of(yyyy, mm, dd);
				} else {
					logger.error("Unexpected redemptionDate");
					logger.error("  isinCode       {}", isinCode);
					logger.error("  redemptionDate {}", resultInfo.redemptionDate);
					throw new UnexpectedException("Unexpected redemptionDate");
				}
			}
		}
		
		int        divFreq = resultInfo.setlFqcy.equals("-") ? 0 : Integer.parseInt(resultInfo.setlFqcy);
		String     name    = resultInfo.fundNm;
		
		BigDecimal expenseRatio = resultInfo.trustReward.scaleByPowerOfTen(-2);        // percent to value
		BigDecimal buyFreeMax   = (resultInfo.buyFee != null) ? resultInfo.buyFee.scaleByPowerOfTen(-2) : BigDecimal.ZERO;
		
		String     fundType = FundDataSearch.FundType.getInstance(resultInfo.unitOpenDiv).getName();

		String investingArea;
		{
			List<String> areaList = new ArrayList<>();
			if (resultInfo.investArea10kindCd1.equals("1")) areaList.add(FundDataSearch.InvestingArea.getInstance("1").getName());
			if (resultInfo.investArea10kindCd2.equals("1")) areaList.add(FundDataSearch.InvestingArea.getInstance("2").getName());
			if (resultInfo.investArea10kindCd3.equals("1")) areaList.add(FundDataSearch.InvestingArea.getInstance("3").getName());
			if (resultInfo.investArea10kindCd4.equals("1")) areaList.add(FundDataSearch.InvestingArea.getInstance("4").getName());
			if (resultInfo.investArea10kindCd5.equals("1")) areaList.add(FundDataSearch.InvestingArea.getInstance("5").getName());
			if (resultInfo.investArea10kindCd6.equals("1")) areaList.add(FundDataSearch.InvestingArea.getInstance("6").getName());
			if (resultInfo.investArea10kindCd7.equals("1")) areaList.add(FundDataSearch.InvestingArea.getInstance("7").getName());
			if (resultInfo.investArea10kindCd8.equals("1")) areaList.add(FundDataSearch.InvestingArea.getInstance("8").getName());
			if (resultInfo.investArea10kindCd8.equals("1")) areaList.add(FundDataSearch.InvestingArea.getInstance("9").getName());
			if (resultInfo.investArea10kindCd10.equals("1")) areaList.add(FundDataSearch.InvestingArea.getInstance("10").getName());
			
			investingArea = String.join(" ", areaList);
		}
		
		String investingAsset = FundDataSearch.InvestingAsset.getInstance(resultInfo.investAssetKindCd).getName();
		String indexFundType  = FundDataSearch.IndexFundType.getInstance(resultInfo.supplementKindCd).getName();
		String settlementDate = resultInfo.setlDate.replace(",", "");
		
		FundInfoJP fund = new FundInfoJP(
				isinCode, fundCode, stockCode, listingDate, redemptionDate, divFreq,
				expenseRatio, buyFreeMax,
				fundType, investingArea, investingAsset, indexFundType, settlementDate,
				name
				);
		return fund;
	}
}
