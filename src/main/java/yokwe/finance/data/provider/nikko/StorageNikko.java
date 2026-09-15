package yokwe.finance.data.provider.nikko;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.finance.data.type.TradingFund;
import yokwe.util.Storage;

public class StorageNikko {
	public static final Storage storage = StorageProvider.storage.getStorage("nikko");

	// trading-fund-jp
	public static final Storage.LoadSaveFileList<TradingFund> TradingFundJPNikko =
		new Storage.LoadSaveFileList<TradingFund>(TradingFund.class, storage, "trading-fund-jp-nikko.csv");

	// courcedata.csv
	public static final Storage.LoadSaveFileString CourceData =
		new Storage.LoadSaveFileString(storage, "coursedata.csv");

	// fundList.json
	public static final Storage.LoadSaveFileString FundListJSON =
		new Storage.LoadSaveFileString(storage, "fund-list.json");
	public static final Storage.LoadSaveFileString FundListString =
		new Storage.LoadSaveFileString(storage, "fund-list.string");

	public static final Storage.LoadSaveFileList<FundInfoNikko> FundInfoNikko =
		new Storage.LoadSaveFileList<FundInfoNikko>(FundInfoNikko.class, storage, "fund-info-nikko.csv");

}
