package yokwe.finance.data.provider.jpx;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.finance.data.type.CodeName;
import yokwe.finance.data.type.DailyValue;
import yokwe.finance.data.type.OHLCV;
import yokwe.finance.data.type.StockValueJP;
import yokwe.util.Storage;

public class StorageJPX {
	public static final Storage storage = StorageProvider.storage.getStorage("jpx");
	
	// stock price
	public static final Storage.LoadSaveDirectoryList<OHLCV>
		StockPriceOHLCV = new Storage.LoadSaveDirectoryList<OHLCV>(OHLCV.class, storage, "stock-price-ohlcv", o -> o + ".csv");
	
	// stock div
	public static final Storage.LoadSaveDirectoryList<DailyValue>
		StockDiv = new Storage.LoadSaveDirectoryList<DailyValue>(DailyValue.class, storage, "stock-div", o -> o + ".csv");
	
	// stock detail
	public static final Storage.LoadSaveDirectoryString
		KessanJSON = new Storage.LoadSaveDirectoryString(storage, "kessan-json",  o -> o + ".json");

	// stock list JSON
	public static final Storage.LoadSaveDirectoryString
		StockListJSON = new Storage.LoadSaveDirectoryString(storage, "stock-list-json", o -> o + ".json");
	// stock list
	public static final Storage.LoadSaveFileList<CodeName>
		StockList = new Storage.LoadSaveFileList<CodeName>(CodeName.class, storage, "stock-list.csv");
	
	// stock detail
	public static final Storage.LoadSaveDirectoryString
		StockDetailJSON = new Storage.LoadSaveDirectoryString(storage, "stock-detail-json",  o -> o + ".json");
	
	// stock code name
	public static final Storage.LoadSaveFileList<StockCodeName>
		StockCodeName = new Storage.LoadSaveFileList<StockCodeName>(StockCodeName.class, storage, "stock-code-name.csv");
	
	// stock value
	public static final Storage.LoadSaveFileList<StockValueJP>
		StockValue = new Storage.LoadSaveFileList<StockValueJP>(StockValueJP.class, storage, "stock-value.csv");
	
	// etf
	public static final Storage.LoadSaveFileList<CodeName>
		ETF = new Storage.LoadSaveFileList<>(CodeName.class,  storage, "etf.csv");
	// etn
	public static final Storage.LoadSaveFileList<CodeName>
		ETN = new Storage.LoadSaveFileList<>(CodeName.class,  storage, "etn.csv");
	// infra fund
	public static final Storage.LoadSaveFileList<CodeName>
		Infra = new Storage.LoadSaveFileList<>(CodeName.class,  storage, "infra.csv");
	// reit
	public static final Storage.LoadSaveFileList<CodeName>
		REIT = new Storage.LoadSaveFileList<>(CodeName.class,  storage, "reit.csv");

	// intra price
	public static final Storage.LoadSaveDirectoryString
		StockIntraPriceJSON = new Storage.LoadSaveDirectoryString(storage, "stock-intra-price-json",  o -> o + ".json");
	public static final Storage.LoadSaveDirectoryList<OHLCVDateTime>
		StockIntraPrice = new Storage.LoadSaveDirectoryList<OHLCVDateTime>(OHLCVDateTime.class, storage, "stock-intra-price", o -> o + ".csv");

}
