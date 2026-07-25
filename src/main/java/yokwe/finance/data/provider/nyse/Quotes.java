package yokwe.finance.data.provider.nyse;

import java.time.LocalDate;

import yokwe.util.ToString;
import yokwe.util.json.JSON.Ignore;

public final class Quotes implements Comparable<Quotes> {
	public static class Quote {
	    public String exchg;          // "ARCX"
	    public String dispname;       // "SJNK"
	    public String desc;           // "STATE STREET SPDR BLOOMBERG SHORT TERM HIGH YIELD BOND ETF"
	    public String last;           // "24.91"
	    public String change;         // "-0.02"
	    public String pctchg;         // "-0.08022"
	    public String volume;         // "1538151"
	    public String time;           // "Friday July 10, 2026 08:00:00 PM ET",
	    public String low;            // "24.910000"
	    public String high;           // "24.950000"
	    public String open;           // "24.950000"
	    public String aveVol;         // "2283625"
	    public String annLow;         // "24.720000"
	    public String annHigh;        // "25.650000"
	    public String tradeSize;      // "37664"
	    public String bid;            // "24.830000"
	    public String ask;            // "25.400000"
	    public String bidSize;        // "1000"
	    public String askSize;        // "700"
	    public String prev;           // "24.930000"
	    public String cusip;          // "78468R408"
	    public String lastUpdateTime; // "00:00:00"
	    public String wl52date;       // "Friday March 27, 2026",
	    public String wh52date;       // "Tuesday September 23, 2025",
	    public String dividend;       // "0.147200"
	    public String divDate;        // "Wednesday July 01, 2026",
	    public String divYield;       // "7.02529"
	    public String divInt;         // "30"
	    public String beta;           // "0.2847"
	    public String eps;            // null

		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}

	public static class BoardMember {
		// IGNORE
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	public static class Trends {
		// IGNORE
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	public static class Options {
		// IGNORE
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}

	public static class TotalReturns {
	    public String oneMonth;     // "-0.04"
	    public String threeMonth;   // "-0.756"
	    public String sixMonth;     // "-1.966"
	    public String fiftyTwoWeek; // "-1.618"
	    public String threeYear;    // "0.609"
	    public String volatility;   // ".039000"
	    public String rsi;          // "44.70347"
	    public String symbolType;   // "ETF"
	    public String futureExDate; // ""

		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}

	public static class QuoteHistory {
		public static class History {
		      public String date;         // "2021/07/14"
		      public String open;         // "27.5"
		      public String high;         // "27.51"
		      public String low;          // "27.48"
		      public String close;        // "27.48"
		      public String volume;       // "3018125"
		      public String openInterest; // "0"

		      LocalDate toLocalDate() {
		    	  return LocalDate.parse(date.replaceAll("/", "-"));
		      }
			@Override
			public String toString() {
				return ToString.withFieldName(this);
			}
		}

		public String symbol;
		public History[] historyList;

		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}

	public static class ErrorMessage {
		// IGNORE
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}


	public Quote quote;

	@Ignore
	public BoardMember boardMember;

	@Ignore
	public Trends trends;
	@Ignore
	public Options options;

	public TotalReturns totalReturns;

	public QuoteHistory quoteHistory;

	@Ignore
	public ErrorMessage errorMessages;

	@Override
	public int compareTo(Quotes that) {
		return this.quote.dispname.compareTo(that.quote.dispname);
	}

	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}
