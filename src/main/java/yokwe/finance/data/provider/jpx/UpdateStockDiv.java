package yokwe.finance.data.provider.jpx;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.finance.data.type.DailyValue;
import yokwe.util.UnexpectedException;
import yokwe.util.json.JSON;
import yokwe.util.makefile.Makefile;
import yokwe.util.update.UpdateBase;

public class UpdateStockDiv extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input(StorageJPX.KessanJSON).
		output(StorageJPX.StockDiv).
		build();
		
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		var stockCodeList = UpdateKessanJSON.getJSONFileList().stream().map(o -> o.getName().replace(".json", "")).toList();
		StorageJPX.StockDiv.delistUnknownFile(stockCodeList);
		
		int count     = 0;
		int countSkip = 0;
		for(var stockCode: stockCodeList) {
			if ((++count % 1000) == 1) logger.info("{}  /  {}", count, stockCodeList.size());
//			logger.info("{}  /  {}  {}", ++count, stockListSize, stock.stockCode);
			var string = StorageJPX.KessanJSON.load(stockCode);
			// sanity check
			if (string.contains("<html>")) {
				logger.error("Unexpected string");
				logger.error("  stockCode  {}", stockCode);
				throw new UnexpectedException("Unexpected string");
			}
			
			Map<LocalDate, BigDecimal> map = StorageJPX.StockDiv.getList(stockCode).stream().collect(Collectors.toMap(o -> o.date, o -> o.value));
			
			var kessan = JSON.unmarshal(Kessan.class, string);
			if (kessan.isEmpty()) {
				countSkip++;
			} else {
				for(var e: kessan.section1.data) {
					if (e.ALK_DIVDH.equals("-")) continue;
					//
					var date     = toLocalDate(e.ALK_EDDATEM);
					var newValue = new BigDecimal(e.ALK_DIVDH.replaceAll(",", ""));
					var oldValue = map.put(date, newValue);
					if (oldValue == null) {
						// new
					} else {
						// old
						if (oldValue.compareTo(newValue) == 0) {
							// same value
						} else {
							// not same value
							logger.warn("value changed  {}  {}  {}  {}", date, oldValue, newValue, stockCode);
						}
					}
				}
			}
			{
				var list = map.entrySet().stream().map(o -> new DailyValue(o.getKey(), o.getValue())).collect(Collectors.toList());
//				logger.info("save  {}  {}", list.size(), StorageJPX.StockDivJPX.getPath(stockCode));
				StorageJPX.StockDiv.save(stockCode, list);
			}
		}
		
		logger.info("countSkip   {}", countSkip);
		logger.info("count       {}", count);
		
		// touch file
		StorageJPX.StockDiv.touch();
	}
	private LocalDate toLocalDate(String string) {
		String[] token = string.split("/");
		if (token.length == 2) {
			int yyyy = Integer.valueOf(token[0]);
			int mm   = Integer.valueOf(token[1]);
			var date = LocalDate.of(yyyy, mm, 1);
			return date.with(TemporalAdjusters.lastDayOfMonth());
		}
		logger.error("Unexpected string");
		logger.error("  {}!", string);
		throw new UnexpectedException("Unexpected string");
	}
}
