package yokwe.finance.data.provider.jreit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import yokwe.finance.data.type.DailyValue;
import yokwe.finance.data.type.StockCodeJP;
import yokwe.finance.data.type.StockInfoJP;
import yokwe.finance.tool.Makefile;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.ScrapeUtil.AsNumber;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;
import yokwe.util.update.UpdateBase;

public class UpdateJREITDiv extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input(StorageJREIT.JREITInfo).
		output(StorageJREIT.JREITDiv).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		var list = StorageJREIT.JREITInfo.getList();
		
		delistUnknownFile(list);
		
		// delete files in StorageJREIT.Page
		FileUtil.deleteFile(StorageJREIT.Page.getDir(), o -> o.isFile());

		int count = 0;
		
		for(var e: list) {
			var stockCode = e.stockCode;
			if ((++count % 10) == 1) logger.info("{}  /  {}  {}", count, list.size(), stockCode);
			
			var type    = e.toType();
			var url     = String.format(urlFormatMap.get(type), StockCodeJP.toStockCode4(stockCode));
			var page    = HttpUtil.getInstance().downloadString(url);
			
			// save for later analysis
			StorageJREIT.Page.save(stockCode, page);
			
			var divList = functionMap.get(type).apply(page);
			StorageJREIT.JREITDiv.save(stockCode, divList);
		}
		// touch file
		StorageJREIT.JREITDiv.touch();
	}
	
	protected void delistUnknownFile(List<JREITInfo> list) {
		var validNameList = list.stream().map(o -> o.stockCode).toList();
		StorageJREIT.JREITDiv.delistUnknownFile(validNameList);
	}
	private static Map<StockInfoJP.Type, String> urlFormatMap = Map.ofEntries(
		Map.entry(StockInfoJP.Type.INFRA, "https://www.japan-reit.com/infra/%s/dividend/"),
		Map.entry(StockInfoJP.Type.REIT,  "https://www.japan-reit.com/meigara/%s/bunpai.json")
	);
	private static Map<StockInfoJP.Type, Function<String, List<DailyValue>>> functionMap = Map.ofEntries(
		Map.entry(StockInfoJP.Type.REIT,  new Functions.REIT()),
		Map.entry(StockInfoJP.Type.INFRA, new Functions.INFRA())
	);
	private static class Functions {
		private static class REIT implements  Function<String, List<DailyValue>> {
			@Override
			public List<DailyValue> apply(String page) {
				var ret = new ArrayList<DailyValue>();
				
				var bunpaiList = JSON.getList(Bunpai.class, page);
				if (bunpaiList == null) {
					throw new UnexpectedException("bunpaiList == null");
				}

				for(var e: bunpaiList) {
				    LocalDate  date = LocalDate.parse(e.date);
				    // sanity check
					if (e.result == null) continue; // skip if no result
					
					BigDecimal value = BigDecimal.valueOf(e.result.intValue());
					
					ret.add(new DailyValue(date, value));
				}

				return ret;
			}
		}
		private static class INFRA implements  Function<String, List<DailyValue>> {
			@Override
			public List<DailyValue> apply(String page) {
				var ret = new ArrayList<DailyValue>();
				
				for(var e: Dividend.getInstance(page)) {
				    LocalDate  date  = e.getDate();
				    // sanity check
					if (e.result.isEmpty()) continue; // skip if no result
				    
					BigDecimal value = new BigDecimal(e.result);
					
					ret.add(new DailyValue(date, value));
				}
				
				return ret;
			}
		}
	}
	
	// 分配金の推移
	static class Dividend {
		// 分配金の推移
		//	<tr>
		//	<td>16</td>
		//	<td>2023-11-30</td>
		//	<td></td>
		//	<td>0</td>
		//	<tr>

		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td>(?<term>[0-9]+)</td>\\s+" +
			"<td>(?<yyyy>20[0-9][0-9])-(?<mm>[01]?[0-9])-(?<dd>[0123]?[0-9])</td>\\s+" +
			"<td>(?<result>[0-9,]*)</td>\\s+" +
			"<td>(?<estimate>[0-9,]*)</td>\\s+" +
			"</tr>"
		);
		public static List<Dividend> getInstance(String page) {
			return ScrapeUtil.getList(Dividend.class, PAT, page);
		}
		
		public final int    term;
		public final int    yyyy;
		public final int    mm;
		public final int    dd;
		@AsNumber
		public final String result;
		@AsNumber
		public final String estimate;
		
		public Dividend(int term, int yyyy, int mm, int dd, String result, String estimate) {
			this.term     = term;
			this.yyyy     = yyyy;
			this.mm       = mm;
			this.dd       = dd;
			this.result   = result.trim();
			this.estimate = estimate.trim();
		}
		
		public LocalDate getDate() {
			return LocalDate.of(yyyy, mm, dd);
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	static class Bunpai {
		//	    "date" : "2023-02-28",
		//	    "estimate" : 3900,
		//	    "result" : null,
		//	    "term" : 42
		public String  date;
		public Integer estimate;
		public Integer result;
		public int     term;
		
		public Bunpai(String date, Integer estimage, Integer result, int term) {
			this.date     = date;
			this.estimate = estimage;
			this.result   = result;
			this.term     = term;
		}
		public Bunpai() {
			this(null, null, null, 0);
		}
				
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
}
