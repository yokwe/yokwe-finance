package yokwe.finance.data.fund.jp;

import yokwe.finance.data.fund.StorageFund;
import yokwe.finance.data.provider.jita.StorageJITA;
import yokwe.finance.data.type.DailyValue;
import yokwe.finance.data.type.FundInfoJP;
import yokwe.finance.data.type.FundPriceJP;
import yokwe.finance.data.type.NISAInfo;
import yokwe.util.Storage;

public class StorageJP {
	public static final Storage storage = StorageFund.storage.getStorage("jp");
	
	// fund-info
	public static final Storage.LoadSaveFileList<FundInfoJP>
		FundInfo = StorageJITA.FundInfo;
	
	// fund-div
	public static final Storage.LoadSaveDirectoryList<DailyValue>
		FundDiv = StorageJITA.FundDiv;
	
	// fund-price
	public static final Storage.LoadSaveDirectoryList<FundPriceJP>
		FundPrice = StorageJITA.FundPrice;
	
	// nisa-info
	public static final Storage.LoadSaveFileList<NISAInfo>
		NISAInfo = StorageJITA.NISAInfo;

}
