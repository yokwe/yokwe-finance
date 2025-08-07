package yokwe.finance.data.provider.mizuho;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.finance.data.type.FXRate;
import yokwe.util.Storage;

public class StorageMizuho {
	public static final Storage storage = StorageProvider.storage.getStorage("mizuho");
	
	// quote
	public static final Storage.LoadSaveFileString Quote =
		new Storage.LoadSaveFileString(storage, "quote.csv");
	
	// quote
	public static final Storage.LoadSaveFileList<FXRate> FXRate =
		new Storage.LoadSaveFileList<FXRate>(FXRate.class, storage, "fx-rate.csv");
}
