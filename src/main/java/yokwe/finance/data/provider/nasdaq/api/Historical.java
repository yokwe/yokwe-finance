package yokwe.finance.data.provider.nasdaq.api;

import java.time.LocalDate;

import yokwe.util.ToString;

public class Historical {
	// https://api.nasdaq.com/api/quote/LMT/historical?assetclass=stocks&fromdate=2020-11-25&limit=9999&todate=2021-11-25
	// https://api.nasdaq.com/api/quote/YYY/historical?assetclass=etf&fromdate=2020-11-25&limit=18&todate=2021-11-25

	public static String getURL(String symbol, AssetClass assetClass, LocalDate fromDate, LocalDate toDate, long limit) {
		return String.format("https://api.nasdaq.com/api/quote/%s/historical?assetclass=%s&fromdate=%s&todate=%s&limit=%d",
				API.encodeSymbolForURL(symbol), assetClass, fromDate.toString(), toDate.toString(), limit);
	}
	
	// NOTE fromDate and toDate are inclusive
	public static Historical getInstance(String symbol, AssetClass assetClass, LocalDate fromDate, LocalDate toDate) {
		String url = getURL(symbol, assetClass, fromDate, toDate, 9999);
		return API.getInstance(Historical.class, url);
//		return API.getInstance(Historical.class, url, getPath(symbol));
	}
	
	public static class Values {
		// close: "194.39", date: "11/26/2021", high: "196.82", low: "194.19", open: "196.82", volume: "11,113"
		// close: "$17.86", date: "11/26/2021", high: "$18.155", low: "$17.765", open: "$18.03", volume: "1,645,865
		public String close;
		public String date;
		public String high;
		public String low;
		public String open;
		public String volume;
        
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
	public static class Data {
		public static class TradesTable {
			public String   asOf;
			public Values   headers;
			public Values[] rows;
			
			@Override
			public String toString() {
				return ToString.withFieldName(this);
			}
		}
		
		public String      symbol;
		public int         totalRecords;
		public TradesTable tradesTable;
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
	public Data   data;
	public String message;
	public Status status;
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}