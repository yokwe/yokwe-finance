package yokwe.finance.data.provider.webull;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.finance.data.type.StockCodeNameUS;
import yokwe.util.Storage;

public class StorageWebull {
	public static final Storage storage = StorageProvider.storage.getStorage("webull");

	public static final Storage.LoadSaveFileList<StockInfo>
		StockInfoWebull = new Storage.LoadSaveFileList<StockInfo>(StockInfo.class, storage, "stock-info-webull.csv");

	public static final Storage.LoadSaveFileList<StockCodeNameUS>
		TradingStockUSWebull = new Storage.LoadSaveFileList<StockCodeNameUS>(StockCodeNameUS.class, storage, "stock-code-name-webull.csv");
}
