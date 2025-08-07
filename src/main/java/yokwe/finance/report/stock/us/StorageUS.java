package yokwe.finance.report.stock.us;

import yokwe.finance.report.stock.StorageStock;
import yokwe.util.Storage;

public class StorageUS {
	public static final Storage storage = StorageStock.storage.getStorage("us");
	
	public static final Storage.LoadSaveDirectoryString
		Report = new Storage.LoadSaveDirectoryString(StorageStock.storage, "us", o -> "report-" + o + ".ods");
	
	public static final Storage.LoadSaveFileList<ReportForm>
		ReportCSV = new Storage.LoadSaveFileList<ReportForm>(ReportForm.class, storage, "report.csv");
}
