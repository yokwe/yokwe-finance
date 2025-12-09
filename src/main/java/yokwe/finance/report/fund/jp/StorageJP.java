package yokwe.finance.report.fund.jp;

import yokwe.finance.report.fund.StorageFund;
import yokwe.util.Storage;

public class StorageJP {
	public static final Storage storage = StorageFund.storage.getStorage("jp");

	public static final Storage.LoadSaveFile
		Report = new Storage.LoadSaveFile(storage, "report.ods");
}
