package yokwe.finance.data.provider.sony;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.finance.data.type.TradingFund;
import yokwe.util.Storage;

public class StorageSony {
	public static final Storage storage = StorageProvider.storage.getStorage("sony");
	
	// trading-fund-sony
	public static final Storage.LoadSaveFileList<TradingFund> TradingFundJP =
		new Storage.LoadSaveFileList<TradingFund>(TradingFund.class, storage, "trading-fund-jp.csv");

	// fund-list.json
	public static final Storage.LoadSaveFileString FundListJSON =
		new Storage.LoadSaveFileString(storage, "fund-list.json");
}
