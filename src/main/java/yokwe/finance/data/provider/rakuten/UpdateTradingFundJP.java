package yokwe.finance.data.provider.rakuten;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.jita.StorageJITA;
import yokwe.finance.data.type.TradingFund;
import yokwe.util.Makefile;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;
import yokwe.util.json.JSON.Ignore;
import yokwe.util.json.JSON.Name;
import yokwe.util.update.UpdateBase;

public class UpdateTradingFundJP extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
//		input(StorageJITA.FundInfo).
		output(StorageRakuten.TradingFundJP).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		var postBody = getPostBody();
		var string   = HttpUtil.getInstance().withPost(postBody, CONTENT_TYPE).downloadString(URL);
		StorageRakuten.ReloadScreener.write(string);		
		
		var data = JSON.unmarshal(ReloadScreener.class, string);
		var list = toTradingFund(data);
		
		checkDuplicateKey(list, o -> o.isinCode);
		save(list, StorageRakuten.TradingFundJP); // use save for make
	}
	private static String getPostBody() {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("result", "ファンド名称,actual_charge");
		map.put("pg", "0");
		map.put("count", "9999");
		map.put("sortnull", "コード=up");
		
		String string = map.entrySet().stream().map(o -> o.getKey() + "=" + o.getValue()).collect(Collectors.joining("&"));
		return String.format("query=%s", URLEncoder.encode(string, StandardCharsets.UTF_8));
	}
	private static final String  URL             = "https://www.rakuten-sec.co.jp/web/fund/scr/find/search/reloadscreener.asp";
	private static final String  CONTENT_TYPE    = "application/x-www-form-urlencoded;charset=UTF-8";
	
	
	private List<TradingFund> toTradingFund(ReloadScreener reloadScreener) {
		var set = StorageJITA.FundInfo.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
		
		List<TradingFund> list = new ArrayList<>();
		for(int i = 0; i < reloadScreener.data.length; i++) {
			String[] data = reloadScreener.data[i];
			
			var isinCode = data[0];
			var name     = data[1].replace("&amp;", "&");
			
			// sanity check
			if (set.contains(isinCode)) {
				list.add(new TradingFund(isinCode, BigDecimal.ZERO, name));
			} else {
				logger.warn("Bogus isinCode  {}  {}", isinCode, name);
			}
		}
		return list;
	}

	public static class ReloadScreener {
		public static class BunruiMapping {
			//
		}
		public static class PageInfo {
			@Name("StartRecord")     public String        startRecord;
			@Name("EndRecord")       public String        endRecord;
			
			@Name("NbrPagesTotal")   public String        pagesTotal;
			@Name("NbrRecsSelected") public String        recordsSelected;
			@Name("NbrRecsTotal")    public String        recordsTotal;
			@Name("PageSelected")    public String        pageSelected;
			@Name("RecordsPerPage")  public String        recordsPerPage;
		}
		public static class ResultCounts {
			//
		}
		
		@Name("BunruiMapping")   @Ignore public BunruiMapping bunruiMapping;
		@Name("BunruiText")      @Ignore public String        bunruiText;
		@Name("Conditions")      @Ignore public String        conditions;
		@Name("Data")                    public String[][]    data;
		@Name("Headers")                 public String[]      headers;
		@Name("JSONBunruiCount") @Ignore public String        jsonBunruiCount;
		@Name("JSONCount")       @Ignore public String        jsonCount;
		@Name("PageInfo")                public PageInfo      pageInfo;
		@Name("ResultCounts")    @Ignore public ResultCounts  resultCounts;
		@Name("TotalCount")      @Ignore public String        totalCount;
		@Name("Warnings")                public String        warnings;
		
		@Override
		public String toString() {
			return String.format("{record %s - %s  %s / %s  %d}", pageInfo.startRecord, pageInfo.endRecord, pageInfo.pageSelected, pageInfo.pagesTotal, data.length);
		}
	}
}