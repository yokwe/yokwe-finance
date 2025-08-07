package yokwe.finance.data.type;

import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.util.ToString;
import yokwe.util.UnexpectedException;

public final class FXRate implements Comparable<FXRate> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public final LocalDate  date;
	public final BigDecimal usd;
	
	public FXRate(LocalDate date, BigDecimal usd) {
		this.date = date;
		this.usd  = usd;
	}
	
	public BigDecimal rate(Currency currency) {
		switch(currency) {
		case USD: return usd;
		default:
			logger.error("Unexpected currency");
			logger.error("  currency {}!", currency);
			throw new UnexpectedException("Unexpected currency");
		}
	}
	
	@Override
	public int compareTo(FXRate that) {
		return this.date.compareTo(that.date);
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}
