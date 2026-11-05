package yokwe.finance.data.analysis;

import yokwe.finance.data.StorageData;
import yokwe.finance.data.type.ForeignTaxAdjusted;
import yokwe.util.Storage;

public class StorageAnalysis {
	public static final Storage storage = StorageData.storage.getStorage("analysis");

	// foreign double tax adjustment
	public static final Storage.LoadSaveFileList<ForeignTaxAdjusted>
		ForeignTaxAdjusted = new Storage.LoadSaveFileList<ForeignTaxAdjusted>(ForeignTaxAdjusted.class,  storage, "foreign-tax-adjusted.csv");
}
