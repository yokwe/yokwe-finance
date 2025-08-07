package yokwe.finance.data.provider.nasdaq.api;

public class Screener {
	public static class Stock {
		public static final String URL = "https://api.nasdaq.com/api/screener/stocks?download=true";

		public static Stock getInstance() {
			return API.getInstance(Stock.class, URL);
//			return API.getInstance(Stock.class, URL, PATH_FILE);
		}

		public static class Values {
			public String country;
			public String industry;
			public String ipoyear;
			public String lastsale;
			public String marketCap;
			public String name;
			public String netchange;
			public String pctchange;
			public String sector;
			public String symbol;
			public String url;
			public String volume;
		}

		public static class Data {
			public String   asOf;
			public Values   headers;
			public Values[] rows;
		}

		public Data   data;
		public String message;
		public Status status;
	}
	
	public static class ETF {
		public static final String URL = "https://api.nasdaq.com/api/screener/etf?download=true";
		
		public static ETF getInstance() {
			return API.getInstance(ETF.class, URL);
//			return API.getInstance(ETF.class, URL, PATH_FILE);
		}
		
		public static class Values {
			public String companyName;
			public String deltaIndicator;
			public String lastSalePrice;
			public String netChange;
			public String oneYearPercentage;
			public String percentageChange;
			public String symbol;
			
			public Values() {
				companyName       = null;
				deltaIndicator    = null;
				lastSalePrice     = null;
				netChange         = null;
				oneYearPercentage = null;
				percentageChange  = null;
				symbol            = null;
			}
		}
		
		public static class Data {
			public String   asOf;
			public Values   headers;
			public Values[] rows;
			
			public Data() {
				headers = null;
				rows    = null;
			}
		}
		
		public static class DataOuter {
			public Data   data;
			public String dataAsOf;
			
			public DataOuter() {
				data     = null;
				dataAsOf = null;
			}
		}
		
		public DataOuter data;
		public String    message;
		public Status    status;
		
		public ETF() {
			data    = null;
			message = null;
			status  = null;
		}
	}
}
