package yokwe.finance.data.type;

import yokwe.util.ToString;

public final class StockInfoUS implements Comparable<StockInfoUS> {
	public static String toYahooSymbol(String symbol) {
		return symbol.replace("-", "-P").replace(".", "-");
	}
	
	public static String toNASDAQSymbol(String stockCode) {
		return stockCode.replace("-", ".PR"); // BC-A => BC.PRA
	}
	
	
	public enum Market {
		BATS,
		NASDAQ,
		NYSE,
		IEXG,
	}
	
	public enum SimpleType {
		STOCK,
		ETF,
		OTHER,
	}
	
	public enum Type {
		CEF    (SimpleType.STOCK), // CLOSED_END_FUND
		COMMON (SimpleType.STOCK), // COMMON_STOCK
		ADR    (SimpleType.STOCK), // DEPOSITORY_RECEIPT
		ETF    (SimpleType.ETF),   // EXCHANGE_TRADED_FUND
		ETN    (SimpleType.ETF),   // EXCHANGE_TRADED_NOTE
		LP     (SimpleType.STOCK), // LIMITED_PARTNERSHIP
		PREF   (SimpleType.STOCK), // PREFERRED_STOCK
		REIT   (SimpleType.STOCK), // REIT
		TRUST  (SimpleType.STOCK), // TRUST
		
		UNIT   (SimpleType.OTHER), // UNIT
		UBI    (SimpleType.OTHER), // UNITS_OF_BENEFICIAL_INTEREST
		
		WARRANT(SimpleType.OTHER); // WARRANT

		public final SimpleType simpleType;
		
		Type(SimpleType simpleType) {
			this.simpleType = simpleType;
		}
		
		public boolean isETF() {
			return simpleType == SimpleType.ETF;
		}
		public boolean isStock() {
			return simpleType == SimpleType.STOCK;
		}
	}
	
	public String	stockCode; // normalized symbol like TRNT-A and RDS.A not like TRTN^A and RDS/A
	public Market	market;
	public Type		type;
	public String	industry;
	public String	sector;
	public String	name;
	
	public StockInfoUS(String stockCode, Market market, Type type, String industry, String sector, String name) {
		this.stockCode = stockCode.trim();
		this.market    = market;
		this.type      = type;
		this.industry  = industry;
		this.sector    = sector;
		this.name      = name;
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	@Override
	public int compareTo(StockInfoUS that) {
		return this.stockCode.compareTo(that.stockCode);
	}
}
