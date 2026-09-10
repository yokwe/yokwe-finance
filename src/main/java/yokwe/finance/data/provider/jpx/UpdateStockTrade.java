package yokwe.finance.data.provider.jpx;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import yokwe.finance.data.type.StockCodeJP;
import yokwe.finance.data.type.StockTradeJP;
import yokwe.util.FileUtil;
import yokwe.util.Makefile;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.json.JSON;
import yokwe.util.update.UpdateBase;

public class UpdateStockTrade extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static Makefile MAKEFILE = Makefile.builder().
		input(StorageJPX.StockIntraJSON).
		output(StorageJPX.StockTradeJPX).
		build();

	public static void main(String[] args) {
		callUpdate();
	}

	@Override
	public void update() {
		var stockCodeList = UpdateStockDetailJSON.getJSONFileList().stream().map(o -> o.getName().replace(".json", "")).collect(Collectors.toList());


		var fileList = Arrays.asList(StorageJPX.StockIntraJSON.getDir().listFiles(o -> o.getName().endsWith(".json")));

		logger.info("stockCodeList  {}", stockCodeList.size());

		var stockTradeList = new ArrayList<StockTradeJP>();

		for(var file: fileList) {
//			logger.info("file  {}  {}", file.canRead(), file.toPath());

			var string = FileUtil.read().file(file);
//			logger.info("string  {}", string.length());

			var json = JSON.unmarshal(StockIntra.class, string);

			var data = json.section1.data.values().stream().toList().getFirst();
			if (data.HISTMDATE1.isEmpty()) {
				// no data
				continue;
			}

			var stockCode = StockCodeJP.toStockCode5(data.TTCODE.replace("/T", ""));

			var liquidityMap = new TreeMap<LocalDate, Integer>();

			var histList = new ArrayList<History>();

			if (!data.HISTMDATE1.isEmpty()) {
				var date = data.HISTMDATE1;
				var hist = data.HISTMIN1;

				var list = toHistoryList(hist);
				liquidityMap.put(toLocalDate(date), list.size());
				histList.addAll(list);
			}
			if (!data.HISTMDATE2.isEmpty()) {
				var date = data.HISTMDATE2;
				var hist = data.HISTMIN2;

				var list = toHistoryList(hist);
				liquidityMap.put(toLocalDate(date), list.size());
				histList.addAll(list);
			}
			if (!data.HISTMDATE3.isEmpty()) {
				var date = data.HISTMDATE3;
				var hist = data.HISTMIN3;

				var list = toHistoryList(hist);
				liquidityMap.put(toLocalDate(date), list.size());
				histList.addAll(list);
			}
			if (!data.HISTMDATE4.isEmpty()) {
				var date = data.HISTMDATE4;
				var hist = data.HISTMIN4;

				var list = toHistoryList(hist);
				liquidityMap.put(toLocalDate(date), list.size());
				histList.addAll(list);
			}
			if (!data.HISTMDATE5.isEmpty()) {
				var date = data.HISTMDATE5;
				var hist = data.HISTMIN5;

				var list = toHistoryList(hist);
				liquidityMap.put(toLocalDate(date), list.size());
				histList.addAll(list);
			}
			if (!data.HISTMDATE6.isEmpty()) {
				var date = data.HISTMDATE6;
				var hist = data.HISTMIN6;

				var list = toHistoryList(hist);
				liquidityMap.put(toLocalDate(date), list.size());
				histList.addAll(list);
			}
			//
			if (!data.HISTMDATE7.isEmpty()) {
				var date = data.HISTMDATE7;
				var hist = data.HISTMIN7;

				var list = toHistoryList(hist);
				liquidityMap.put(toLocalDate(date), list.size());
				histList.addAll(list);
			}
			if (!data.HISTMDATE8.isEmpty()) {
				var date = data.HISTMDATE8;
				var hist = data.HISTMIN8;

				var list = toHistoryList(hist);
				liquidityMap.put(toLocalDate(date), list.size());
				histList.addAll(list);
			}
			if (!data.HISTMDATE9.isEmpty()) {
				var date = data.HISTMDATE9;
				var hist = data.HISTMIN9;

				var list = toHistoryList(hist);
				liquidityMap.put(toLocalDate(date), list.size());
				histList.addAll(list);
			}
			if (!data.HISTMDATE10.isEmpty()) {
				var date = data.HISTMDATE10;
				var hist = data.HISTMIN10;

				var list = toHistoryList(hist);
				liquidityMap.put(toLocalDate(date), list.size());
				histList.addAll(list);
			}

			long volume = 0;
			long value  = 0;

			for(var e: histList) {
				var v = e.volume.longValue();
				volume += v;
				value  += v * e.close.doubleValue();
			}

			var tradeUnit = Long.parseLong(data.LOSH);

			var days = liquidityMap.size();

			BigDecimal liquidity;
			{
				var sum = liquidityMap.values().stream().mapToInt(o -> o).sum();
				liquidity = new BigDecimal(sum).divide(new BigDecimal(days), 2, RoundingMode.HALF_EVEN);
			}

			var dateFirst = liquidityMap.firstKey();
			var dateLast  = liquidityMap.lastKey();

			var stockTrade = new StockTradeJP();
			stockTrade.stockCode  = stockCode;
			stockTrade.dateFirst  = dateFirst;
			stockTrade.dateLast   = dateLast;
			stockTrade.days       = days;
			stockTrade.liquidity  = liquidity;
			stockTrade.volume     = volume / days;
			stockTrade.units      = volume / (days * tradeUnit);
			stockTrade.value      = value  / days;

			stockTradeList.add(stockTrade);
		}

		// fix liquidity
		if (!stockTradeList.isEmpty()) {
			var max = stockTradeList.stream().map(o -> o.liquidity).max(BigDecimal::compareTo).get();
			logger.info("max liquidity  {}", max.toPlainString());
			for(var e: stockTradeList) {
				e.liquidity = e.liquidity.divide(max, 2, RoundingMode.HALF_EVEN);
			}
		}

		logger.info("save  {}  {}", stockTradeList.size(), StorageJPX.StockTradeJPX.getFile());
		StorageJPX.StockTradeJPX.save(stockTradeList);
	}

	public static LocalDate toLocalDate(String string) {
		return LocalDate.parse(string, DATE_FORMAT);
	}
	private static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/M/d");

	public static BigDecimal toBigDecimal(String string) {
		if (string.isEmpty()) {
			return BigDecimal.ZERO;
		}
		return new BigDecimal(string);
	}

	public static class History implements Comparable<History> {
		public final LocalTime  time;    // "09:00",
		public final BigDecimal open;    // "430.0",
		public final BigDecimal close;   // "430.0",
		public final BigDecimal high;    // "430.5",
		public final BigDecimal low;     // "429.7",
		public final BigDecimal vwap;    // "430.0262",
		public final BigDecimal volume;  // "912740"

		public History(String time, String open, String close, String high, String low, String vwap, String volume) {
			this.time   = LocalTime.parse(time);
			this.open   = new BigDecimal(open);
			this.close  = new BigDecimal(close);
			this.high   = new BigDecimal(high);
			this.low    = new BigDecimal(low);
			this.vwap   = new BigDecimal(vwap);
			this.volume = new BigDecimal(volume);
		}

		public static History getInstance(String[] stringArray) {
			if (stringArray.length != 7) {
				logger.error("Unexpected stringArray");
				logger.error("  stringArray  {}", stringArray.length);
				for(var e: stringArray) {
					logger.info("  {}!", e);
				}
				throw new UnexpectedException("Unexpected stringArray");
			}
			return new History(stringArray[0], stringArray[1], stringArray[2], stringArray[3], stringArray[4], stringArray[5], stringArray[6]);
		}
		public static History getInstance(String string) {
			var stringArray = string.split(",");
			if (stringArray.length != 7) {
				logger.error("Unexpected string");
				logger.error("  string  {}!", string);
				throw new UnexpectedException("Unexpected stringArray");
			}
			return new History(stringArray[0], stringArray[1], stringArray[2], stringArray[3], stringArray[4], stringArray[5], stringArray[6]);
		}

		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}

		@Override
		public int compareTo(History that) {
			return this.time.compareTo(that.time);
		}
	}

	public static List<History> toHistoryList(String[][] stringArray) {
		var list = new ArrayList<History>();

		for(var e: stringArray) {
			list.add(History.getInstance(e));
		}

		Collections.sort(list);
		return list;
	}
	public static List<History> toHistoryList(String string) {
		var list = new ArrayList<History>();

		if (!string.isEmpty()) {
			for(var e: string.split("\\\\n")) {
				list.add(History.getInstance(e));
			}

			Collections.sort(list);
		}

		return list;
	}
}
