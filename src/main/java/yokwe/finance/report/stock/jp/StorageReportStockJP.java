package yokwe.finance.report.stock.jp;

import yokwe.finance.report.stock.StorageStock;
import yokwe.util.Storage;

public class StorageReportStockJP {
	public static final Storage storage = StorageStock.storage.getStorage("jp");

	public static final Storage.LoadSaveFile
		ReportODS = new Storage.LoadSaveFile(storage, "report-stock-jp.ods");
	public static final Storage.LoadSaveFile
		ReportCSV = new Storage.LoadSaveFile(storage, "report-stock-jp.csv");
}
