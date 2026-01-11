package yokwe.finance.data.provider.click;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.finance.data.type.TradingFund;
import yokwe.util.Storage;
import yokwe.util.Storage.LoadSaveFileList;

public class StorageClick {
	public static final Storage storage = StorageProvider.storage.getStorage("click");
	
	// trading-fund-jp
	public static final Storage.LoadSaveFileList<TradingFund> TradingFundJP =
		new Storage.LoadSaveFileList<TradingFund>(TradingFund.class, storage, "trading-fund-jp.csv");
	
	// fund
	public static final LoadSaveFileList<UpdateTradingFundJP.FundList> FundListJSON =
		new Storage.LoadSaveFileList<UpdateTradingFundJP.FundList>(UpdateTradingFundJP.FundList.class, storage, "fund-list.json");	
}
