package yokwe.finance.report.stats;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.finance.data.type.DailyValue;
import yokwe.finance.data.type.OHLCV;
import yokwe.finance.report.stats.online.HV;
import yokwe.finance.report.stats.online.LogReturn;
import yokwe.finance.report.stats.online.Mean;
import yokwe.finance.report.stats.online.NoReinvestedValue;
import yokwe.finance.report.stats.online.RSI;
import yokwe.finance.report.stats.online.ReinvestedValue;
import yokwe.finance.report.stats.online.SimpleReturn;
import yokwe.finance.report.stats.online.Variance;

public class StockStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static double doubleOrElse(double value, double orElse) {
		return Double.isNaN(value) ? orElse : value;
	}
	
	public static StockStats getInstance(String stockCode, LocalDate dateStop, List<OHLCV> priceList, List<DailyValue> divList) {
		// sanity check
		if (priceList == null) {
			logger.warn("priceList is null");
			return null;
		}
		if (divList == null) {
			logger.warn("divList is null");
			return null;
		}
		
		// NOTE dateStart an dateStop is inclusive
		var dateStartY1 = dateStop.minusYears(1).plusDays(1);
		var dateStartY3 = dateStop.minusYears(3).plusDays(1);
		
		OHLCV[] priceArrayY1 = priceList.stream().filter(o -> !o.date.isBefore(dateStartY1) && !o.date.isAfter(dateStop)).toArray(OHLCV[]::new);
		OHLCV[] priceArrayY3 = priceList.stream().filter(o -> !o.date.isBefore(dateStartY3) && !o.date.isAfter(dateStop)).toArray(OHLCV[]::new);
		// sanity check
		if (priceArrayY1.length < 4) {
			logger.warn("number of price in date range is too small {}  {}  {}", dateStartY1, dateStop, priceArrayY1.length);
			return null;
		}
		double[] closeArrayY1 = Arrays.stream(priceArrayY1).mapToDouble(o -> o.close.doubleValue()).toArray();
		double[] volArrayY1   = Arrays.stream(priceArrayY1).mapToDouble(o -> o.volume).toArray(); // intentionally use double[] for vol5 and vol21
		
		DailyValue[] divArrayY1 = divList.stream().filter(o -> !o.date.isBefore(dateStartY1) && !o.date.isAfter(dateStop)).toArray(DailyValue[]::new);
		
		// build stats
		StockStats stats = new StockStats();
		
		{
			var lastPrice     = priceArrayY1[priceArrayY1.length - 1];
			var lastLastPrice = priceArrayY1[priceArrayY1.length - 2];
			
			stats.date   = lastPrice.date;
			stats.pricec = priceArrayY1.length;
			stats.price  = lastPrice.close.doubleValue();
			stats.last   = SimpleReturn.getValue(lastLastPrice.close.doubleValue(), lastPrice.close.doubleValue());
		}
		
		// rorPrice, rorReinvested and rorNoReinvested
		{
			var divMap            = Arrays.asList(divArrayY1).stream().collect(Collectors.toMap(o -> o.date, o -> o.value.doubleValue()));
			var reinvestedValue   = new ReinvestedValue();
			var noReinvestedValue = new NoReinvestedValue();
			
			var startPrice = priceArrayY1[0].close.doubleValue();
			var endPrice   = priceArrayY1[priceArrayY1.length - 1].close.doubleValue();
			for(var e: priceArrayY1) {
				var date  = e.date;
				var price = e.close.doubleValue();
				var div   = divMap.getOrDefault(date, (double)0);
				
				noReinvestedValue.accept(price, div);
				reinvestedValue.accept(price, div);
			}
			
			stats.rorPrice        = SimpleReturn.getValue(startPrice, endPrice);
			stats.rorReinvested   = SimpleReturn.getValue(startPrice, reinvestedValue.getAsDouble());
			stats.rorNoReinvested = SimpleReturn.getValue(startPrice, noReinvestedValue.getAsDouble());
		}
		
		// sd hv
		{
			if (30 <= stats.pricec) {
				{
					var logReturn = Arrays.stream(closeArrayY1).map(new LogReturn()).toArray();
					Variance variance = new Variance();
					variance.accept(logReturn);
					stats.sd = doubleOrElse(variance.standardDeviation(), -1);
				}
				stats.hv = doubleOrElse(new HV().applyAsDouble(closeArrayY1), -1);
			} else {
				stats.sd = -1;
				stats.hv = -1;
			}
		}
		
		// rsi
		{
			int duration = 14;
			stats.rsi14 = duration <= stats.pricec ? doubleOrElse(new RSI(duration).applyAsDouble(closeArrayY1), -1) : -1;
		}
		{
			int duration = 7;
			stats.rsi7 = duration <= stats.pricec ? doubleOrElse(new RSI(duration).applyAsDouble(closeArrayY1), -1) : -1;
		}
		
		// min max
		{
			var min = Arrays.stream(priceArrayY1).mapToDouble(o -> o.low.doubleValue()).min().getAsDouble();
			var max = Arrays.stream(priceArrayY1).mapToDouble(o -> o.high.doubleValue()).max().getAsDouble();
			
			stats.min = (stats.price - min) / stats.price;
			stats.max = (max - stats.price) / stats.price;
		}
		{
			var min = Arrays.stream(priceArrayY3).mapToDouble(o -> o.low.doubleValue()).min().getAsDouble();
			var max = Arrays.stream(priceArrayY3).mapToDouble(o -> o.high.doubleValue()).max().getAsDouble();
			
			stats.minY3 = (stats.price - min) / stats.price;
			stats.maxY3 = (max - stats.price) / stats.price;
		}
		
		// dividend
		{
			stats.divc     = divArrayY1.length;
			
			if (divArrayY1.length == 0) {
				stats.lastDiv       = 0;
				stats.forwardYield  = 0;
				stats.trailingYield = 0;
				stats.annualDiv     = 0;
			} else {
				stats.lastDiv      = divArrayY1[divArrayY1.length - 1].value.doubleValue();
				stats.forwardYield = (stats.lastDiv * stats.divc) / stats.price;
				
				stats.annualDiv     = Arrays.stream(divArrayY1).mapToDouble(o -> o.value.doubleValue()).sum();
				stats.trailingYield = stats.annualDiv / stats.price;
			}
		}
		
		// volume
		{
			int length = volArrayY1.length;
			
			stats.vol = (long)volArrayY1[length - 1];
			
			{
				int duration = 5;
				stats.vol5  = (duration <= stats.pricec) ? (long)new Mean().applyAsDouble(volArrayY1, length - duration, length) : -1;
			}
			{
				int duration = 21;
				stats.vol21 = (duration <= stats.pricec) ? (long)new Mean().applyAsDouble(volArrayY1, length - duration, length) : -1;
			}
		}
		
		return stats;
	}
	
	
	// current date, price and volume
	public LocalDate date;
	public int       pricec;
	public double    price;
	public double	 last;
	
	public double rorPrice;
	public double rorReinvested;
	public double rorNoReinvested;
	
	// stats - sd hv rsi
	//  30 < pricec
	public double sd;
	public double hv;
	
	public double rsi14;
	public double rsi7;
	
	// min max
	public double min;
	public double max;
	public double minY3;
	public double maxY3;
	
	// dividend
	public int    divc;
	public double lastDiv;
	public double forwardYield;
	public double annualDiv;
	public double trailingYield;

	// volume
	public long   vol;
	// 5 <= pricec
	public long   vol5;
	// 20 <= pricec
	public long   vol21;
	
}
