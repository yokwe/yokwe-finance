package yokwe.finance.report.stats.online;

import yokwe.util.UnexpectedException;

public final class LogReturn implements OnlineDoubleUnaryOperator {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
		
	public static final double getValue(double startValue, double endValue) {
		return Math.log(endValue / startValue);
	}
	
	
	private boolean hasValue;
	private double  value;
	private double  lastValue = Double.NaN;

	public LogReturn() {
		hasValue = false;
		value    = Double.NaN;
	}
	public LogReturn(double newValue) {
		hasValue = true;
		value    = newValue;
	}
	
	@Override
	public void accept(double newValue) {
		// sanity check
		if (Double.isInfinite(newValue)) {
			logger.error("newValue is infinite");
			logger.error("  newValue {}", Double.toString(value));
			throw new UnexpectedException("newValue is infinite");
		}
		
		if (hasValue) {
			lastValue = value;
		} else {
			hasValue  = true;
			lastValue = newValue;
		}
		value = newValue;
	}
	
	@Override
	public double getAsDouble() {
		return hasValue ? getValue(lastValue, value) : Double.NaN;
	}
}
