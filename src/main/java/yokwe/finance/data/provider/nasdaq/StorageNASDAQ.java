package yokwe.finance.data.provider.nasdaq;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.finance.data.type.StockCodeNameUS;
import yokwe.util.Storage;

public class StorageNASDAQ {
	public static final Storage storage = StorageProvider.storage.getStorage("nasdaq");
	
	// stock-code-name
	public static final Storage.LoadSaveFileList<StockCodeNameUS>
		StockCodeName = new Storage.LoadSaveFileList<StockCodeNameUS>(StockCodeNameUS.class, storage, "stock-code-name.csv");
	
	// nasdqqlisted
	public static final Storage.LoadSaveFileList<NasdaqListed>
		NasdaqListed = new Storage.LoadSaveFileList<NasdaqListed>(NasdaqListed.class, storage, "nasdaqlisted.csv");
	public static final Storage.LoadSaveFileString
		NasdaqListedText = new Storage.LoadSaveFileString(storage, "nasdaqlisted.txt");
	
	// otherlisted
	public static final Storage.LoadSaveFileList<OtherListed>
		OtherListed = new Storage.LoadSaveFileList<OtherListed>(OtherListed.class, storage, "otherlisted.csv");
	public static final Storage.LoadSaveFileString
		OtherListedText = new Storage.LoadSaveFileString(storage, "otherlisted.txt");
}
