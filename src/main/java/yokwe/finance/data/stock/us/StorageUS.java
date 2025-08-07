package yokwe.finance.data.stock.us;

import yokwe.finance.data.stock.StorageStock;
import yokwe.finance.data.type.DailyValue;
import yokwe.finance.data.type.OHLCV;
import yokwe.finance.data.type.StockCodeNameUS;
import yokwe.finance.data.type.StockInfoUS;
import yokwe.util.Storage;

public class StorageUS {
	public static final Storage storage = StorageStock.storage.getStorage("us");
	
	public static final Storage.LoadSaveFileList<StockCodeNameUS>
		StockCodeName    = new Storage.LoadSaveFileList<StockCodeNameUS>(StockCodeNameUS.class, storage, "stock-code-name.csv");
	public static final Storage.LoadSaveFileList<StockInfoUS>
		StockInfo        = new Storage.LoadSaveFileList<StockInfoUS>(StockInfoUS.class, storage, "stock-info.csv");
	
	// stock-div
	public static final Storage.LoadSaveDirectoryList<DailyValue>
		StockDiv = new Storage.LoadSaveDirectoryList<DailyValue>(DailyValue.class, storage, "stock-div", o -> o + ".csv");
	// stock-price
	public static final Storage.LoadSaveDirectoryList<OHLCV>
		StockPriceOHLCV = new Storage.LoadSaveDirectoryList<OHLCV>(OHLCV.class, storage, "stock-price-ohlcv", o -> o + ".csv");
}
