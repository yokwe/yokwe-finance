package yokwe.finance.data.stock.jp;

import yokwe.finance.data.provider.jpx.StorageJPX;
import yokwe.finance.data.stock.StorageStock;
import yokwe.finance.data.type.DailyValue;
import yokwe.finance.data.type.OHLCV;
import yokwe.finance.data.type.StockInfoJP;
import yokwe.finance.data.type.StockValueJP;
import yokwe.util.Storage;

public class StorageJP {
	public static final Storage storage = StorageStock.storage.getStorage("jp");
	
	public static final Storage.LoadSaveFileList<StockInfoJP>
		StockInfo       = new Storage.LoadSaveFileList<StockInfoJP>    (StockInfoJP.class, storage, "stock-info.csv");
	public static final Storage.LoadSaveDirectoryList<OHLCV>
		StockPriceOHLCV = StorageJPX.StockPriceOHLCV;
	public static final Storage.LoadSaveDirectoryList<DailyValue>
		StockPrice      = new Storage.LoadSaveDirectoryList<DailyValue>(DailyValue.class,  storage, "stock-price", o -> o + ".csv");
	public static final Storage.LoadSaveDirectoryList<DailyValue>
		StockDiv        = new Storage.LoadSaveDirectoryList<DailyValue>(DailyValue.class,  storage, "stock-div", o -> o + ".csv");
	public static final Storage.LoadSaveFileList<StockValueJP>
		StockValue      = StorageJPX.StockValue;
}
