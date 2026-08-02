package yokwe.finance.data.stock.us;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import yokwe.finance.data.provider.nyse.Quotes;
import yokwe.finance.data.type.StockStatsUS;
import yokwe.util.FileUtil;
import yokwe.util.Makefile;
import yokwe.util.UnexpectedException;
import yokwe.util.json.JSON;
import yokwe.util.update.UpdateBase;

public class UpdateStockStats extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	protected static Makefile MAKEFILE = Makefile.builder().
		input(StorageUS.StockInfoUS, StorageUS.Quotes).
		output(StorageUS.StockStatsUS).
		build();

	public static void main(String[] args) throws IOException {
		callUpdate();
	}

	@Override
	public void update() {
		var stockInfoList = StorageUS.StockInfoUS.getList();
		logger.info("stockInfoList  {}", stockInfoList.size());

		var list = new ArrayList<StockStatsUS>(stockInfoList.size());

		int count = 0;
		for(var stockInfo: stockInfoList) {
			var symbol = stockInfo.stockCode;

			if ((++count % 1000) == 1) {
				logger.info("{}  /  {}", count - 1, stockInfoList.size());
			} else {
//				logger.info("{}  /  {}", count - 1, stockInfoList.size());
			}

			var file = StorageUS.Quotes.getFile(symbol);
			if (!file.exists()) {
				logger.info("no file  {}", file.getPath());
				continue;
			}

			var string = FileUtil.read().file(file);
			var quotes = JSON.unmarshal(Quotes.class, string);

			if (quotes.totalReturns == null) {
				logger.warn("totalReturn is null  {}  {}", symbol, stockInfo.name);
				continue;
			}
			if (quotes.quote == null) {
				logger.warn("quote is null  {}  {}", symbol, stockInfo.name);
				continue;
			}

			var stockStatus = toStockStatsUS(quotes);
			if (stockStatus != null) {
				list.add(stockStatus);
			}
		}

		StorageUS.StockStatsUS.save(list);
	}

	private StockStatsUS toStockStatsUS(Quotes quotes) {
		var quote       = quotes.quote;
		var totalReturn = quotes.totalReturns;

		StockStatsUS ret = new StockStatsUS();

		ret.stockCode = quote.dispname;
		ret.stockType = SYMBOL_TYPE_MAP.get(totalReturn.symbolType);
		if (ret.stockType == null) {
			logger.error("Unexpected symbolType");
			logger.error("  {}  {}", quote.dispname, totalReturn.symbolType);
			throw new UnexpectedException("Unexpected symbolType");
		}

		// dividend
	    ret.dividend = quote.dividend == null ? BigDecimal.ZERO : new BigDecimal(quote.dividend);

	    ret.divDate  = quote.divDate == null ? UNKNOWN_DATE : LocalDate.parse(quote.divDate, DIV_DATE);
	    ret.divYield = quote.divYield == null ? BigDecimal.ZERO : new BigDecimal(quote.divYield).movePointLeft(2);
	    ret.divInt   = quote.divInt == null ? 0 : Integer.parseInt(quote.divInt);

	    // stats
	    ret.beta       = quote.beta == null ? BigDecimal.ZERO : new BigDecimal(quote.beta);
	    ret.volatility = totalReturn.volatility == null ? BigDecimal.ZERO : new BigDecimal(totalReturn.volatility);
	    ret.rsi        = new BigDecimal(totalReturn.rsi);

	    ret.oneMonth     = new BigDecimal(totalReturn.oneMonth).movePointLeft(2);
	    ret.threeMonth   = new BigDecimal(totalReturn.threeMonth).movePointLeft(2);
	    ret.sixMonth     = new BigDecimal(totalReturn.sixMonth).movePointLeft(2);
	    ret.fiftyTwoWeek = new BigDecimal(totalReturn.fiftyTwoWeek).movePointLeft(2);
	    ret.threeYear    = new BigDecimal(totalReturn.threeYear).movePointLeft(2);

		ret.name = quote.desc;

		return ret;
	}
	private static DateTimeFormatter DIV_DATE     = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy", Locale.US); // Tuesday, June 30, 2026
	private static LocalDate         UNKNOWN_DATE = LocalDate.of(2999, 1, 1);

	private static Map<String, String> SYMBOL_TYPE_MAP = Map.ofEntries(
		Map.entry("American Depository Receipt",   "ADR"),
		Map.entry("ETF",                           "ETF"),
		Map.entry("Shares of Beneficial Interest", "STOCK"),
		Map.entry("Stock",                         "STOCK"),
		Map.entry("Right",                         "RIGHT"),   // VIK is right
		Map.entry("Warrant",                       "WARRANT")  // VIK is warrant
	);

}
