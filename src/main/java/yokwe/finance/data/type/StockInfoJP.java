package yokwe.finance.data.type;

import yokwe.util.ToString;

public final class StockInfoJP implements Comparable<StockInfoJP> {
	public enum SimpleType {
		STOCK,
		ETF,
		ETN,
		REIT,
		INFRA,
		OTHER,
		NEW,
	}
	public enum Type {
		// STOCK
		DOMESTIC_PRIME(SimpleType.STOCK),
		DOMESTIC_STANDARD(SimpleType.STOCK),
		DOMESTIC_GROWTH(SimpleType.STOCK),
		FOREIGN_PRIME(SimpleType.STOCK),
		FOREIGN_STANDARD(SimpleType.STOCK),
		FOREIGN_GROWTH(SimpleType.STOCK),
		// ETF
		ETF(SimpleType.ETF),
		// ETN
		ETN(SimpleType.ETN),
		// REIT
		REIT(SimpleType.REIT),
		INFRA(SimpleType.INFRA),
		// OTHER
		CERTIFICATE(SimpleType.OTHER);
		
		public final SimpleType simpleType;
		
		private Type(SimpleType simpleType) {
			this.simpleType = simpleType;
		}
		
		public boolean isStock() {
			return this.simpleType == SimpleType.STOCK;
		}
		public boolean isETF() {
			return this.simpleType == SimpleType.ETF;
		}
		public boolean isETN() {
			return this.simpleType == SimpleType.ETN;
		}
		public boolean isREIT() {
			return this.simpleType == SimpleType.REIT;
		}
		public boolean isInfra() {
			return this.simpleType == SimpleType.INFRA;
		}
	}
	
	public String	stockCode;
	public String	isinCode;
	public int		tradeUnit;
	public Type		type;
	public String	sector;
	public String	industry;
	public String	name;
	
	public StockInfoJP(
		String     stockCode,
		String     isinCode,
		int        tradeUnit,
		Type       type,
		String     sector,
		String     industry,
		String     name
		) {
		this.stockCode = stockCode;
		this.isinCode  = isinCode;
		this.tradeUnit = tradeUnit;
		this.type      = type;
		this.sector    = sector;
		this.industry  = industry;
		this.name      = name;
	}
	
	@Override
	public int compareTo(StockInfoJP that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}
