package yokwe.finance.data.provider.nyse;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.finance.data.type.StockCodeNameUS;
import yokwe.util.Storage;

public class StorageNYSE {
	public static final Storage storage = StorageProvider.storage.getStorage("nyse");
	
	// stock-code-name
	public static final Storage.LoadSaveFileList<StockCodeNameUS>
		StockCodeName = new Storage.LoadSaveFileList<StockCodeNameUS>(StockCodeNameUS.class, storage, "stock-code-name.csv");
	
	// stock-info-nasdaq
	public static final Storage.LoadSaveFileList<Filter>
		Filter = new Storage.LoadSaveFileList<Filter>(Filter.class,  storage, "filter.csv");
}
