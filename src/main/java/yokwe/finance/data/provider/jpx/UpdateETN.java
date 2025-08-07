package yokwe.finance.data.provider.jpx;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import yokwe.finance.data.type.CodeName;
import yokwe.finance.data.type.StockCodeJP;
import yokwe.util.ScrapeUtil;
import yokwe.util.ToString;
import yokwe.util.http.HttpUtil;
import yokwe.util.makefile.Makefile;
import yokwe.util.update.UpdateList;

public class UpdateETN extends UpdateList<CodeName> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
		
	public static Makefile MAKEFILE = Makefile.builder().
		input().
		output(StorageJPX.ETN).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	// Current
	// https://www.jpx.co.jp/equities/products/etns/issues/01.html
	// https://www.jpx.co.jp/equities/products/etns/leveraged-inverse/01.html

	private static final String URL_NORMAL   = "https://www.jpx.co.jp/equities/products/etns/issues/01.html";
	private static final String URL_LEVERAGE = "https://www.jpx.co.jp/equities/products/etns/leveraged-inverse/01.html";
	
	@Override
	protected List<CodeName> downloadFile() {
		var normalList   = getList(URL_NORMAL);
		var leverageList = getList(URL_LEVERAGE);
		
		logger.info("normal    {}", normalList.size());
		logger.info("leverage  {}", leverageList.size());
		
		return Stream.concat(normalList.stream(), leverageList.stream()).collect(Collectors.toList());
	}
	private static List<CodeName> getList(String url) {
		var page = HttpUtil.getInstance().downloadString(url);
		
		List<CodeName> list = new ArrayList<>();
		for(var e: ETNInfo.getInstance(page)) {
			var code = StockCodeJP.toStockCode5(e.stockCode);
			var name = StockCodeName.getName(code, e.name);
			list.add(new CodeName(code, name));
		}
		
		return list;
	}
	public static class ETNInfo {
		public static final Pattern PAT = Pattern.compile(
				"<tr>\\s+" +
				"<td .+?</td>\\s+" +
				"<td class=\"tb-color00[12]\">(?<indexName>.+?)</td>\\s+" +
				"<td class=\"a-center tb-color00[12]\">\\s+<a .+?>(?<stockCode>.+?)</a>\\s+</td>\\s+" +
				"<td class=\"tb-color00[12]\">\\s*(?<name>.+?)\\s*(?:<div>.+?</div>)?\\s*</td>\\s+" +
				"<td class=\"tb-color00[12]\">.+?</td>\\s+" +
				"<td class=\"a-right tb-color00[12] w-space\">(?<expenseRatio>[0-9]+\\.[0-9]+).+?</td>\\s+" +
				"",
				Pattern.DOTALL
		);
		public static List<ETNInfo> getInstance(String page) {
			return ScrapeUtil.getList(ETNInfo.class, PAT, page);
		}
		
		public String indexName;
		public String stockCode;
		public String name;
		public String expenseRatio;
		
		public ETNInfo(String indexName, String stockCode, String name, String expenseRatio) {
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
	
	@Override
	protected void updateFile(List<CodeName> list) {
		checkDuplicateKey(list, o -> o.code);
		save(list, StorageJPX.ETN); // use save for make
	}
}
