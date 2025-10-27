package yokwe.finance.report.fund.jp;

import yokwe.finance.report.fund.StorageFund;
import yokwe.util.Storage;

public class StorageJP {
	public static final Storage storage = StorageFund.storage.getStorage("jp");

	public static final Storage.LoadSaveDirectoryString
		Report = new Storage.LoadSaveDirectoryString(StorageFund.storage, "jp", o -> "report-" + o + ".ods");

	public static final Storage.LoadSaveFileList<ReportForm>
		ReportCSV = new Storage.LoadSaveFileList<ReportForm>(ReportForm.class, storage, "report.csv");
}
