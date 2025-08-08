package yokwe.finance.data.provider.jreit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import yokwe.finance.data.provider.jpx.StockCodeName;
import yokwe.finance.data.provider.jpx.StorageJPX;
import yokwe.finance.data.type.StockCodeJP;
import yokwe.finance.data.type.StockInfoJP;
import yokwe.finance.tool.Makefile;
import yokwe.util.ScrapeUtil;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.update.UpdateBase;

public class UpdateJREITInfo extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input(StorageJPX.StockCodeName).
		output(StorageJREIT.JREITInfo).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		var list = new ArrayList<JREITInfo>();
		
		var stockList = StorageJPX.StockCodeName.getList().stream().filter(o -> o.type.isREIT() || o.type.isInfra()).toList();
		logger.info("stockList  {}", stockList.size());
		
		var categoryMap = getCategoryMap(stockList);
		
		for(var stock: stockList) {
			var stockCode = stock.stockCode;
			
			if (stockCode.equals("401A0")) continue; // FIXME skip 401A0 霞ヶ関リートアドバイザーズ株式会社. But it should be 401A

			var type = stock.type;
			String urlFormat = urlFormatMap.get(type);
			if (urlFormat == null) continue;
			
			var url       = String.format(urlFormat, StockCodeJP.toStockCode4(stockCode));
			var category  = categoryMap.get(stockCode);
			if (category == null) {
				logger.warn("Unknown stockCode {}  {}", stock.stockCode, stock.name);
				// add dummy data
				var listingDate = LocalDate.of(2099, 1, 1);
				var divFreq     = 2;
				list.add(new JREITInfo(stockCode, listingDate, divFreq, "*UNKNOWN*", stock.name));
				continue;
			}
			var name      = stock.name;
			
			list.add(getREIT(url, stockCode, category, name));
		}
		
		checkDuplicateKey(list, o -> o.stockCode);
		save(list, StorageJREIT.JREITInfo); // use save for make
	}
	//
	private Map<String, String> getCategoryMap(List<StockCodeName> stockList) {
		//      stockCode
		//              category
		var map = new HashMap<String, String>();
		
		// add category of reit
		{
			var url  = "https://www.japan-reit.com/list/link/";
			var page = HttpUtil.getInstance().downloadString(url);
			
			var list = Link.getInstance(page);
			// sanity check
			if (list == null) throw new UnexpectedException("list is null");
			if (list.isEmpty()) throw new UnexpectedException("list is empty");
			
			for(var e: list) {
				if (e.stockCode.equals("4011")) continue; // FIXME Use 4011 for 霞ヶ関リートアドバイザーズ株式会社. But it should be 401A
				map.put(StockCodeJP.toStockCode5(e.stockCode), e.category);
			}
		}
		
		// fix map with stockList
		for(var e: stockList) {
			if (e.type.isInfra()) {
				map.put(e.stockCode, JREITInfo.CATEGORY_INFRA);
			}
			if (e.type.isREIT()) {
				if (map.containsKey(e.stockCode)) continue;
				map.put(e.stockCode, "*UNKNOWN*");
			}
		}
		
		return map;
	}
	//
	static class Link {
		//<tr>
		//<td><a href="/meigara/3279/">3279</a></td>
		//<td><a href="http://www.activia-reit.co.jp/" target="_blank">アクティビア・プロパティーズ投資法人</a><!--アクティビア・プロパティーズ投資法人--></td>
		//<td><a href="http://www.tokyu-trm.co.jp/" target="_blank">東急不動産リート・マネジメント投信株式会社</a><!--東急不動産リート・マネジメント投信株式会社--></td>
		//<td><a href="http://www.activia-reit.co.jp/index.html" target="_blank">データシート</a></td>
		//<td>複合型（オフィス＋都市型商業施設）</td>
		//</tr>
		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td><a .+>(?<stockCode>.+)</a></td>\\s+" +
			"<td><a .+>(?<reitName>.+)</a>.*</td>\\s+" +
			"<td><a .+>(?<managementName>.+)</a>.*</td>\\s+" +
			"<td><a .+>データシート</a></td>\\s+" +
			"<td>(?<category>.+)</td>\\s+" +
			"</tr>"
		);
		public static List<Link> getInstance(String page) {
			return ScrapeUtil.getList(Link.class, PAT, page);
		}
		
		public final String stockCode;
		public final String reitName;
		public final String managementName;
		public final String category;
		
		public Link(String stockCode, String reitName, String managementName, String category) {
			this.stockCode      = stockCode;
			this.reitName       = reitName;
			this.managementName = managementName;
			this.category       = category;
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	//
	private static Map<StockInfoJP.Type, String> urlFormatMap = Map.ofEntries(
		Map.entry(StockInfoJP.Type.REIT,  "https://www.japan-reit.com/meigara/%s/info/"),
		Map.entry(StockInfoJP.Type.INFRA, "https://www.japan-reit.com/infra/%s/info/")
	);
	//
	private static JREITInfo getREIT(String url, String stockCode, String category, String name) {
		var page = HttpUtil.getInstance().downloadString(url);
		
		LocalDate listingDate = ListingDate.getInstance(page).getLocalDate();
		int       divFreq     = Settlement.getInstance(page).getSize();

		return new JREITInfo(stockCode, listingDate, divFreq, category, name);
	}
	// 名称
	static class Name {
		public static final Pattern PAT = Pattern.compile(
			//  <tr>
			//    <th scope="col">名称</th>
			//    <td colspan="3" scope="col">日本ビルファンド投資法人</td>
			//  </tr>
			"<tr>\\s+" +
			"<th .+?>名称</th>\\s+" +
			"<td .+?>(?<value>.+?)</td>\\s+" +
			"</tr>"
		);
		public static Name getInstance(String page) {
			return ScrapeUtil.get(Name.class, PAT, page);
		}

		public final String value;
		
		public Name(String value) {
			this.value = value.trim();
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	// 上場日
	static class ListingDate {
		public static final Pattern PAT = Pattern.compile(
			// <th>上場日</th>
		    // <td>2002/06/12</td>
			"<th>上場日</th>\\s*" +
			"<td>(?<yyyy>20[0-9]{1,2})/(?<mm>[0-9]{1,2})/(?<dd>[0-9]{1,2})</td>"
		);
		public static ListingDate getInstance(String page) {
			return ScrapeUtil.get(ListingDate.class, PAT, page);
		}

		public final int yyyy;
		public final int mm;
		public final int dd;
		
		public ListingDate(int yyyy, int mm, int dd) {
			this.yyyy = yyyy;
			this.mm   = mm;
			this.dd   = dd;
		}
		
		public LocalDate getLocalDate() {
			return LocalDate.of(yyyy, mm, dd);
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	// 決算月
	static class Settlement {
		public static final Pattern PAT = Pattern.compile(
			// <th>決算月</th>
		    // <td>5月/11月</td>
			"<th>決算月</th>\\s+" +
			"<td>(?<value>.+?)</td>"
		);
		public static Settlement getInstance(String page) {
			return ScrapeUtil.get(Settlement.class, PAT, page);
		}

		public final String value;
		
		public Settlement(String value) {
			this.value = value.trim();
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
		
		public int getSize() {
			String[] token = value.split("/");
			return token.length;
		}

		public List<Integer> getList() {
			List<Integer> ret = new ArrayList<>();
			
			String[] token = value.split("/");
			for(var e: token) {
				String string = e.replace("月", "").trim();
				ret.add(Integer.parseInt(string));
			}
			return ret;
		}
	}
}
