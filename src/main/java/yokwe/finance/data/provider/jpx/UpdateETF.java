package yokwe.finance.data.provider.jpx;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import yokwe.finance.data.type.CodeName;
import yokwe.finance.data.type.StockCodeJP;
import yokwe.util.ScrapeUtil;
import yokwe.util.ToString;
import yokwe.util.http.HttpUtil;
import yokwe.util.makefile.Makefile;
import yokwe.util.update.UpdateBase;

public class UpdateETF extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input().
		output(StorageJPX.ETF).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	// Current
	// https://www.jpx.co.jp/equities/products/etfs/issues/01.html
	// https://www.jpx.co.jp/equities/products/etfs/leveraged-inverse/01.html
	
	// Delist
	// https://www.jpx.co.jp/equities/products/etfs/delisting/index.html
	
	// New
	// https://www.jpx.co.jp/equities/products/etfs/issues/index.html
	
	
	private static final String URL_NORMAL   = "https://www.jpx.co.jp/equities/products/etfs/issues/01.html";
	private static final String URL_LEVERAGE = "https://www.jpx.co.jp/equities/products/etfs/leveraged-inverse/01.html";
	
	@Override
	public void update() {
		var normalList   = getList(URL_NORMAL);
		var leverageList = getList(URL_LEVERAGE);
		
		logger.info("normal    {}", normalList.size());
		logger.info("leverage  {}", leverageList.size());
		
		var list = new ArrayList<CodeName>(normalList.size() + leverageList.size());
		list.addAll(normalList);
		list.addAll(leverageList);
		
		// sanity check
		checkDuplicateKey(list, o -> o.code);
		save(list, StorageJPX.ETF); // use save for make
	}
	private List<CodeName> getList(String url) {
		var page = HttpUtil.getInstance().downloadString(url);
		
		List<CodeName> list = new ArrayList<>();
		for(var e: ETFInfo.getInstance(page)) {
			var code = StockCodeJP.toStockCode5(e.stockCode);
			var name = StockCodeName.getName(code, e.name);
			list.add(new CodeName(code, name));
		}
		
		return list;
	}
	protected static class ETFInfo {
		public static final Pattern PAT = Pattern.compile(
				"<tr>\\s+" +
				"<td class=\"tb-color00[12]\">(?<indexName>.+?)</td>\\s+" +
				"<td class=\"a-center tb-color00[12]\">\\s+<a .+?>(?<stockCode>.+?)</a>\\s+</td>\\s+" +
				"<td class=\"tb-color00[12]\">\\s*(?<name>.+?)\\s*(?:<div>.+?</div>)?\\s*</td>\\s+" +
				"<td class=\"tb-color00[12]\">.+?</td>\\s+" +
				"<td class=\"a-right tb-color00[12] w-space\">(?<expenseRatio>[0-9]+\\.[0-9]+).+?</td>\\s+" +
				"",
				Pattern.DOTALL
		);
		public static List<ETFInfo> getInstance(String page) {
			return ScrapeUtil.getList(ETFInfo.class, PAT, page);
		}
		
		public String indexName;
		public String stockCode;
		public String name;
		public String expenseRatio;
		
		public ETFInfo(String indexName, String stockCode, String name, String expenseRatio) {
			this.indexName    = indexName;
			this.stockCode    = stockCode;
			this.name         = name;
			this.expenseRatio = expenseRatio;
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
}
