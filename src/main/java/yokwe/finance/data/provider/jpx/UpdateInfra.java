package yokwe.finance.data.provider.jpx;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import yokwe.finance.data.type.CodeName;
import yokwe.finance.data.type.StockCodeJP;
import yokwe.finance.tool.Makefile;
import yokwe.util.ScrapeUtil;
import yokwe.util.ToString;
import yokwe.util.http.HttpUtil;
import yokwe.util.update.UpdateBase;

public class UpdateInfra extends UpdateBase {
//	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input().
		output(StorageJPX.Infra).
		build();
		
	public static void main(String[] args) {
		callUpdate();
	}
	
	// Current
	// https://www.jpx.co.jp/equities/products/infrastructure/issues/index.html

	@Override
	public void update() {
		var url  = "https://www.jpx.co.jp/equities/products/infrastructure/issues/index.html";
		var page = HttpUtil.getInstance().downloadString(url);
		
		List<CodeName> list = new ArrayList<>();
		for(var e: InfraInfo.getInstance(page)) {
			var code = StockCodeJP.toStockCode5(e.stockCode);
			var name = StockCodeName.getName(code, e.name);
			list.add(new CodeName(code, name));
		}
		
		// sanicy check
		checkDuplicateKey(list, o -> o.code);
		save(list, StorageJPX.Infra); // use save for make
	}
	public static class InfraInfo {
		public static final Pattern PAT = Pattern.compile(
				"<tr>\\s+" +
				"<td class=\"a-center w-space.+?</td>\\s+" +
				"<td class=\"a-left.+?<a .+?>(?<name>.+?)</a>\\s+</td>\\s+" +
				"<td class=\"a-center.+?<a .+?>(?<stockCode>.+?)<br.+?</a></td>\\s+" +
				"",
				Pattern.DOTALL
		);
		public static List<InfraInfo> getInstance(String page) {
			return ScrapeUtil.getList(InfraInfo.class, PAT, page);
		}
		
		public String stockCode;
		public String name;
		
		public InfraInfo(String stockCode, String name) {
			this.stockCode = stockCode;
			this.name      = name;
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
}
