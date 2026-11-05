package yokwe.finance.data.type;

import yokwe.util.UnexpectedException;

public enum Currency {
	JPY,
	USD,
	EUR,
	GBP,
	AUD,
	NZD;

	public static Currency getInstance(String string) {
		for(var e: values()) {
			if (string.equals(e.name())) {
				return e;
			}
		}
		org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
		logger.error("Unexpeced string");
		logger.error("  string  {}!", string);
		throw new UnexpectedException("Unexpeced string");
	}
}