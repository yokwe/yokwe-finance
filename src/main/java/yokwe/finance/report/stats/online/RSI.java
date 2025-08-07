package yokwe.finance.report.stats.online;

import yokwe.util.UnexpectedException;

// Calculate RSI using Wilder methods
//   See https://school.stockcharts.com/doku.php?id=technical_indicators:relative_strength_index_rsi
public final class RSI implements OnlineDoubleUnaryOperator {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static final int    DEFAULT_SIZE = 14;
	public static final double UNKNOWN_RSI  = -1;
	
	private final   int    size;
	private final   double alpha;
	private final   EMA    emaGain;
	private final   EMA    emaLoss;
	
	private int    count     = 0;
	private double lastValue = 0;
	private double sumGain   = 0;
	private double sumLoss   = 0;
	private double rsi       = Double.NaN;
	
	public RSI() {
		this(DEFAULT_SIZE);
	}
	public RSI(int size_) {
		size  = size_;
		alpha = 1.0 / size;
		
		emaGain = new EMA(alpha);
		emaLoss = new EMA(alpha);
	}
	
	@Override
	public void accept(double value) {
		// sanity check
		if (Double.isInfinite(value)) {
			logger.error("value is infinite");
			logger.error("  value {}", Double.toString(value));
			throw new UnexpectedException("value is infinite");
		}

		final double change;
		final double changeGain;
		final double changeLoss;
		{
			change  = (count == 0) ? 0 : value - lastValue;
			changeGain = (0 < change) ? change : 0;
			changeLoss = (change < 0) ? -change : 0;
		}
		
		
		final double avgGain;
		final double avgLoss;
		{
			if (count < size) {				
				sumGain += changeGain;
				sumLoss += changeLoss;

				avgGain = 0;
				avgLoss = 0;
			} else if (count == size) {
				sumGain += changeGain;
				sumLoss += changeLoss;

				// alpha == (1 / size)
				// sumGain * alpha == sumGain / size
				// sumLoss * alpha == sumLoss / size
				avgGain = emaGain.applyAsDouble(sumGain * alpha);
				avgLoss = emaLoss.applyAsDouble(sumLoss * alpha);

				sumGain = 0;
				sumLoss = 0;
			} else {
				avgGain = emaGain.applyAsDouble(changeGain);
				avgLoss = emaLoss.applyAsDouble(changeLoss);
			}
		}
		
		if (count < size) {
			rsi = UNKNOWN_RSI;
		} else {
			double a = avgGain * 100;
			double b = avgGain + avgLoss;
			// avoid divide by zero
			rsi = (b == 0) ? UNKNOWN_RSI : a / b;
		}
				
		// update for next iteration
		lastValue = value;
		count++;
	}
	
	@Override
	public double getAsDouble() {
		return rsi;
	}
	
	private static void testA() {
		// See http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:relative_strength_index_rsi
		double data[] = {
			44.3389,
			44.0902,
			44.1497,
			43.6124,
			44.3278,
			44.8264,
			45.0955,
			45.4245,
			45.8433,
			46.0826,
			45.8931,
			46.0328,
			45.6140,
			46.2820,
			46.2820,
			46.0028,
			46.0328,
			46.4116,
			46.2222,
			45.6439,
			46.2122,
			46.2521,
			45.7137,
			46.4515,
			45.7835,
			45.3548,
			44.0288,
			44.1783,
			44.2181,
			44.5672,
			43.4205,
			42.6628,
			43.1314,
		};
		
		var op = new RSI(14);
		for(int i = 0; i < data.length; i++) {
			var rsi = op.applyAsDouble(data[i]);
			logger.info("data {}", String.format("%2d  %6.4f  %6.4f", i + 1, data[i], rsi));
		}
		logger.info("last line should be 43.13  37.77");
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		testA();
		
		logger.info("STOP");
	}
}
