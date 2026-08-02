package yokwe.finance.report.stats.online;

import java.math.BigDecimal;

import yokwe.util.UnexpectedException;

public class BigDecimalSMA implements BigDecimalOnlineUnaryOperator {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private final int          size;
	private final BigDecimal[] data;

	private int        count   = 0;
	private int        index   = 0;
	private BigDecimal average = null;

	private BigDecimalMean mean = new BigDecimalMean();

	public BigDecimalSMA(int size_) {
		size = size_;
		data = new BigDecimal[size];
	}

	@Override
	public BigDecimal get() {
		return average;
	}

	@Override
	public void accept(BigDecimal value) {
		// sanity check
		if (value == null) {
			logger.error("value is null");
			throw new UnexpectedException("value is null");
		}

		if (count < size) {
			// write data
			data[index] = value;
			// update mean
			mean.accept(value);
		} else {
			// save old value
			BigDecimal oldValue = data[index];
			// overwrite data
			data[index] = value;
			// update mean with oldValue and value
			mean.replace(oldValue, value);
		}
		average = mean.get();

		// update for next iteration
		count++;
		index++;
		if (index == size) {
			index = 0;
		}
	}


	public static void main(String[] args) {
		logger.info("START");

		BigDecimal[] data = {new BigDecimal(10), new BigDecimal(11), new BigDecimal(12), new BigDecimal(13), new BigDecimal(14), new BigDecimal(15), new BigDecimal(16)};


		var sma = new BigDecimalSMA(5);
		for(var e: data) {
			sma.accept(e);
			logger.info("SMA  {}  {}  {}", sma.size, e, sma.get());
		}

		logger.info("STOP");
	}
}
