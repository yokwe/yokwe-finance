package yokwe.finance.report.stats.online;

import java.math.BigDecimal;
import java.math.MathContext;

import yokwe.util.UnexpectedException;

public class BigDecimalMean implements BigDecimalOnlineUnaryOperator {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private MathContext mathContext = MathContext.DECIMAL64;
	private int        count = 0;
	private BigDecimal sum   = null;

	@Override
	public BigDecimal get() {
		if (count == 0) {
			logger.error("count is zero");
			throw new UnexpectedException("count is zero");
		}
		return sum.divide(new BigDecimal(count), mathContext);
	}

	// method for SMA
	// replace oldValue with value
	public BigDecimal replace(BigDecimal oldValue, BigDecimal value) {
		if (count == 0) {
			logger.error("count is zero");
			throw new UnexpectedException("count is zero");
		}
		sum = sum.subtract(oldValue).add(value);
		return get();
	}

	@Override
	public void accept(BigDecimal value) {
		if (count == 0) {
			sum = value;
			count = 1;
		} else {
			sum = sum.add(value);
			count++;
		}
	}

}
