package yokwe.finance.data.provider.nasdaq.api;

import yokwe.util.ToString;

public class Dividends {
	// https://api.nasdaq.com/api/quote/YYY/dividends?assetclass=etf
	// https://api.nasdaq.com/api/quote/LMT/dividends?assetclass=stocks
	
	public static String getURL(String symbol, AssetClass assetClass, int limit) {
		return String.format("https://api.nasdaq.com/api/quote/%s/dividends?assetclass=%s&limit=%d",
				API.encodeSymbolForURL(symbol), assetClass, limit);
	}
	public static String getURL(String symbol, AssetClass assetClass) {
		return String.format("https://api.nasdaq.com/api/quote/%s/dividends?assetclass=%s",
				API.encodeSymbolForURL(symbol), assetClass);
	}

	public static Dividends getInstance(String symbol, AssetClass assetClass, int limit) {
		String url  = getURL(symbol, assetClass, limit);
		return API.getInstance(Dividends.class, url);
	}
	public static Dividends getInstance(String symbol, AssetClass assetClass) {
		String url  = getURL(symbol, assetClass);
		return API.getInstance(Dividends.class, url);
	}

	
	public static class Values {
		// {"exOrEffDate":"05/26/2022","type":"CASH","amount":"$0.12","declarationDate":"05/25/2022","recordDate":"05/27/2022","paymentDate":"05/31/2022","currency":"USD"}
		public String exOrEffDate;
		public String type;
		public String amount;
		public String declarationDate;
		public String recordDate;
		public String paymentDate;
		public String currency;
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
	public static class Header {
		public String exOrEffDate;
		public String type;
		public String amount;
		public String declarationDate;
		public String recordDate;
		public String paymentDate;
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}


	public static class Table {
		public String   asOf;
		public Header   headers;
		public Values[] rows;
        
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	public static class Data {
//		      "annualizedDividend" : "11.2",
//		      "dividendPaymentDate" : "12/27/2021",
//		      "exDividendDate" : "11/30/2021",
//		      "payoutRatio" : "15.75",
//		      "yield" : "3.03%"

		public LabelValue[] dividendHeaderValues;
		public String       annualizedDividend;
		public String       dividendPaymentDate;
		public String       exDividendDate;
		public String       payoutRatio;
		public String       yield;
		public Table        dividends;
        
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
	public Data   data;
	public String message;
	public Status status;
	
	public Dividends() {
		data    = null;
		message = null;
		status  = null;
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}