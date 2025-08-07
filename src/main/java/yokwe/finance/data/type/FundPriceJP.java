package yokwe.finance.data.type;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import yokwe.util.ToString;

public class FundPriceJP implements Comparable<FundPriceJP> {
	public final LocalDate  date;   // 年月日
	public final BigDecimal nav;    // 純資産総額（円）
	public final BigDecimal price;  // 基準価額(円)
	public final BigDecimal units;  // 総口数 = 純資産総額 / 基準価額

	public FundPriceJP(LocalDate date, BigDecimal nav, BigDecimal price, BigDecimal units) {		
		this.date  = date;
		this.nav   = nav;
		this.price = price;
		this.units = units;
	}
	public FundPriceJP(LocalDate date, BigDecimal nav, BigDecimal price) {
		this(date, nav, price, price.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : nav.divide(price, 0, RoundingMode.HALF_UP));
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}

	@Override
	public int compareTo(FundPriceJP that) {
		return this.date.compareTo(that.date);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else {
			if (o instanceof FundPriceJP) {
				FundPriceJP that = (FundPriceJP)o;
				return
					this.date.equals(that.date) &&
					this.nav.equals(that.nav) &&
					this.price.equals(that.price) &&
					this.units.equals(that.units);
			} else {
				return false;
			}
		}
	}
	@Override
	public int hashCode() {
		return this.date.hashCode();
	}

}
