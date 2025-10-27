package yokwe.finance.report.stats;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.finance.data.type.DailyValue;
import yokwe.finance.report.stats.online.RSI;
import yokwe.util.UnexpectedException;

public final class MonthlyStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	// https://www.nikkei.com/help/contents/markets/fund/
	
	public static final int DAY_IN_YEAR   = 250;
	public static final int WEEK_IN_YEAR  =  52;
	public static final int MONTH_IN_YEAR =  12;
	
	public static final double SQRT_DAY_IN_YEAR   = Math.sqrt(DAY_IN_YEAR);
	public static final double SQRT_WEEK_IN_YEAR  = Math.sqrt(WEEK_IN_YEAR);
	public static final double SQRT_MONTH_IN_YEAR = Math.sqrt(MONTH_IN_YEAR);
	
	public static List<DailyValue> getDivList(List<DailyValue> priceList, List<DailyValue> divList) {
		var list = new ArrayList<DailyValue>();
		
		var divMap = divList.stream().collect(Collectors.toMap(o -> o.date, o -> o.value));
		for(var price: priceList) {
			var date  = price.date;
			var value = divMap.getOrDefault(date, BigDecimal.ZERO);
			
			list.add(new DailyValue(date, value));
		}
		
		return list;
	}
	
	
	public static final class RateOfReturns {
		public final double[] rorPriceArray;    // rorPriceArray.length == duration
		public final double[] rorReinvestArray; // rorReinvestArray.length == duration
		
		public RateOfReturns(double[] rorPriceArray, double[] rorReinvestArray) {
			this.rorPriceArray    = rorPriceArray;
			this.rorReinvestArray = rorReinvestArray;
		}
	}

	public final int          length;           // length of array
	public final LocalDate[]  dateArray;        // 日付
	public final double[]     priceArray;       // 基準価格
	public final double[]     divArray;         // 分配金
	public final double[]     returnArray;      // 日次リターン
	
	public final int          duration;         // duration in Month
	public final LocalDate    firstDate;        // logical first date
	public final LocalDate    lastDate;         // logical last date
	
	public final int[]        startIndexArray;  // array of startIndex
	                                            // startIndexArray[0] is startIndex of first month
	                                            // startIndexArray[1] is startIndex of second month also stopIndexPlusOne of first month
	                                            // startIndexArray[startIndexArray.length - 2] is startIndex of last month
	                                            // startIndexArray[startIndexArray.length - 1] is stopIndexPlusOne of last month

	public final int          startIndex;       // startIndex for firstDate
	public final int          stopIndexPlusOne; // stopIndexPlusOne for lastDate
	
	public final RateOfReturns rateOfReturns;   // monthlyStats.rorPriceArray.length == duration
	
	
	private MonthlyStats(
		LocalDate[] dateArray, double[] priceArray, double[] divArray, double[] returnArray,
		int duration, LocalDate firstDate, LocalDate lastDate,
		int startIndexArray[],
		RateOfReturns rateOfReturns) {
		this.length            = dateArray.length;
		this.dateArray         = dateArray;
		this.priceArray        = priceArray;
		this.divArray          = divArray;
		this.returnArray       = returnArray;
		this.duration          = duration;
		this.firstDate         = firstDate;
		this.lastDate          = lastDate;
		
		this.startIndexArray   = startIndexArray;
		this.startIndex        = startIndexArray[0];
		this.stopIndexPlusOne  = startIndexArray[startIndexArray.length - 1];
		
		this.rateOfReturns     = rateOfReturns;
	}
	public static MonthlyStats getInstance(String code, List<DailyValue> priceList, List<DailyValue> divList) {
		// sanity check
		{
			// priceList and divList has same size
			if (priceList.size() != divList.size()) {
				logger.error("Unexpected list size");
				logger.error("  priceList  {}", priceList.size());
				logger.error("  divList    {}", divList.size());
				throw new UnexpectedException("Unexpected list size");
			}
			// priceList and divList has same date
			for(int i = 0; i < priceList.size(); i++) {
				var priceDate = priceList.get(i).date;
				var divDate   = divList.get(i).date;
				if (priceDate.equals(divDate)) continue;
				logger.error("Unexpected list date");
				logger.error("  {}  {}  {}", i, priceDate, divDate);
				throw new UnexpectedException("Unexpected list date");
			}
			
			{
				long threshold = 11; // 2019-04-26  18012  2019-05-07  18023

				var lastDate = priceList.get(0).date;
				for(int i = 1; i < priceList.size(); i++) {
					var date = priceList.get(i).date;
					var days = ChronoUnit.DAYS.between(lastDate, date);
					
					if (threshold < days) {
						logger.warn("Data has hole  {}  {}  {}  {}", code, days, lastDate, date);
						return null;
					}
					
					// update for next iteration
					lastDate = date;
				}
			}
		}
		
		final LocalDate[]  dateArray   = priceList.stream().map(o -> o.date).toArray(LocalDate[]::new);
		final double[]     priceArray  = priceList.stream().mapToDouble(o -> o.value.doubleValue()).toArray();
		final double[]     divArray    = divList.stream().mapToDouble(o -> o.value.doubleValue()).toArray();
		
//		final int startDayOfMonth = 1;                                                           // new period start at day 1 of month
		final int startDayOfMonth = dateArray[dateArray.length - 1].plusDays(1).getDayOfMonth(); // new period after at next day of last price date
		final int[] startIndexArray = getStartIndexArray(dateArray, startDayOfMonth);
		
		if (startIndexArray.length == 0) {
			// return null if duration is less than 1
			var dateA = dateArray[0];
			var dateB = dateArray[dateArray.length - 1];
			logger.warn("Data period is too short  {}  {}  {}  {}  {}", code, startIndexArray.length, dateA, dateB, dateB.toEpochDay() - dateA.toEpochDay());
			return null;
		}
//		logger.debug("XX  {}  {}  {} - {} => {} - {}", code, startIndexArray.length, dateArray[0], dateArray[dateArray.length - 1], dateArray[startIndexArray[0]], dateArray[startIndexArray[startIndexArray.length - 1]]);

		LocalDate firstDate = dateArray[startIndexArray[0]];
		LocalDate lastDate  = dateArray[startIndexArray[startIndexArray.length - 1]];
		int       duration  = startIndexArray.length - 1;
		
		final double[]     returnArray = new double[priceArray.length];
		{
			returnArray[0] = 0;
			for(int i = 1; i < priceArray.length; i++) {
				double startValue = priceArray[i - 1];
				double endValue   = priceArray[i];
				returnArray[i] = (endValue / startValue) - 1;
			}
		}
		
		final RateOfReturns rateOfReturns = getRateOfReturns(startIndexArray, priceArray, divArray);
		
		return new MonthlyStats(dateArray, priceArray, divArray, returnArray, duration, firstDate, lastDate, startIndexArray, rateOfReturns);
	}
	
	private static RateOfReturns getRateOfReturns(int[] startIndexArray, double[] priceArray, double[] divArray) {
		List<Double> rorPriceList      = new ArrayList<>();
		List<Double> rorReinvestList   = new ArrayList<>();
		
		for(int i = 1; i < startIndexArray.length; i++) {
			int startIndex       = startIndexArray[i - 1];
			int stopIndexPlusOne = startIndexArray[i];
			
			double rorPrice;
			{
				double   startValue   = priceArray[startIndex - 1];
				double   endValue     = priceArray[stopIndexPlusOne - 1];
				
				rorPrice = (endValue / startValue) - 1;
			}
			rorPriceList.add(rorPrice);
			
			double rorReinvest;
			{
				// https://www.nikkei.com/help/contents/markets/fund/#qf15
				//
				// リターン・リターン(１年)・リターン(年率)
				// 投資家が期間中に投資信託を保有して得られた収益を示します。
				// 分配金を受け取らずにその分を元本に加えて運用を続けた場合、
				// 基準価格（分配金再投資ベース）がどれだけ上昇または下落したかをパーセントで表示しています。
				// リターン(年率)は対象期間中のリターンを１年間に換算した年率で表示しています。
				// 【計算内容】
				// ・リターン　(＝累積リターン)
				// {nΠ1(1+月次リターンn)} - 1 　n=6,12,36,60,120,設定来月数
				// ・リターン(1年)、リターン(年率)　（＝年率累積リターン）
				// (1+上記累積リターン)^(12/n) - 1 　n=6,12,36,60,120,設定来月数
				
				double lastPrice           = priceArray[startIndex - 1];
				double lastReinvestedPrice = lastPrice;
				for(int j = startIndex; j < stopIndexPlusOne; j++) {
					double price = priceArray[j];
					double div   = divArray[j];
					
					double dailyReturn     = (price + div) / lastPrice;
					double reinvestedPrice = lastReinvestedPrice * dailyReturn;
					
					// update for next iteration
					lastPrice           = price;
					lastReinvestedPrice = reinvestedPrice;
				}
				double startValue   = priceArray[startIndex - 1];
				double endValue     = lastReinvestedPrice;
				
				rorReinvest = (endValue / startValue) - 1;
			}
			rorReinvestList.add(rorReinvest);
		}
		
		// reverse list. So first entry of list point to latest week
		Collections.reverse(rorPriceList);
		Collections.reverse(rorReinvestList);
		return new RateOfReturns(
			rorPriceList.stream().mapToDouble(o -> o).toArray(),
			rorReinvestList.stream().mapToDouble(o -> o).toArray()
		);
	}
	
	private static int[] getStartIndexArray(LocalDate[] dateArray, TemporalField temporalField) {
	    // array[0] is startIndex of first month
	    // array[1] is startIndex of second month also stopIndexPlusOne of first month
	    // array[array.length - 2] is startIndex of last month
	    // array[array.length - 1] is stopIndexPlusOne of last month

		// first entry point to startIndex of first month
		// last  entry point to stopIndexPlusOne of last month
		List<Integer> list = new ArrayList<>(dateArray.length);
		
		int lastValue = dateArray[0].get(temporalField);
		for(int i = 1; i < dateArray.length; i++) {
			int value = dateArray[i].get(temporalField);
			if (value == lastValue) continue;
			
			list.add(i);
			lastValue = value;
		}
		
		// list to array
		int[] array = list.stream().mapToInt(o -> o).toArray();
		return array;
	}
	
	private static int[] getStartIndexArray(LocalDate[] dateArray, int startDayOfMonth) {
		// check special case  -  dateArray is empty
		if (dateArray.length == 0) return new int[0];
				
		int targetMonth;
		{
			var date = dateArray[0];
			targetMonth = date.getMonthValue();
			if (startDayOfMonth < date.getDayOfMonth()) {
				date = date.plusMonths(1);
			}
			targetMonth = date.getMonthValue();
		}
		
		List<Integer> list = new ArrayList<>(dateArray.length);
		for(int i = 0; i < dateArray.length; i++) {
			var date = dateArray[i];
			if (date.getMonthValue() == targetMonth) {
				if (startDayOfMonth <= date.getDayOfMonth()) {
					if (i != 0) list.add(i); // avoid 0 as first element of list to prevent access priceArray[-1]
					// skip to next month
					targetMonth = date.plusMonths(1).getMonthValue();
				}
			}
		}
		// list to array
		int[] array = list.stream().mapToInt(o -> o).toArray();
		return array;
	}
	
	public boolean contains(int nMonth, int nOffset) {
		if (nMonth <= 0 || nOffset < 0) {
			logger.error("Unexpected value");
			logger.error("  nMonth    {}", nMonth);
			logger.error("  nOffset   {}", nOffset);
			throw new UnexpectedException("Unexpected value");
		}
		return (nMonth + nOffset) <= duration;
	}
	private void checkMonthOffsetValue(int nMonth, int nOffset) {
		if (nMonth <= 0 || nOffset < 0 || duration < (nMonth + nOffset)) {
			logger.error("Unexpected value");
			logger.error("  nMonth    {}", nMonth);
			logger.error("  nOffset   {}", nOffset);
			logger.error("  duration  {}", duration);
			throw new UnexpectedException("Unexpected value");
		}
	}
	private int getStartIndex(int nMonth, int nOffset) {
		return startIndexArray[startIndexArray.length - 1 - nMonth - nOffset];
	}
	private int getStopIndexPlusOne(int nMonth, int nOffset) {
		return startIndexArray[startIndexArray.length - 1 - nOffset];
	}
	
	public LocalDate startDate(int nMonth, int nOffset) {
		// sanity check
		checkMonthOffsetValue(nMonth, nOffset);
		
		final int startIndex = getStartIndex(nMonth, nOffset);
		return dateArray[startIndex];
	}
	public LocalDate stopDate(int nMonth, int nOffset) {
		// sanity check
		checkMonthOffsetValue(nMonth, nOffset);
		
		final int stopIndexPlusOne = getStopIndexPlusOne(nMonth, nOffset);
		return dateArray[stopIndexPlusOne - 1];
	}
	
	public double rateOfReturn(int nMonth, int nOffset) {
		// sanity check
		checkMonthOffsetValue(nMonth, nOffset);
		
		double value = 1;
		for(int i = 0; i < nMonth; i++) {
			value *= (1 + rateOfReturns.rorReinvestArray[nOffset + i]);
		}
		return Math.pow(value, MONTH_IN_YEAR / (double)nMonth) - 1;
	}
	
	public double rateOfReturnNoReinvest(int nMonth, int nOffset) {
		// https://www.nikkei.com/help/contents/markets/fund/#qf16
		//
		// 分配金受取ベースのリターン(年率)
		// 分配金を足した基準価格がどれだけ上昇または下落したかをパーセントで表示しています。
		// その投資信託を購入した投資家の保有期間中の損得の実感に近いリターンと言えます。
		// 対象期間中のリターンを１年間に換算した年率で表示しています。
		// 【計算内容】
		// {(計算期末基準価格＋計算期間分配金合計)／計算期初基準価格} ^ (12／n) - 1
		// n=6,12,36,60,120
		
		// sanity check
		checkMonthOffsetValue(nMonth, nOffset);
		final int startIndex       = getStartIndex(nMonth, nOffset);
		final int stopIndexPlusOne = getStopIndexPlusOne(nMonth, nOffset);
		
		double startValue       = priceArray[startIndex - 1];       // use previous day price as startValue
		double endValue         = priceArray[stopIndexPlusOne - 1];
		double divTotal         = Arrays.stream(divArray, startIndex, stopIndexPlusOne).sum();
		
		return Math.pow((endValue + divTotal) / startValue, MONTH_IN_YEAR / (double)nMonth) - 1;
	}
	
	public double dividend(int nMonth, int nOffset) {
		// sanity check
		checkMonthOffsetValue(nMonth, nOffset);
		final int startIndex       = getStartIndex(nMonth, nOffset);
		final int stopIndexPlusOne = getStopIndexPlusOne(nMonth, nOffset);

		return Arrays.stream(divArray, startIndex, stopIndexPlusOne).sum();
	}
	
	public double yield(int nMonth, int nOffset) {
		// sanity check
		checkMonthOffsetValue(nMonth, nOffset);
//		final int startIndex       = getStartIndex(nMonth, nOffset);
		final int stopIndexPlusOne = getStopIndexPlusOne(nMonth, nOffset);
		
		double endPrice = priceArray[stopIndexPlusOne - 1];
		double dividend = dividend(nMonth, nOffset);
		return (dividend / endPrice) * MONTH_IN_YEAR / (double)nMonth; // calculate annual yield
	}
	
	public double riskDaily(int nMonth, int nOffset) {
		// sanity check
		checkMonthOffsetValue(nMonth, nOffset);
		final int startIndex       = getStartIndex(nMonth, nOffset);
		final int stopIndexPlusOne = getStopIndexPlusOne(nMonth, nOffset);
		
		// calculate risk using daily price value
		return DoubleArray.standardDeviation(returnArray, startIndex, stopIndexPlusOne) * SQRT_DAY_IN_YEAR;
	}
	public double riskMonthly(int nMonth, int nOffset) {
		// sanity check
		checkMonthOffsetValue(nMonth, nOffset);
		
		return DoubleArray.standardDeviation(rateOfReturns.rorPriceArray, nOffset, nOffset + nMonth) * SQRT_MONTH_IN_YEAR;
	}
	public double risk(int nMonth, int nOffset) {
		// リスク・リスク(１年)・リスク(年率)
		// 基準価格のブレ幅の大きさ表します。
		// 過去の基準価格の一定間隔（日次、週次、月次）のリターンを統計処理した標準偏差の数値です。
		// この数値が大きな投資信託ほど大きく値上がりしたり、大きく値下がりしたりする可能性が高く、
		// 逆にリスクの小さい投信ほど値動きは緩やかになると推測できます。
		// 月次更新。6カ月は日次データ、1年は週次データ、3年超は月次データで算出しています。
		// リスク(年率)は対象期間中のリスクを１年間に換算した年率で表示しています。
		// 【計算内容】
		// ・リスク(1年)、リスク(年率)1年　（=年率標準偏差1年）
		// √(nΣ週次リターン^2 - (Σ週次リターン)^2) / n(n-1) × √52　n=52
		// ・リスク(年率)3年～設定来　（=年率標準偏差3年～設定来）
		// √(nΣ月次リターン^2 - (Σ月次リターン)^2) / n(n-1) × √12　n=36,60,120,設定来月数
		
		// sanity check
		checkMonthOffsetValue(nMonth, nOffset);
		
		double value;
		if (nMonth < 12) {
			value = riskDaily(nMonth, nOffset);
		} else if (nMonth < 36) {
			// use weeklyStats
			double[] array;
			{
				List<Double> list = new ArrayList<>();
				
				LocalDate firstDate = lastDate.minusMonths(nMonth + nOffset).plusDays(1); // inclusive
				int[] startIndexArray = getStartIndexArray(dateArray, ChronoField.ALIGNED_WEEK_OF_YEAR);
				for(int i = 1; i < startIndexArray.length; i++) {
					int       startIndex       = startIndexArray[i - 1];
					int       stopIndexPlusOne = startIndexArray[i];
					LocalDate startDate        = dateArray[startIndex];
					LocalDate endDate          = dateArray[stopIndexPlusOne - 1];
					
					if (startDate.isBefore(firstDate)) continue;
					if (endDate.isAfter(lastDate)) break;
					
					double startValue = priceArray[startIndex - 1];       // previous day price as startValue
					double endValue   = priceArray[stopIndexPlusOne - 1];
					list.add((endValue / startValue) - 1);
				}
				array = list.stream().mapToDouble(o -> o).toArray();
			}
			value = DoubleArray.standardDeviation(array) * SQRT_WEEK_IN_YEAR;
		} else {
			value = riskMonthly(nMonth, nOffset);
		}
		
		return value;
	}
	
	public double rsi(int nMonth, int nOffset, int duration) {
		// sanity check
		checkMonthOffsetValue(nMonth, nOffset);
		final int startIndex       = getStartIndex(nMonth, nOffset);
		final int stopIndexPlusOne = getStopIndexPlusOne(nMonth, nOffset);
		
		return new RSI(duration).applyAsDouble(priceArray, startIndex, stopIndexPlusOne);
	}
	public double rsi(int nMonth, int nOffset) {
		return rsi(nMonth, nOffset, 14);
	}
	
	private <T> T[] copyOfRange(int nMonth, int nOffset, T[] array) {
		// sanity check
		checkMonthOffsetValue(nMonth, nOffset);
		final int startIndex       = getStartIndex(nMonth, nOffset);
		final int stopIndexPlusOne = getStopIndexPlusOne(nMonth, nOffset);
		
		return Arrays.copyOfRange(array, startIndex, stopIndexPlusOne);
	}
	private double[] copyOfRange(int nMonth, int nOffset, double[] array) {
		// sanity check
		checkMonthOffsetValue(nMonth, nOffset);
		final int startIndex       = getStartIndex(nMonth, nOffset);
		final int stopIndexPlusOne = getStopIndexPlusOne(nMonth, nOffset);
		
		return Arrays.copyOfRange(array, startIndex, stopIndexPlusOne);
	}
	
	public LocalDate[] dateArray(int nMonth, int nOffset) {
		return copyOfRange(nMonth, nOffset, dateArray);
	}
	public double[] priceArray(int nMonth, int nOffset) {
		return copyOfRange(nMonth, nOffset, priceArray);
	}
	public double[] divArray(int nMonth, int nOffset) {
		return copyOfRange(nMonth, nOffset, divArray);
	}
	public double[] returnArray(int nMonth, int nOffset) {
		return copyOfRange(nMonth, nOffset, returnArray);
	}
	
}
