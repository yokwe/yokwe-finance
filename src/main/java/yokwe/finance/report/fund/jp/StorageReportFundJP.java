package yokwe.finance.report.fund.jp;

import yokwe.finance.report.fund.StorageFund;
import yokwe.util.Storage;

public class StorageReportFundJP {
	public static final Storage storage = StorageFund.storage.getStorage("jp");

	public static final Storage.LoadSaveFile
		ReportODS = new Storage.LoadSaveFile(storage, "report-fund-jp.ods");
	public static final Storage.LoadSaveFile
		ReportCSV = new Storage.LoadSaveFile(storage, "report-fund-jp.csv");
}
