package yokwe.finance.data.provider.webull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.ToString;

/*

open https://www.webull.co.jp/search with firefox and press page down key and reach to bottom of page
select text in stock list and show menu using right click and select "調査" from menu
select table element '<table class="wbapp135 wbapp6">' and using right click and select "コピー" and "outerHTML"
in terminal window, enter following command  pbpaste -Prefer txt >tmp/a" ; tidy -i -utf8 tmp/a >tmp/a.html

 */
public class T001 {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public void main(String[] args) {
		logger.info("START");

		var page = FileUtil.read().file(HTML_FILE_PATH);
		logger.info("page  {}", page.length());

		var list = new ArrayList<StockInfo>();

		var htmlList = StockInfoHTML.getInstance(page);
		logger.info("htmlList  {}", htmlList.size());


		for(var e: htmlList) {
			var code = e.code;
			// replace space and new line
			code = code.replace("\n", "");
			code = code.replaceAll(" +", " ");

			var name = e.name;
			// replace space and new line
			name = name.trim();
			name = name.replace("\n", "");
			name = name.replaceAll(" +", " ");
			// replace html entity
			name = name.replace("&lt;", "<");
			name = name.replace("&gt;", ">");
			name = name.replace("&amp;", "&");

			list.add(new StockInfo(code, name, e.exchange));
		}

		StorageWebull.StockInfoWebull.save(list);

		logger.info("STOP");
	}

	static String HTML_FILE_PATH = "tmp/a.html";

	protected static class StockInfoHTML {
		public static final Pattern PAT = Pattern.compile(
				"<tr class=\"wbapp130\">\\s+" +
				"<td class=\"wbapp133 body\">.+?<span>(?<code>.+?)</span></td>\\s+" +
				"<td class=\"wbapp133 body\">(?<name>.+?)</td>\\s+" +
				"<td class=\"wbapp133 body\">(?<exchange>.+?)</td>\\s+" +
				"</tr>",
				Pattern.DOTALL
		);
		public static List<StockInfoHTML> getInstance(String page) {
			return ScrapeUtil.getList(StockInfoHTML.class, PAT, page);
		}

		public String code;
		public String name;
		public String exchange;

		public StockInfoHTML(String code, String name, String exchange) {
			this.code     = code;
			this.name     = name;
			this.exchange = exchange;
		}

		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}

}
