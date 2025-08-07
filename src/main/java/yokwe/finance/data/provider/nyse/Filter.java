package yokwe.finance.data.provider.nyse;

import yokwe.util.ToString;

public final class Filter implements Comparable<Filter> {
	public String normalizedTicker;
	public String exchangeId;
	public String instrumentName;
	public String instrumentType;
	public String micCode;
//	public String normalizedTicker;
	public String symbolEsignalTicker;
	public String symbolExchangeTicker;
	public String symbolTicker;
	public int    total;
	public String url;
	
	@Override
	public int compareTo(Filter that) {
		return this.normalizedTicker.compareTo(that.normalizedTicker);
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}
