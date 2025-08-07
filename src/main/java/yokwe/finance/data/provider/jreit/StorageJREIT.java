package yokwe.finance.data.provider.jreit;

import java.util.function.Function;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.finance.data.type.DailyValue;
import yokwe.util.Storage;

public class StorageJREIT {
	public static final Storage storage = StorageProvider.storage.getStorage("jreit");
	
	// jreit-div
	public static final Storage.LoadSaveDirectoryList<DailyValue> JREITDiv =
		new Storage.LoadSaveDirectoryList<DailyValue>(DailyValue.class, storage, "jreit-div", o -> o + ".csv");
	
	// jreit-info
	public static final Storage.LoadSaveFileList<JREITInfo> JREITInfo =
		new Storage.LoadSaveFileList<JREITInfo>(JREITInfo.class, storage, "jreit-info.csv");
	
	// jreit-info
	public static final Storage.LoadSaveDirectoryString Page =
		new Storage.LoadSaveDirectoryString(storage, "page", Function.identity());
}
