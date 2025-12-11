package yokwe.finance.data.provider.nikkei;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.finance.data.type.FundDivScore;
import yokwe.util.Storage;

public class StorageNikkei {
	public static final Storage storage = StorageProvider.storage.getStorage("nikkei");
	
	// trading-fund-jp
	public static final Storage.LoadSaveFileList<FundDivScore> FundDivScore =
		new Storage.LoadSaveFileList<FundDivScore>(FundDivScore.class, storage, "fund-div-score.csv");
	
	// webpage
	public static final Storage.LoadSaveDirectoryString WebPage =
		new Storage.LoadSaveDirectoryString(storage, "webpage", o -> o + ".html");
}
