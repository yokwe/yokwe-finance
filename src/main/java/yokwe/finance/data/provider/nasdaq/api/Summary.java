package yokwe.finance.data.provider.nasdaq.api;

import yokwe.util.ToString;
import yokwe.util.json.JSON;

public final class Summary {
	// https://api.nasdaq.com/api/quote/LMT/summary?assetclass=stocks
	// https://api.nasdaq.com/api/quote/YYY/summary?assetclass=etf
	
	public static String getURL(String symbol, AssetClass assetClass) {
		return String.format("https://api.nasdaq.com/api/quote/%s/summary?assetclass=%s",
			API.encodeSymbolForURL(symbol), assetClass);
	}
	
	public static final class AdditionalData {
		@JSON.Name("ComplianceStatusLink")  public LabelValue  complianceStatusLink;
	}

	public static final class Stock {
		public static Stock getInstance(String symbol) {
			String url  = getURL(symbol, AssetClass.STOCK);
			return API.getInstance(Stock.class, url);
//			return API.getInstance(Stock.class, url, getPath(symbol));
		}
		
		public static final class SummaryData {
			@JSON.Name("AnnualizedDividend")         @JSON.Ignore public LabelValue annualizedDividend;
			@JSON.Name("Beta")                       @JSON.Ignore public LabelValue beta;
			@JSON.Name("DividendPaymentDate")        @JSON.Ignore public LabelValue dividendPaymentDate;
			@JSON.Name("ExDividendDate")             @JSON.Ignore public LabelValue exDividendDate;
			@JSON.Name("FiftTwoWeekHighLow")         @JSON.Ignore public LabelValue fiftTwoWeekHighLow;
			@JSON.Name("MarketCap")                  @JSON.Ignore public LabelValue marketCap;
			@JSON.Name("PreviousClose")              @JSON.Ignore public LabelValue previousClose;
			@JSON.Name("ShareVolume")                @JSON.Ignore public LabelValue shareVolume;
			@JSON.Name("TodayHighLow")               @JSON.Ignore public LabelValue todayHighLow;
			@JSON.Name("Yield")                      @JSON.Ignore public LabelValue yield;
			
			@JSON.Name("SpecialDividendDate")        @JSON.Ignore public LabelValue specialDividendDate;
			@JSON.Name("SpecialDividendAmount")      @JSON.Ignore public LabelValue specialDividendAmount;
			@JSON.Name("SpecialDividendPaymentDate") @JSON.Ignore public LabelValue specialDividendPaymentDate;
			
			// Stock unique
			@JSON.Name("AverageVolume")              @JSON.Ignore public LabelValue averageVolume;
			@JSON.Name("EarningsPerShare")           @JSON.Ignore public LabelValue earningsPerShare;
			@JSON.Name("Exchange")                                public LabelValue exchange;
			@JSON.Name("ForwardPE1Yr")               @JSON.Ignore public LabelValue forwardPE1Yr;
			@JSON.Name("Industry")                                public LabelValue industry;
			@JSON.Name("OneYrTarget")                @JSON.Ignore public LabelValue oneYrTarget;
			@JSON.Name("PERatio")                    @JSON.Ignore public LabelValue peRatio;
			@JSON.Name("Sector")                                  public LabelValue sector;
			
			@Override
			public String toString() {
				return String.format("{%s  %s  %s}", exchange, sector, industry);
			}
		}
		
		public static final class Data {
			             public AdditionalData additionalData;
			             public String         assetClass;
                         public SummaryData    summaryData;
			             public String         symbol;
			@JSON.Ignore public Object         bidAsk;
			
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
	public static final class ETF {
		public static ETF getInstance(String symbol) {
			String url  = getURL(symbol, AssetClass.ETF);
			return API.getInstance(ETF.class, url);
//			return API.getInstance(ETF.class, url, getPath(symbol));
		}
		
		public static final class SummaryData {
			@JSON.Name("AnnualizedDividend")  public LabelValue annualizedDividend;
			@JSON.Name("Beta")                public LabelValue beta;
			@JSON.Name("DividendPaymentDate") public LabelValue dividendPaymentDate;
			@JSON.Name("ExDividendDate")      public LabelValue exDividendDate;
			@JSON.Name("FiftTwoWeekHighLow")  public LabelValue fiftTwoWeekHighLow;
			@JSON.Name("MarketCap")           public LabelValue marketCap;
			@JSON.Name("PreviousClose")       public LabelValue previousClose;
			@JSON.Name("ShareVolume")         public LabelValue shareVolume;
			@JSON.Name("TodayHighLow")        public LabelValue todayHighLow;
			@JSON.Name("Yield")               public LabelValue yield;
			
			@JSON.Ignore @JSON.Name("SpecialDividendDate")        public LabelValue specialDividendDate;
			@JSON.Ignore @JSON.Name("SpecialDividendAmount")      public LabelValue specialDividendAmount;
			@JSON.Ignore @JSON.Name("SpecialDividendPaymentDate") public LabelValue specialDividendPaymentDate;
			
			// ETF unique
			@JSON.Name("AUM")                 public LabelValue aum;
			@JSON.Name("Alpha")               public LabelValue alpha;
			@JSON.Name("AvgDailyVol20Days")   public LabelValue avgDailyVol20Days;
			@JSON.Name("AvgDailyVol65Days")   public LabelValue avgDailyVol65Days;
			@JSON.Name("ExpenseRatio")        public LabelValue expenseRatio;
			@JSON.Name("FiftyDayAvgDailyVol") public LabelValue fiftyDayAvgDailyVol;
			@JSON.Name("StandardDeviation")   public LabelValue standardDeviation;
			@JSON.Name("WeightedAlpha")       public LabelValue weightedAlpha;

			@Override
			public String toString() {
				return ToString.withFieldName(this);
			}
		}
		
		public static final class Data {
			public AdditionalData additionalData;
			public String         assetClass;
			public SummaryData    summaryData;
			public String         symbol;
			
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
}
