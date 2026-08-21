package yokwe.finance.data.stock.us;

import yokwe.finance.data.stock.StorageStock;
import yokwe.finance.data.type.DailyValue;
import yokwe.finance.data.type.OHLCV;
import yokwe.finance.data.type.StockCodeNameUS;
import yokwe.finance.data.type.StockInfoUS;
import yokwe.finance.data.type.StockStatsUS;
import yokwe.util.Storage;

public class StorageStockUS {
	public static final Storage storage = StorageStock.storage.getStorage("us");

	public static final Storage.LoadSaveFileList<StockCodeNameUS>
		StockCodeNameUS    = new Storage.LoadSaveFileList<StockCodeNameUS>(StockCodeNameUS.class, storage, "stock-code-name-us.csv");

	public static final Storage.LoadSaveDirectoryString
		Quotes = new Storage.LoadSaveDirectoryString(storage, "quotes", o -> o + ".json");

	public static final Storage.LoadSaveFileList<StockInfoUS>
		StockInfoUS        = new Storage.LoadSaveFileList<StockInfoUS>(StockInfoUS.class, storage, "stock-info-us.csv");

	// stock-div
	public static final Storage.LoadSaveDirectoryList<DailyValue>
		StockDiv = new Storage.LoadSaveDirectoryList<DailyValue>(DailyValue.class, storage, "stock-div", o -> o + ".csv");
	// stock-price
	public static final Storage.LoadSaveDirectoryList<OHLCV>
		StockPriceOHLCV = new Storage.LoadSaveDirectoryList<OHLCV>(OHLCV.class, storage, "stock-price-ohlcv", o -> o + ".csv");

	// stock-stats
	public static final Storage.LoadSaveFileList<StockStatsUS>
		StockStatsUS = new Storage.LoadSaveFileList<StockStatsUS>(StockStatsUS.class, storage, "stock-stats-us.csv");
}
