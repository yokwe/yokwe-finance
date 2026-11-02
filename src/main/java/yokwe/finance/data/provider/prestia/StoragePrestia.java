package yokwe.finance.data.provider.prestia;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.finance.data.type.DailyValue;
import yokwe.util.Storage;

public class StoragePrestia {
	public static final Storage storage = StorageProvider.storage.getStorage("prestia");

	// fund list json
	public static final Storage.LoadSaveFileString FundListJSON =
		new Storage.LoadSaveFileString(storage, "fund-list.json");

	// fund detail json
	public static final Storage.LoadSaveDirectoryString FundDetailJSON =
		new Storage.LoadSaveDirectoryString(storage, "fund-detail", o -> o + ".json");

	// fund data
	public static final Storage.LoadSaveDirectoryString FundDataJSON =
		new Storage.LoadSaveDirectoryString(storage, "fund-data", o -> o + ".json");

	// TradingFundPrestia
	public static final Storage.LoadSaveFileList<TradingFundPrestia>
		TradingFundPrestia = new Storage.LoadSaveFileList<TradingFundPrestia>(TradingFundPrestia.class, storage, "trading-fund-prestia.csv");

	// fund-div
	public static final Storage.LoadSaveDirectoryList<DailyValue>
		FundDiv = new Storage.LoadSaveDirectoryList<DailyValue>(DailyValue.class, storage, "fund-div", o -> o + ".csv");

	// fund-price
	public static final Storage.LoadSaveDirectoryList<DailyValue>
		FundPrice = new Storage.LoadSaveDirectoryList<DailyValue>(DailyValue.class, storage, "fund-price", o -> o + ".csv");

}
