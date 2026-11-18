package yokwe.finance.data.provider.jita;

import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.util.ToString;

public class FundPrice implements Comparable<FundPrice> {
	public LocalDate  date;
	public BigDecimal price;
	public BigDecimal nav;

	public FundPrice(LocalDate date, BigDecimal price, BigDecimal nav) {
		this.date  = date;
		this.price = price;
		this.nav   = nav;
	}

	@Override
	public int compareTo(FundPrice that) {
		return this.date.compareTo(that.date);
	}

	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}
