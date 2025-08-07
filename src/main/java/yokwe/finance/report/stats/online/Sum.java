package yokwe.finance.report.stats.online;

import yokwe.util.UnexpectedException;

public class Sum implements OnlineDoubleUnaryOperator {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private boolean firstTime = true;
	private double  sum       = 0;
	
	@Override
	public void accept(double value) {
		// sanity check
		if (Double.isInfinite(value)) {
			logger.error("value is infinite");
			logger.error("  value {}", Double.toString(value));
			throw new UnexpectedException("value is infinite");
		}
		
		if (firstTime) firstTime = false;
		
		sum += value;
	}
	
	@Override
	public double getAsDouble() {
		return firstTime ? Double.NaN : sum;
	}
}
