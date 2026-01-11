package yokwe.finance.data.provider.click;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.data.fund.jp.StorageJP;
import yokwe.finance.data.type.TradingFund;
import yokwe.util.Makefile;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;
import yokwe.util.update.UpdateBase;

public class UpdateTradingFundJP extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input(). // StorageJITA.FundInfo
		output(StorageClick.TradingFundJP).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		downloadFile();
		updateFile();
	}
	
	void downloadFile() {
		var fundList = new ArrayList<FundList>();
		
		int maxPage = 99;
		for(int page = 0; page <= maxPage; page++) {
			logger.info("page  {}", page);
			var url = getURL(page);
			var string = HttpUtil.getInstance().downloadString(url);
			
			int posA = string.indexOf("{");
			int posB = string.indexOf(");");
			var jsonString = string.substring(posA, posB).replace("\n\n,", "");
			var data = JSON.unmarshal(Data.class, jsonString);
			for(var e: data.fundList) {
				fundList.add(e);
			}
			
			if (page == 0) {
				maxPage = Integer.valueOf(data.hitCount) / 100;
				logger.info("maxPage  {}", maxPage);
			}
		}
		logger.info("save  {}  {}", fundList.size(), StorageClick.FundListJSON.getFile());
		StorageClick.FundListJSON.save(fundList);
	}
	String getURL(int beforeGoNo) {
		var beforeGo = beforeGoNo == 0 ? "" : "BEFORE=" + String.valueOf(beforeGoNo * 100) + "&GO_BEFORE=&";
		
		var time = String.valueOf(System.currentTimeMillis());
		String url =
			"https://ot36.qhit.net/gmo-clsec/qsearch.exe?" + 
			"callback=callFunds&F=fund%2Ffund_list&" +
			"KEY1=&KEY3=&KEY5=&KEY6=&KEY7=&KEY8=&KEY9=&KEY10=&KEY11=&KEY12=&KEY13=&KEY14=&KEY15=&KEY16=&KEY17=&KEY18=0&KEY19=&KEY20=&KEY21=&KEY22=&" +
			"REFINDEX=-C%E7%B4%AF%E7%A9%8D%E3%83%AA%E3%82%BF%E3%83%BC%E3%83%B33Y&" +
			"MAXDISP=10000&" +
			beforeGo +
			"_=" + time;;
		return url;
	}
	
	void updateFile() {
		var list = new ArrayList<TradingFund>();
		
		var fundMap = StorageJP.FundInfo.getList().stream().collect(Collectors.toMap(o -> o.isinCode, Function.identity()));
		
		var fundList = StorageClick.FundListJSON.load();
		logger.info("fundList  {}", fundList.size());
		for(var e: fundList) {
			var isinCode = e.isinCode;
			var salesFee = BigDecimal.ZERO;
			String name;
			if (fundMap.containsKey(isinCode)) {
				name = fundMap.get(isinCode).name;
			} else {
				logger.error("Unexpected isinCode");
				logger.error("  fund  {}", e.toString());
				throw new UnexpectedException("Unexpected isinCode");
			}
			
			list.add(new TradingFund(isinCode, salesFee, name));
		}
		
		logger.info("save  {}  {}", list.size(), StorageClick.TradingFundJP.getFile());
		StorageClick.TradingFundJP.save(list);
	}
	
	public static class FundList implements Comparable<FundList> {
		@JSON.Optional String isinCode;
		@JSON.Optional String fundname;
		@JSON.Optional String area;
		@JSON.Optional String assets;
		@JSON.Optional String kagaku;
		@JSON.Optional String width;
		@JSON.Optional String ratio;
		@JSON.Optional String netassets;
		@JSON.Optional String return1m;
		@JSON.Optional String return3m;
		@JSON.Optional String return6m;
		@JSON.Optional String return1y;
		@JSON.Optional String return3y;
		@JSON.Optional String return5y;
		@JSON.Optional String netassetsio;
		@JSON.Optional String commission;
		@JSON.Optional String mgtfee;
		@JSON.Optional String nisaSaving;
		@JSON.Optional String nisaGrowth;
		
		@Override
		public int compareTo(FundList that) {
			return this.isinCode.compareTo(that.isinCode);
		}
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}

	public static class Data {
		String         state;
		String         hitCount;
		FundList[] fundList;
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}

}