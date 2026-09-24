package yokwe.finance.data.provider.nikkei;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.finance.data.type.FundDivInfo;
import yokwe.util.Storage;

public class StorageNikkei {
	public static final Storage storage = StorageProvider.storage.getStorage("nikkei");

	// fund div info
	public static final Storage.LoadSaveFileList<FundDivInfo> FundDivInfo =
		new Storage.LoadSaveFileList<FundDivInfo>(FundDivInfo.class, storage, "fund-div-info.csv");

	// webpage
	public static final Storage.LoadSaveDirectoryString Webpage =
		new Storage.LoadSaveDirectoryString(storage, "webpage", o -> o + ".html");
}
