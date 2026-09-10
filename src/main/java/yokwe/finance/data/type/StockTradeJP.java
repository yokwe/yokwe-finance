package yokwe.finance.data.type;

import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.util.ToString;

public class StockTradeJP implements Comparable<StockTradeJP> {
	public String     stockCode;

	public LocalDate  dateFirst;
	public LocalDate  dateLast;
	public int        days;

	public BigDecimal liquidity; // 0.00 - 1.00

	// below is average of last 10 days or less
	public long  volume;
	public long  units;
	public long  value;

	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}

	@Override
	public int compareTo(StockTradeJP that) {
		return this.stockCode.compareTo(that.stockCode);
	}
}
