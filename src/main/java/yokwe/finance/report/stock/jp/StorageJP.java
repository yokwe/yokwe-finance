package yokwe.finance.report.stock.jp;

import yokwe.finance.report.stock.StorageStock;
import yokwe.util.Storage;

public class StorageJP {
	public static final Storage storage = StorageStock.storage.getStorage("jp");
	
	public static final Storage.LoadSaveDirectoryString
		Report = new Storage.LoadSaveDirectoryString(StorageStock.storage, "jp", o -> "report-" + o + ".ods");
	
	public static final Storage.LoadSaveFileList<ReportForm>
		ReportCSV = new Storage.LoadSaveFileList<ReportForm>(ReportForm.class, storage, "report.csv");
}
