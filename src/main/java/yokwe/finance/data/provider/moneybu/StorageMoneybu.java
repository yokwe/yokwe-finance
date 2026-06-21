package yokwe.finance.data.provider.moneybu;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.util.Storage;

public class StorageMoneybu {
	public static final Storage storage = StorageProvider.storage.getStorage("moneybu");

	// list-0-json
	public static final Storage.LoadSaveFileString LIST_0_JSON =
		new Storage.LoadSaveFileString(storage, "list-0.json");
	// list-1-json
	public static final Storage.LoadSaveFileString LIST_1_JSON =
		new Storage.LoadSaveFileString(storage, "list-1.json");

	// stock-list
	public static final Storage.LoadSaveFileList<StockList> StockList =
		new Storage.LoadSaveFileList<StockList>(StockList.class, storage, "stock-list.csv");

	// stock-info-json
	public static final Storage.LoadSaveDirectoryString StockInfoJSON =
		new Storage.LoadSaveDirectoryString(storage, "stock-info-json", o -> o + ".json");

	// stock-list
	public static final Storage.LoadSaveFileList<StockInfo> StockInfo =
		new Storage.LoadSaveFileList<StockInfo>(StockInfo.class, storage, "stock-info.csv");

}
