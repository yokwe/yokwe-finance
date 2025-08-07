package yokwe.finance.report.stats.online;

import yokwe.util.UnexpectedException;

public class Max implements OnlineDoubleUnaryOperator {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private boolean firstTime = true;
	private double  maxValue  = Double.NaN;
	
	@Override
	public void accept(double value) {
		// sanity check
		if (Double.isInfinite(value)) {
			logger.error("value is infinite");
			logger.error("  value {}", Double.toString(value));
			throw new UnexpectedException("value is infinite");
		}
		
		if (firstTime) {
			maxValue  = value;
			firstTime = false;
		} else {
			if (maxValue < value) maxValue = value;
		}
	}
	
	@Override
	public double getAsDouble() {
		return maxValue;
	}

}
