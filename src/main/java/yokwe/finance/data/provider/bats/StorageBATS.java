package yokwe.finance.data.provider.bats;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.finance.data.type.StockCodeNameUS;
import yokwe.util.Storage;

public class StorageBATS {
	public static final Storage storage = StorageProvider.storage.getStorage("bats");
	
	// stock-code-name
	public static final Storage.LoadSaveFileList<StockCodeNameUS>
		StockCodeName = new Storage.LoadSaveFileList<StockCodeNameUS>(StockCodeNameUS.class, storage, "stock-code-name.csv");
	
	// listed-security-report
	public static final Storage.LoadSaveFileList<ListedSecurityReport> ListedSecurityReport =
		new Storage.LoadSaveFileList<ListedSecurityReport>(ListedSecurityReport.class, storage, "listed-security-report.csv");
}
