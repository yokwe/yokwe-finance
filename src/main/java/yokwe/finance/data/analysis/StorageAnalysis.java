package yokwe.finance.data.analysis;

import yokwe.finance.data.StorageData;
import yokwe.finance.data.type.TaxAdjustment;
import yokwe.util.Storage;

public class StorageAnalysis {
	public static final Storage storage = StorageData.storage.getStorage("analysis");

	// double tax adjustment
	public static final Storage.LoadSaveFileList<TaxAdjustment>
		TaxAdjustment = new Storage.LoadSaveFileList<TaxAdjustment>(TaxAdjustment.class,  storage, "tax-adjustment.csv");
}
