package yokwe.finance.data.provider.yahoo;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.finance.data.type.CompanyInfo;
import yokwe.util.Storage;

public class StorageYahoo {
	public static final Storage storage = StorageProvider.storage.getStorage("yahoo");
	
	// CompanyInfoJP
	public static final Storage.LoadSaveFileList<CompanyInfo> CompanyInfoJP =
		new Storage.LoadSaveFileList<CompanyInfo>(CompanyInfo.class, storage, "company-info-jp.csv");
	
	// CompanyInfoUS
	public static final Storage.LoadSaveFileList<CompanyInfo> CompanyInfoUS =
		new Storage.LoadSaveFileList<CompanyInfo>(CompanyInfo.class, storage, "company-info-us.csv");
}
