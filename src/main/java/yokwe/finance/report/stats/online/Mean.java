package yokwe.finance.report.stats.online;

import yokwe.util.UnexpectedException;

public final class Mean implements OnlineDoubleUnaryOperator {	
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private int     count = 0;
	private double  mean  = 0;
	
	@Override
	public double getAsDouble() {
		return (count == 0) ? Double.NaN : mean;
	}
	
	// method for SMA
	// replace oldValue with value and update lastMean
	public double replace(double oldValue, double value) {
		// ((lastMean * count) - oldValue + value) / count
		// lastMean + (value - oldValue) / count
		mean += (value - oldValue) / count;
		return mean;
	}

	@Override
	public void accept(double value) {
		// sanity check
		if (Double.isInfinite(value)) {
			logger.error("value is infinite");
			logger.error("  value {}", Double.toString(value));
			throw new UnexpectedException("value is infinite");
		}

		count++;
		mean += (value - mean) / count;
	}

}
