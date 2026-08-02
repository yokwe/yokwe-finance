package yokwe.finance.data.provider.smtb;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.finance.data.type.TradingFund;
import yokwe.util.Storage;

public class StorageSMTB {
	public static final Storage storage = StorageProvider.storage.getStorage("smtb");

	// trading-fund-smtb
	public static final Storage.LoadSaveFileList<TradingFund> TradingFundJPSMTB =
		new Storage.LoadSaveFileList<TradingFund>(TradingFund.class, storage, "trading-fund-jp-smtb.csv");

	// webpage
	public static final Storage.LoadSaveDirectoryString WebPage =
		new Storage.LoadSaveDirectoryString(storage, "webpage", o -> o + ".html");

}
