package yokwe.finance.report.stats.online;

import yokwe.util.UnexpectedException;

public class GeometricMean implements OnlineDoubleUnaryOperator {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private Mean mean = new Mean();
	
	@Override
	public void accept(double value) {
		// sanity check
		if (Double.isInfinite(value)) {
			logger.error("value is infinite");
			logger.error("  value {}", Double.toString(value));
			throw new UnexpectedException("value is infinite");
		}
		if (value < 0) {
			logger.error("value is negative");
			logger.error("  value {}", Double.toString(value));
			throw new UnexpectedException("value is negative");
		}
		
		mean.accept(Math.log(value));
	}
	
	@Override
	public double getAsDouble() {
		return Math.exp(mean.getAsDouble());
	}

}
