package yokwe.finance.data.provider.moneybu;

import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.util.ToString;


public class StockInfo implements Comparable<StockInfo> {
	public static final LocalDate  UNKNOWN_DIV_DATE  = LocalDate.of(2099, 1, 1);
	public static final BigDecimal UNKNOWN_DIV_VALUE = BigDecimal.ONE.negate();
	public static final BigDecimal UNKNOWN_DIV_YIELD = BigDecimal.TWO.negate();

	public String     stockCode;
	public String     isinCode;

	public int        duration; // listing duration in month

	public LocalDate  lastDivDate;
	public BigDecimal lastDivValue;

	public BigDecimal divYield;

	public String     name;


	@Override
	public int compareTo(StockInfo that) {
		return this.stockCode.compareTo(that.stockCode);
	}

	@Override
	public String toString() {
	    return ToString.withFieldName(this);
	}

	public boolean hasValidDivDate() {
		return lastDivDate.compareTo(UNKNOWN_DIV_DATE) != 0;
	}
	public boolean hasValidDivValue() {
		return lastDivValue.compareTo(UNKNOWN_DIV_VALUE) != 0;
	}
	public boolean hasValidDivYield() {
		return divYield.compareTo(UNKNOWN_DIV_YIELD) != 0;
	}

	public boolean hasZeroDivValue() {
		return lastDivValue.signum() == 0;
	}
}
