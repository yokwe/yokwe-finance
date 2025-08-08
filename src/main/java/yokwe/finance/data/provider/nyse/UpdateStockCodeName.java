package yokwe.finance.data.provider.nyse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import yokwe.finance.data.type.StockCodeNameUS;
import yokwe.finance.data.type.StockInfoUS.Market;
import yokwe.finance.data.type.StockInfoUS.Type;
import yokwe.finance.tool.Makefile;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;
import yokwe.util.update.UpdateBase;

public class UpdateStockCodeName extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input().
		output(StorageNYSE.StockCodeName).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		var stockList = downloadFilter(TYPE_STOCK);
		var etfList   = downloadFilter(TYPE_ETF);
		logger.info("stock  {}", stockList.size());
		logger.info("etf    {}", etfList.size());
		
		// as of 2025-08-04 field url, normalizedTicker, instrumentName and symbolExchangeTicker has not null value in Filter
		var map = new HashMap<String, StockCodeNameUS>();
		
		for(var e: stockList) {
			String stockCode = e.normalizedTicker.replace("p", "-").replace(".CL", "").replace("w", "");
			//                                            preferred         called             when issue
			Market market    = Market.NYSE;
			Type   type      = Type.COMMON;
			String name      = e.instrumentName.replace(",", "").replace(".", "").toUpperCase(); // use upper case
			
			var stockCodeName = new StockCodeNameUS(stockCode, market, type, name);
			
			if (map.containsKey(stockCode)) {
				var old = map.get(stockCode);
				if (old.name.length() < name.length()) {
					map.put(stockCode, stockCodeName);
					logger.info("stock  {}  {}  ->  {}", stockCode, old.name, name);
				}
			} else {
				map.put(stockCode, stockCodeName);
			}
		}
		for(var e: etfList) {
			String stockCode = e.normalizedTicker.replace("p", "-").replace(".CL", "").replace("w", "");
			//                                            preferred         called             when issue
			Market market    = Market.NYSE;
			Type   type      = Type.ETF;
			String name      = e.instrumentName.replace(",", "").replace(".", "").toUpperCase(); // use upper case
			
			var stockCodeName = new StockCodeNameUS(stockCode, market, type, name);
			
			if (map.containsKey(stockCode)) {
				var old = map.get(stockCode);
				if (old.name.length() < name.length()) {
					map.put(stockCode, stockCodeName);
					logger.info("etf    {}  {}", stockCode, name);
				}
			} else {
				map.put(stockCode, stockCodeName);
			}
		}
		var list = new ArrayList<StockCodeNameUS>(map.values());
		
		logger.info("list  {}", list.size());
		list.removeIf(o -> o.stockCode.endsWith(".U")); // remove unit
		logger.info("list  {}  after remove ends with .U", list.size());
		list.removeIf(o -> o.stockCode.contains(".W")); // remove warrant
		logger.info("list  {}  after remove contains .W", list.size());

		// sanity check
		checkDuplicateKey(list, o -> o.stockCode);
		
		save(list, StorageNYSE.StockCodeName);  // use save for make
	}
	private static final String TYPE_STOCK = "EQUITY";
	private static final String TYPE_ETF   = "EXCHANGE_TRADED_FUND";
	
	private List<Filter> downloadFilter(String instrumentType) {
		var body = String.format(BODY_FORMAT, instrumentType);
		
		var string = HttpUtil.getInstance().withPost(body, CONTENT_TYPE).downloadString(URL);
		var list = JSON.getList(Filter.class, string);
		
		// remove "," from instrumentName
		for(var e: list) e.instrumentName = e.instrumentName;
		
		return list;
	}
	private static final String URL          = "https://www.nyse.com/api/quotes/filter";
	private static final String BODY_FORMAT  = "{\"instrumentType\":\"%s\",\"pageNumber\":1,\"sortColumn\":\"NORMALIZED_TICKER\",\"sortOrder\":\"ASC\",\"maxResultsPerPage\":10000,\"filterToken\":\"\"}";
	private static final String CONTENT_TYPE = "application/json";
}
