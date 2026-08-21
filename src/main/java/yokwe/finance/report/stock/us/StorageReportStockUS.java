package yokwe.finance.report.stock.us;

import yokwe.finance.report.stock.StorageStock;
import yokwe.util.Storage;

public class StorageReportStockUS {
	public static final Storage storage = StorageStock.storage.getStorage("us");

	public static final Storage.LoadSaveFile
		ReportODS = new Storage.LoadSaveFile(storage, "report-stock-us.ods");
	public static final Storage.LoadSaveFile
		ReportCSV = new Storage.LoadSaveFile(storage, "report-stock-us.csv");
}
