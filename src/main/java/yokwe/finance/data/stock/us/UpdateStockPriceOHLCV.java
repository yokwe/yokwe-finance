package yokwe.finance.data.stock.us;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.nyse.Quotes;
import yokwe.finance.data.provider.nyse.Quotes.QuoteHistory;
import yokwe.finance.data.type.OHLCV;
import yokwe.util.FileUtil;
import yokwe.util.Makefile;
import yokwe.util.json.JSON;
import yokwe.util.update.UpdateBase;

public class UpdateStockPriceOHLCV extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	protected static Makefile MAKEFILE = Makefile.builder().
		input(StorageUS.StockInfo, StorageUS.Quotes).
		output(StorageUS.StockPriceOHLCV).
		build();

	public static void main(String[] args) throws IOException {
		callUpdate();
	}

	@Override
	public void update() {
		var stockInfoList = StorageUS.StockInfo.getList();
		logger.info("stockInfoList  {}", stockInfoList.size());

		// delist unknonwn
		{
			logger.info("delist unknown");
			Set<String> validNameSet = stockInfoList.stream().map(o -> o.stockCode).collect(Collectors.toSet());
			StorageUS.StockPriceOHLCV.delistUnknownFile(validNameSet);
		}

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
			var quoteHistory= quotes.quoteHistory;

			if (quoteHistory == null) {
				logger.warn("quoteHistory is null {}  {}", symbol, stockInfo.name);
				continue;
			}

			if (!symbol.equals(quoteHistory.symbol)) {
				logger.warn("symbol not eauals {}  {}", symbol, quoteHistory.symbol);
				continue;
			}

			var ohlcvList = Arrays.stream(quoteHistory.historyList).map(o -> toOHLCV(o)).collect(Collectors.toList());
			StorageUS.StockPriceOHLCV.save(symbol, ohlcvList);
		}

		StorageUS.StockPriceOHLCV.touch();
	}

	private OHLCV toOHLCV(QuoteHistory.History history) {
		LocalDate  date   = LocalDate.parse(history.date, HISTORY_DATE);
		BigDecimal open   = new BigDecimal(history.open);
		BigDecimal high   = new BigDecimal(history.high);
		BigDecimal low    = new BigDecimal(history.low);
		BigDecimal close  = new BigDecimal(history.close);
		long       volume = Long.parseLong(history.volume);
		return new OHLCV(date, open, high, low, close, volume);
	}
	private static DateTimeFormatter HISTORY_DATE = DateTimeFormatter.ofPattern("yyyy/MM/dd"); // 2021/07/14

}
