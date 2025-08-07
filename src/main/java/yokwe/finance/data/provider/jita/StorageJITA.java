package yokwe.finance.data.provider.jita;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.finance.data.type.DailyValue;
import yokwe.finance.data.type.FundInfoJP;
import yokwe.finance.data.type.FundPriceJP;
import yokwe.finance.data.type.NISAInfo;
import yokwe.util.Storage;

public class StorageJITA {
	public static final Storage storage = StorageProvider.storage.getStorage("jita");
	
	// fund-info
	public static final Storage.LoadSaveFileList<FundInfoJP>
		FundInfo = new Storage.LoadSaveFileList<FundInfoJP>(FundInfoJP.class, storage, "fund-info.csv");
	
	// fund-div-price
	public static final Storage.LoadSaveDirectoryList<FundDivPrice>
		FundDivPrice = new Storage.LoadSaveDirectoryList<FundDivPrice>(FundDivPrice.class, storage, "fund-div-price", o -> o + ".csv");
	
	// fund-div
	public static final Storage.LoadSaveDirectoryList<DailyValue>
		FundDiv = new Storage.LoadSaveDirectoryList<DailyValue>(DailyValue.class, storage, "fund-div", o -> o + ".csv");
	
	// fund-price
	public static final Storage.LoadSaveDirectoryList<FundPriceJP>
		FundPrice = new Storage.LoadSaveDirectoryList<FundPriceJP>(FundPriceJP.class, storage, "fund-price", o -> o + ".csv");
	
	// nisa-info
	public static final Storage.LoadSaveFileList<NISAInfo>
		NISAInfo = new Storage.LoadSaveFileList<NISAInfo>(NISAInfo.class, storage, "nisa-info.csv");
	// listed_fund_for_investor
	public static final Storage.LoadSaveFileList<ListedFundForInvestor>
		ListedFundForInvestor = new Storage.LoadSaveFileList<ListedFundForInvestor>(ListedFundForInvestor.class,  storage, "listed-fund-for-investor.csv");
	// unlisted_fund_for_investor
	public static final Storage.LoadSaveFileList<UnlistedFundForInvestor>
		UnlistedFundForInvestor = new Storage.LoadSaveFileList<UnlistedFundForInvestor>(UnlistedFundForInvestor.class,  storage, "unlisted-fund-for-investor.csv");
}
