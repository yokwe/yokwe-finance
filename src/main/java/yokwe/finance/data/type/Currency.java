package yokwe.finance.data.type;

import yokwe.util.UnexpectedException;

public enum Currency {
	JPY("日本円"),
	USD("米ドル"),
	EUR("ユーロ"),
	GBP("ポンド"),
	AUD("AUドル"),
	NZD("NZドル");
	
	public static Currency getInstance(String string) {
		for(var e: values()) {
			if (string.equals(e.description)) return e;
			if (string.equals(e.name()))      return e;
		}
		org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
		logger.error("Unexpeced string");
		logger.error("  string  {}!", string);
		throw new UnexpectedException("Unexpeced string");
	}
	
	public final String description;
	private Currency(String description) {
		this.description = description;
	}
}