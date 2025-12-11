package yokwe.finance.data.provider.nikko;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.finance.data.type.TradingFund;
import yokwe.util.Storage;

public class StorageNikko {
	public static final Storage storage = StorageProvider.storage.getStorage("nikko");
	
	// trading-fund-jp
	public static final Storage.LoadSaveFileList<TradingFund> TradingFundJP =
		new Storage.LoadSaveFileList<TradingFund>(TradingFund.class, storage, "trading-fund-jp.csv");
	
	// courcedata.csv
	public static final Storage.LoadSaveFileString CourceData =
		new Storage.LoadSaveFileString(storage, "coursedata.csv");
	
}
