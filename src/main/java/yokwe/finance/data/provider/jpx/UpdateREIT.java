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

public class UpdateREIT extends UpdateBase {
//	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input().
		output(StorageJPX.REIT).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	
	// Current
	// https://www.jpx.co.jp/equities/products/reits/issues/index.html
	
	@Override
	public void update() {
		var url  = "https://www.jpx.co.jp/equities/products/reits/issues/index.html";
		var page = HttpUtil.getInstance().downloadString(url);
		
		var list = new ArrayList<CodeName>();
		for(var e: REITInfo.getInstance(page)) {
			var code = StockCodeJP.toStockCode5(e.code);
			var name = StockCodeName.getName(code, e.name);
			list.add(new CodeName(code, name));
		}
		
		// sancy check
		checkDuplicateKey(list, o -> o.code);
		save(list, StorageJPX.REIT); // use save for make
	}
	
	public static class REITInfo {		
		public static final Pattern PAT_A = Pattern.compile(
			"" +
			"<tr>\\s+" +
			"<td class=\"a-center w-space.+?\" rowspan=\"2\">(?<date>[0-9/]{10})</td>\\s+" +
			"<td class=\"a-left.+?\"><a href=\"http.+?\" rel=\"external\">(?<name>.+?)</a>\\s+</td>\\s+" +
			"<td class=\"a-center w-space.+?\" rowspan=\"2\">\\s+<a href=\".+?\" rel=\"external\">(?<code>.+?)<br.+?</td>\\s+" +
			"",
			Pattern.DOTALL);
		public static final Pattern PAT_B = Pattern.compile(
			"" +
			"<tr>\\s+" +
			"<td class=\"a-center w-space.+?\" rowspan=\"2\">(?<date>[0-9/]{10})</td>\\s+" +
			"<td class=\"a-left.+?\">(?<name>[^<]+?)\\s+</td>\\s+" +
			"<td class=\"a-center w-space.+?\" rowspan=\"2\">\\s+<a href=\".+?\" rel=\"external\">(?<code>.+?)<br.+?</td>\\s+" +
			"",
			Pattern.DOTALL);
		
		public static List<REITInfo> getInstance(String page) {
			var list = new ArrayList<REITInfo>();
			list.addAll(ScrapeUtil.getList(REITInfo.class, PAT_A, page));
			list.addAll(ScrapeUtil.getList(REITInfo.class, PAT_B, page));
			return list;
		}
		
		public String date;
		public String name;
		public String code;
		
		public REITInfo(String date, String name, String stockCode) {
			this.date  = date;
			this.name = name;
			this.code = stockCode;
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
}
