package yokwe.finance.data.type;

public class TradingStock implements Comparable<TradingStock>{
	public enum FeeType {
		PAID(1),
		BUY_FREE(2),
		FREE(3);
		
		public final int value;
		
		private FeeType(int value) {
			this.value = value;
		}
	}
	public enum TradeType {
		BUY_SELL(1),
		SELL(2);
		
		public final int value;
		
		private TradeType(int value) {
			this.value = value;
		}
	}
	
	public String    stockCode;
	public FeeType   feeType;
	public TradeType tradeType;
	public String    name;
	
	public TradingStock(String stockCode, FeeType feeType, TradeType tradeType, String name) {
		this.stockCode = stockCode;
		this.feeType   = feeType;
		this.tradeType = tradeType;
		this.name      = name;
	}
	
	@Override
	public String toString() {
		return String.format("{%s  %s  %s  %s}", stockCode, feeType, tradeType, name);
	}
	@Override
	public int compareTo(TradingStock that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof TradingStock) {
			var that = (TradingStock)o;
			return
				this.stockCode.equals(that.stockCode) &&
				this.feeType == that.feeType &&
				this.tradeType == that.tradeType;
			// ignore name
		}
		return false;
	}
}
