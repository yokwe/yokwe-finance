package yokwe.finance.data.provider.smtb;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;
import yokwe.util.ToString;

public class WebPage {
	public static class FundInfo {
		public static final Pattern PAT = Pattern.compile(
			"<a href=\"//www.smtb.jp/personal/saving/investment/fund/(?<fundCode>.+?)\" .+?><span .+?>(?<fundName>.+?)</span></a>\\s+" +
			".+?-->\\s+" +
			"<td rowspan=\"2\">\\s+<div .+?><script>omitZero\\(\"(?<initialFee>.*?)\"\\);</script></div>\\s+</td>\\s+" +
			"",
			Pattern.DOTALL
		);
		public static List<FundInfo> getInstance(String page) {
			return ScrapeUtil.getList(FundInfo.class, PAT, page);
		}
		
		public final String fundCode;
		public final String fundName;
		public final String initialFee;
		
		public FundInfo(
			String fundCode,
			String fundName,
			String initialFee
			) {
			this.fundCode      = fundCode;
			{
				int pos = fundName.indexOf("愛称");
				if (pos == -1) {
					this.fundName      = fundName;
				} else {
					this.fundName      = fundName.substring(0, pos - 1);
				}
			}
			if (initialFee.isEmpty()) {
				this.initialFee = "";
			} else {
				this.initialFee = new BigDecimal(initialFee).scaleByPowerOfTen(-2).stripTrailingZeros().toPlainString();
			}
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	public static class PageInfo {
		//<input type="hidden" value="300" id="maxdisp">
		//<input type="hidden" value="262" id="hitcount">
		//<input type="hidden" value="100" id="dispcount">
		//<input type="hidden" value="0" id="before">
		public static final Pattern PAT = Pattern.compile(
			"<input type=\"hidden\" value=\"(?<maxdisp>.+?)\" id=\"maxdisp\">\\s+" +
			"<input type=\"hidden\" value=\"(?<hitcount>.+?)\" id=\"hitcount\">\\s+" +
			"<input type=\"hidden\" value=\"(?<dispcount>.+?)\" id=\"dispcount\">\\s+" +
			"<input type=\"hidden\" value=\"(?<before>.+?)\" id=\"before\">\\s+" +
			""
		);

		public static PageInfo getInstance(String page) {
			return ScrapeUtil.get(PageInfo.class, PAT, page);
		}
		
		public final int maxdisp;
		public final int hitcount;
		public final int dispcount;
		public final int before;
		
		public PageInfo(
			int maxdisp,
			int hitcount,
			int dispcount,
			int before
			) {
			this.maxdisp   = maxdisp;
			this.hitcount  = hitcount;
			this.dispcount = dispcount;
			this.before    = before;
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
}
