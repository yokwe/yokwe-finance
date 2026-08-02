package yokwe.finance.data.provider.rakuten;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.finance.data.type.TradingFund;
import yokwe.finance.data.type.TradingStock;
import yokwe.util.Storage;

public class StorageRakuten {
	public static final Storage storage = StorageProvider.storage.getStorage("rakuten");

	// trading-fund-rakuten
	public static final Storage.LoadSaveFileList<TradingFund> TradingFundJPRakuten =
		new Storage.LoadSaveFileList<TradingFund>(TradingFund.class, storage, "trading-fund-jp-rakuten.csv");
	// trading-stock-rakuten
	public static final Storage.LoadSaveFileList<TradingStock> TradingStockUSRakuten =
		new Storage.LoadSaveFileList<TradingStock>(TradingStock.class, storage, "trading-stock-us-rakuten.csv");

	// reloadscreener
	public static final Storage.LoadSaveFileString ReloadScreener =
		new Storage.LoadSaveFileString(storage, "reloadscreener.json");

	// export-csv-us
	public static final Storage.LoadSaveFileString ExportCSVUS =
		new Storage.LoadSaveFileString(storage, "exportcsvus.csv");
	// export-csv-us
	public static final Storage.LoadSaveFileString ETFD =
		new Storage.LoadSaveFileString(storage, "etfd.csv");


}
