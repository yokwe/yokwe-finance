package yokwe.finance.data.type;

import java.math.BigDecimal;

public class TradingFund implements Comparable<TradingFund> {
	public static final BigDecimal SALES_FEE_UNKNOWN = BigDecimal.valueOf(-1);
	
	public String     isinCode;
	public BigDecimal salesFee;  // 0 for no load
	public String     name;
	
	public TradingFund(String isinCode, BigDecimal salesFee, String name) {
		this.isinCode = isinCode;
		this.salesFee = salesFee;
		this.name     = name;
	}
	
	@Override
	public String toString() {
		return String.format("{%s  %s  %s}", isinCode, salesFee, name);
	}
	@Override
	public int compareTo(TradingFund that) {
		return this.isinCode.compareTo(that.isinCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof TradingFund) {
			var that = (TradingFund)o;
			return
				this.isinCode.equals(that.isinCode) &&
				this.salesFee.compareTo(that.salesFee) == 0;
			// ignore name
		}
		return false;
	}
}
