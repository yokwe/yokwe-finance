package yokwe.finance.data.provider.yahoo;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import yokwe.finance.data.type.CompanyInfo;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class CompanyInfoYahoo {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String  URL            = "https://query1.finance.yahoo.com/v1/finance/search";
	private static final Charset CHARSET_UTF_8  = StandardCharsets.UTF_8;
	
	public static class Result {
		public static class Quote {
			@JSON.Name("symbol")                        public String     symbol;
			@JSON.Name("prevTicker")       @JSON.Ignore public String     prevTicker;
			@JSON.Name("tickerChangeDate") @JSON.Ignore public String     tickerChangeDate;
			
			@JSON.Name("quoteType")                     public String     type;
			@JSON.Name("typeDisp")       @JSON.Ignore   public String     typeDisp;
			
			@JSON.Name("exchange")                          public String exchange;
			@JSON.Name("exchDisp")                          public String exchDisp;
			@JSON.Name("prevExchange")         @JSON.Ignore public String prevExchange;
			@JSON.Name("exchangeTransferDate") @JSON.Ignore public String exchangeTransferDate;

			
			@JSON.Name("shortname")      @JSON.Optional public String     shortname;
			@JSON.Name("longname")       @JSON.Optional public String     longname;
			@JSON.Name("prevName")       @JSON.Ignore   public String     prevName;
			@JSON.Name("nameChangeDate") @JSON.Ignore   public String     nameChangeDate;

			
			@JSON.Name("index")          @JSON.Ignore   public String     index;
			@JSON.Name("score")          @JSON.Ignore   public BigDecimal score;
			@JSON.Name("isYahooFinance")                public boolean    isYahooFinance;

			@JSON.Name("sector")         @JSON.Ignore   public String     sector;         // only for EQUITY
			@JSON.Name("sectorDisp")     @JSON.Optional public String     sectorDisp;     // only for EQUITY
			@JSON.Name("industry")       @JSON.Ignore   public String     industry;       // only for EQUITY
			@JSON.Name("industryDisp")   @JSON.Optional public String     industryDisp;   // only for EQUITY
			
			@JSON.Name("newListingDate") @JSON.Ignore   public String     newListingDate; // only for EQUITY
			@JSON.Name("dispSecIndFlag") @JSON.Ignore   public String     dispSecIndFlag; // only for EQUITY
			
			// dispSecIndFlag
			@Override
			public String toString() {
				return String.format("{%s  %s  \"%s\"  \"%s\"  \"%s\"  \"%s\"  \"%s\"  %s}", symbol, type, exchange, exchDisp, longname, sector, industry, isYahooFinance);
			}
		}

		@JSON.Name("explains")                       @JSON.Ignore public String[] explains;
		@JSON.Name("count")                                       public int      count;
		@JSON.Name("quotes")                                      public Quote[]  quotes;
		@JSON.Name("news")                           @JSON.Ignore public String[] news;
		@JSON.Name("nav")                            @JSON.Ignore public String[] nav;
		@JSON.Name("lists")                          @JSON.Ignore public String[] lists;
		@JSON.Name("researchReports")                @JSON.Ignore public String[] researchReports;
		@JSON.Name("screenerFieldResults")           @JSON.Ignore public String[] screenerFieldResults;
		@JSON.Name("totalTime")                                   public int      totalTime;
		@JSON.Name("timeTakenForQuotes")             @JSON.Ignore public int      timeTakenForQuotes;
		@JSON.Name("timeTakenForNews")               @JSON.Ignore public int      timeTakenForNews;
		@JSON.Name("timeTakenForAlgowatchlist")      @JSON.Ignore public int      timeTakenForAlgowatchlist;
		@JSON.Name("timeTakenForPredefinedScreener") @JSON.Ignore public int      timeTakenForPredefinedScreener;
		@JSON.Name("timeTakenForCrunchbase")         @JSON.Ignore public int      timeTakenForCrunchbase;
		@JSON.Name("timeTakenForNav")                @JSON.Ignore public int      timeTakenForNav;
		@JSON.Name("timeTakenForResearchReports")    @JSON.Ignore public int      timeTakenForResearchReports;
		@JSON.Name("timeTakenForScreenerField")      @JSON.Ignore public int      timeTakenForScreenerField;
		@JSON.Name("timeTakenForSearchLists")        @JSON.Ignore public int      timeTakenForSearchLists;
		@JSON.Name("timeTakenForCulturalAssets")     @JSON.Ignore public int      timeTakenForCulturalAssets;
		
		@Override
		public String toString() {
			return String.format("{%d  %d  %s}", count, totalTime, (quotes == null) ? "null" : Arrays.stream(quotes).toList());
		}
	}
	
	private static String getURL(String q) {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("q",  q);
		map.put("quotesCount", "3");
		map.put("newsCount",   "0");
		map.put("listsCount",  "0");
		String queryString = map.entrySet().stream().map(o -> o.getKey() + "=" + URLEncoder.encode(o.getValue(), CHARSET_UTF_8)).collect(Collectors.joining("&"));
		
		return String.format("%s?%s", URL, queryString);
	}
	
	private static String downloadString(String key) {
		String url = getURL(key);
		return HttpUtil.getInstance().downloadString(url);
	}
	
	public static CompanyInfo getInstance(String key) {
		String string = downloadString(key);
		if (string == null) {
			logger.warn("string is null  {}", key);
			return null;
		}
		Result result = JSON.unmarshal(Result.class, string);
		if (result.quotes == null) {
			logger.warn("result.quotes is null  {}", key);
//			logger.warn("  string  {}", string);
			return null;
		}
		if (result.quotes.length == 0) {
//			logger.warn("result.quotes.length is zero  {}", key);
//			logger.warn("  string  {}", string);
			return null;
		}
		for(var e: result.quotes) {
			if (e.symbol.equals(key)) {
				if (e.sectorDisp == null) {
					logger.warn("sectorDisp is null  {}", e);
					return null;
				}
				if (e.industryDisp == null) {
					logger.warn("industryDisp is null  {}", e);
					return null;
				}
				return new CompanyInfo(e.symbol, e.sectorDisp, e.industryDisp);
			}
		}
//		logger.warn("no key in quotes  {}", key);
//		for(var e: raw.quotes) {
//			logger.warn("  quote   {}", e);
//		}
		return null;
	}

}
