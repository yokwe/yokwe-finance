package yokwe.finance.report.stock.us;

import yokwe.finance.report.stock.StorageStock;
import yokwe.util.Storage;

public class StorageUS {
	public static final Storage storage = StorageStock.storage.getStorage("us");
	
	public static final Storage.LoadSaveFile
		Report = new Storage.LoadSaveFile(storage, "report.ods");
}
