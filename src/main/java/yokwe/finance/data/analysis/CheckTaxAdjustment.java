package yokwe.finance.data.analysis;

import java.util.stream.Collectors;

import yokwe.finance.data.provider.jpx.StorageJPX;

public class CheckTaxAdjustment {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static void main(String[] args) {
		logger.info("START");

		var stockSet = StorageJPX.StockListJPX.getList().stream().map(o -> o.code).collect(Collectors.toSet());
		logger.info("stockSet  {}", stockSet.size());

		var taxList = StorageAnalysis.TaxAdjustment.getList();
		logger.info("taxList  {}", taxList.size());

		for(var e: taxList) {
			if (!stockSet.contains(e.stockCode)) {
				logger.info("delisted  {}  {}", e.stockCode, e.name);
			}
		}

		logger.info("STOP");
	}
}
