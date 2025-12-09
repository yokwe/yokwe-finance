package yokwe.finance.report.stock.jp;

import yokwe.finance.report.stock.StorageStock;
import yokwe.util.Storage;

public class StorageJP {
	public static final Storage storage = StorageStock.storage.getStorage("jp");
	
	public static final Storage.LoadSaveFile
		Report = new Storage.LoadSaveFile(storage, "report.ods");
}
