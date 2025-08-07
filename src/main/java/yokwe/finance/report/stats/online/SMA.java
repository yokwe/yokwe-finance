package yokwe.finance.report.stats.online;

import yokwe.util.UnexpectedException;

//
// simple moving average
//
public final class SMA implements OnlineDoubleUnaryOperator {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private final int      size;
	private final double[] data;

	private int      count   = 0;
	private int      index   = 0;
	private double   average = Double.NaN;
	
	private Mean mean = new Mean();
	
	public SMA(int size_) {
		size = size_;
		data = new double[size];
	}
	
	@Override
	public double getAsDouble() {
		return average;
	}
	
	@Override
	public void accept(double value) {
		// sanity check
		if (Double.isInfinite(value)) {
			logger.error("value is infinite");
			logger.error("  value {}", Double.toString(value));
			throw new UnexpectedException("value is infinite");
		}
		
		if (count < size) {
			// write data
			data[index] = value;
			// update mean
			mean.accept(value);
		} else {
			// save old value
			double oldValue = data[index];
			// overwrite data
			data[index] = value;
			// update mean with oldValue and value
			mean.replace(oldValue, value);
		}
		average = mean.getAsDouble();
		
		// update for next iteration
		count++;
		index++;
		if (index == size) index = 0;
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		double[] data = {10, 11, 12, 13, 14, 15, 16};
		
		SMA sma = new SMA(5);
		for(var e: data) {
			sma.accept(e);
			logger.info("SMA  {}  {}  {}", sma.size, e, sma.getAsDouble());
		}
		
		logger.info("STOP");
	}
}