package yokwe.finance.data.provider.rakuten;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.data.type.CodeName;
import yokwe.finance.data.type.TradingStock;
import yokwe.finance.data.type.TradingStock.TradeType;
import yokwe.util.CSVUtil;
import yokwe.util.CSVUtil.ColumnName;
import yokwe.util.ScrapeUtil;
import yokwe.util.Storage;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.makefile.Makefile;
import yokwe.util.update.UpdateBase;

public class UpdateTradingStockUS extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input().
		output(StorageRakuten.TradingStockUS).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		Storage.initialize();
		
		var listETF   = getETFList();
		var listStock = getStockList();
		logger.info("etf        {}", listETF.size());
		logger.info("stock      {}", listStock.size());

		var map = new HashMap<String, CodeName>();
		add(map, listETF);
		add(map, listStock);
		logger.info("map        {}", map.size());

		// fix buyFree and name in list
		var buyFreeSet = getBuyFreeSet();
		logger.info("buyFree    {}", buyFreeSet.size());
		
		// build list
		var list = new ArrayList<TradingStock>();
		for(var codeName: map.values()) {
			var code      = codeName.code;
			var feeType   = buyFreeSet.contains(code) ? TradingStock.FeeType.BUY_FREE : TradingStock.FeeType.PAID;
			var tradeType = TradeType.BUY_SELL;
			var name      = codeName.name.replace(",", "").toUpperCase();

			list.add(new TradingStock(code, feeType, tradeType, name));
		}
		
		checkDuplicateKey(list, o -> o.stockCode);
		save(list, StorageRakuten.TradingStockUS); // use save for make
	}
	private void add(Map<String, CodeName> map, List<CodeName> list) {
		for(var e: list) {
			var key = e.code;
			if (map.containsKey(key)) {
				var old = map.get(key);
				if (old.name.length() < e.name.length()) {
					map.put(key, e);
				}
			} else {
				map.put(key, e);
			}
		}

	}
	private Set<String> getBuyFreeSet() {
		var url    = "https://www.rakuten-sec.co.jp/web/foreign/etf/etf-etn-reit/lineup/0-etf.html";
		var string = HttpUtil.getInstance().downloadString(url);
		var list   = ZeroETF.getInstance(string);
		var set    = list.stream().map(o -> o.symbol).collect(Collectors.toSet());
		return set;
	}
	public static class ZeroETF {
		public static final Pattern PAT = Pattern.compile(
				"<tr>\\s+" +
				"<td class=\"ta-c va-m\" rowspan=\"2\"><a .+?>(?<symbol>.+?)</a>.*?</td>\\s+" +
				"<td>.+?</td>\\s+" +
				"<td class=\"ta-c\">(?<exchange>.+?)</td>\\s+" +
				"<td class=\"ta-c\">(?<expenseRatio>.+?)</td>\\s+" +
				"</tr>"
		);
		public static List<ZeroETF> getInstance(String page) {
			return ScrapeUtil.getList(ZeroETF.class, PAT, page);
		}
		
		public final String symbol;
		public final String exchange;
		public final String expenseRatio;
		
		public ZeroETF(String symbol, String exchange, String expenseRatio) {
			this.symbol       = symbol;
			this.exchange     = exchange;
			this.expenseRatio = expenseRatio;
		}
		
		@Override
		public String toString() {
			return String.format("%s %s %s", symbol, exchange, expenseRatio);
		}
	}
	
	private List<CodeName> getETFList() {
		var url      = "https://www.rakuten-sec.co.jp/web/market/search/etf_search/ETFD.csv";
		var string   = HttpUtil.getInstance().downloadString(url);
		StorageRakuten.ETFD.write(string);
		
		var dataList = CSVUtil.read(ETFData.class).withHeader(false).file(new StringReader(string));
		
		var list = new ArrayList<CodeName>();
		for(var data: dataList) {
			String code = data.symbol;
			String name = data.name;
			
			// sanity check
			if (code.isEmpty()) continue;
			if (exchangeMap.containsKey(data.exchange)) {
				var skip = exchangeMap.get(data.exchange);
				if (skip) continue;
			} else {
				logger.error("Unpexpected exchangeJP");
				logger.error("  etf {}", ToString.withFieldName(data));
				throw new UnexpectedException("Unexpected");
			}
			
			list.add(new CodeName(code, name));
		}
		
		return list;
	}
	public static class ETFData {
		public String f01;
		public String symbol;
		public String name;
		public String exchange;
		public String f05;
		public String f06;
		public String f07;
		public String f08;
		public String f09;
		
		public String f10;
		public String f11;
		public String f12;
		public String f13;
		public String f14;
		public String f15;
		public String f16;
		public String f17;
		public String f18;
		public String f19;
		
		public String f20;
		public String f21;
		public String f22;
		public String f23;
		public String f24;
		public String f25;
		public String f26;
		public String f27;
		public String f28;
		public String f29;
		
		public String f30;
		public String f31;
		public String f32;
		public String f33;
		public String f34;
		public String f35;
		public String f36;
		public String f37;
		public String f38;
		public String f39;
		
		public String f40;
		public String f41;
		public String f42;
		public String f43;
		public String f44;
		public String f45;
		public String f46;
		public String f47;
		public String f48;
		public String f49;
		
		public String f50;
		public String f51;
		public String f52;
		public String f53;
		public String f54;
		public String f55;
		public String f56;
		public String f57;
		public String f58;			
	}
	private static Map<String, Boolean> exchangeMap = Map.ofEntries(
		Map.entry("香港",      Boolean.TRUE),
		Map.entry("名証ETF",   Boolean.TRUE),
		Map.entry("東証ETF",   Boolean.TRUE),
		Map.entry("ｼﾝｶﾞﾎﾟｰﾙ",  Boolean.TRUE),
		Map.entry("ﾅｽﾀﾞｯｸ",    Boolean.FALSE),
		Map.entry("NYSE ARCA", Boolean.FALSE),
		Map.entry("Cboe",      Boolean.FALSE)
	);
	
	private List<CodeName> getStockList() {
		var url      = "https://www.trkd-asia.com/rakutensec/exportcsvus?all=on&vall=on&r1=on&forwarding=na&target=0&theme=na&returns=na&head_office=na&name=&sector=na&pageNo=&c=us&p=result";
		var string   = HttpUtil.getInstance().downloadString(url);
		StorageRakuten.ExportCSVUS.write(string);
		
		var dataList = CSVUtil.read(StockData.class).file(new StringReader(string));
		
		var list = new ArrayList<CodeName>();
		for(var data: dataList) {
			String code = data.ticker;
			String name = data.name;
			
			// sanity check
			if (code.isEmpty()) continue;
			if (tradeableMap.containsKey(data.tradeable)) {
				var skip = tradeableMap.get(data.tradeable);
				if (skip) continue;
			} else {
				logger.error("Unpexpected tradeable");
				logger.error("  etf {}", ToString.withFieldName(data));
				throw new UnexpectedException("tradeable");
			}
			
			list.add(new CodeName(code, name));
		}
		
		return list;
	}
	// 現地コード,銘柄名(English),銘柄名,市場,業種,取扱
	public static class StockData {
		@ColumnName("現地コード")
		public String ticker;
		@ColumnName("銘柄名(English)")
		public String name;
		@ColumnName("銘柄名")
		public String nameJP;
		@ColumnName("市場")
		public String exchange;
		@ColumnName("業種")
		public String industry;
		@ColumnName("取扱")
		public String tradeable;
	}
	
	private static Map<String, Boolean> tradeableMap = Map.ofEntries(
		Map.entry("○", Boolean.FALSE),
		Map.entry("-",  Boolean.TRUE)
	);
}
