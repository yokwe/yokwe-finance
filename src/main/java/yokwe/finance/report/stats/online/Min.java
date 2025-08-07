package yokwe.finance.report.stats.online;

import yokwe.util.UnexpectedException;

public class Min implements OnlineDoubleUnaryOperator {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private boolean firstTime = true;
	private double  minValue  = Double.NaN;
	
	@Override
	public void accept(double value) {
		// sanity check
		if (Double.isInfinite(value)) {
			logger.error("value is infinite");
			logger.error("  value {}", Double.toString(value));
			throw new UnexpectedException("value is infinite");
		}
		
		if (firstTime) {
			minValue  = value;
			firstTime = false;
		} else {
			if (value < minValue) minValue = value;
		}
	}
	
	@Override
	public double getAsDouble() {
		return minValue;
	}

}
