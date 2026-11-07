package yokwe.finance.report.stats.online;

import java.util.stream.Collectors;

import yokwe.finance.data.provider.jpx.StorageJPX;
import yokwe.finance.data.type.DailyValue;
import yokwe.util.UnexpectedException;

public class MACD implements OnlineDoubleUnaryOperator {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static final int    PERIOD_A      = 26;
	public static final int    PERIOD_B      = 12;
	public static final int    PERIOD_SIGNAL = 9;

	public static MACD getInstance(int periodA, int periodB, int periodSignal) {
		return new MACD(periodA, periodB, periodSignal);
	}
	public static MACD getInstance() {
		return new MACD(PERIOD_A, PERIOD_B, PERIOD_SIGNAL);
	}

	private MACD(int periodA, int periodB, int periodSignal) {
		emaA   = EMA.fromDays(periodA);
		emaB   = EMA.fromDays(periodB);
		signal = new SMA(PERIOD_SIGNAL);
	}

	private EMA emaA;
	private EMA emaB;
	private SMA signal;

	private double lastValue     = Double.NaN;
	private double lastSignal    = Double.NaN;
	private double lastHistogram = Double.NaN;

	public double getValue() {
		return lastValue;
	}
	public double getSignal() {
		return lastSignal;
	}
	public double getHistogram() {
		return lastHistogram;
	}


	@Override
	public double getAsDouble() {
		return getValue();
	}
	@Override
	public void accept(double value) {
		// sanity check
		if (Double.isInfinite(value)) {
			logger.error("value is infinite");
			logger.error("  value {}", Double.toString(value));
			throw new UnexpectedException("value is infinite");
		}

		emaA.accept(value);
		emaB.accept(value);
		lastValue = emaB.getAsDouble() - emaA.getAsDouble();

		signal.accept(lastValue);
		lastSignal = signal.getAsDouble();

		lastHistogram = lastValue - lastSignal;
	}

	private static void test() {
		var stockCode = "28400";

		var stockName = StorageJPX.StockCodeNameJPX.getList().stream().filter(o -> o.stockCode.equals(stockCode)).map(o -> o.name).findAny().get();
		logger.info("{}  {}", stockCode, stockName);
		var priceList = StorageJPX.StockPriceOHLCV.getList(stockCode).stream().map(o -> new DailyValue(o.date, o.close)).collect(Collectors.toList());
		var stopDate  = priceList.getLast().date;
		var startDate = stopDate.minusYears(1).plusDays(1);
		priceList.removeIf(o -> o.date.isBefore(startDate));

		logger.info("date  {}  {}", priceList.getFirst().date, priceList.getLast().date);

		var array = priceList.stream().mapToDouble(o -> o.value.doubleValue()).toArray();

		MACD macd = MACD.getInstance();

		for(var e: array) {
			macd.accept(e);
		}
		var value     = macd.getValue();
		var signal    = macd.getSignal();
		var histogram = macd.getHistogram();

		logger.info("value     {}", String.format("%.2f", value));
		logger.info("signal    {}", String.format("%.2f", signal));
		logger.info("histogram {}", String.format("%.2f", histogram));

	}

	public static void main(String[] args) {
		logger.info("START");
		test();
		logger.info("STOP");
	}
}
