package yokwe.finance.report.stats.online;

import java.util.Arrays;

import yokwe.util.UnexpectedException;

//
// return exponential moving average
//
public final class EMA implements OnlineDoubleUnaryOperator {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	// calculation of alpha
	public static final double SMOOTHING = 2.0;
	public static double getAlpha(int days) {
		return SMOOTHING / (1 + days);
	}
	public static double getAlphaFromDecayFactor(double decayFactor) {
		return 1.0 - decayFactor;
	}
	
	private final double alpha;
	
	private boolean firstTime = true;
	private double  average   = Double.NaN;
	
	public EMA(double alpha) {
		this.alpha = alpha;
	}
	
	// DoubleConsumer
	@Override
	public void accept(double value) {
		// sanity check
		if (Double.isInfinite(value)) {
			logger.error("value is infinite");
			logger.error("  value {}", Double.toString(value));
			throw new UnexpectedException("value is infinite");
		}
		
		if (firstTime) {
			firstTime = false;
			average   = value;
		}
		
		// average = value * alpha + average * (1 - alpha)
		//         = average * (1 - alpha) + value * alpha
		//         = average - average * alpha + value * alpha
		//         = average + alpha * (value - average)
		// number of multiply is reduced
		average += alpha * (value - average);
	}
	
	// DoubleSupplier
	@Override
	public double getAsDouble() {
		return average;
	}
	
	private static void testTable53() {
		// See Table 5.3 of page 82
		//   https://www.msci.com/documents/10199/5915b101-4206-4ba0-aee2-3449d5c7e95a
		//   https://elischolar.library.yale.edu/cgi/viewcontent.cgi?article=1546&context=ypfs-documents
		double alpha = 0.06; // 1 - 0.94 
		
		double[] data = {
			0.633,
			0.115,
			-0.459,
			0.093,
			0.176,
			-0.087,
			-0.142,
			0.324,
			-0.943,
			-0.528,
			-0.107,
			-0.159,
			-0.445,
		 	0.053,
			0.152,
			-0.318,
			0.424,
			-0.708,
			-0.105,
			-0.257,
		};
		
		double[] var_r = Arrays.stream(data).map(o -> o * o).map(new EMA(alpha)).toArray();
		logger.info("");
		for(int i = 0; i < var_r.length; i++) {
			logger.info("Table 5.3 {}", String.format("%8.3f  %8.3f", data[i], var_r[i]));
		}
		logger.info("last line should be -0.257     0.224");
	}
	
	public static void main(String[] args) {
		logger.info("START");
		testTable53();
		logger.info("STOP");
	}

}