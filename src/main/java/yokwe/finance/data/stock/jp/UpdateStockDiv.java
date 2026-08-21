package yokwe.finance.data.stock.jp;

import yokwe.finance.data.provider.jpx.StorageJPX;
import yokwe.finance.data.provider.jreit.StorageJREIT;
import yokwe.util.Makefile;
import yokwe.util.update.UpdateBase;

public class UpdateStockDiv extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	protected static Makefile MAKEFILE = Makefile.builder().
		input(StorageStockJP.StockInfoJP, StorageJPX.StockDiv, yokwe.finance.data.fund.jp.StorageFundJP.FundDiv, StorageJREIT.JREITDiv).
		output(StorageStockJP.StockDiv).
		build();

	public static void main(String[] args) {
		callUpdate();
	}

	@Override
	public void update() {
		var list = StorageStockJP.StockInfoJP.getList();
		logger.info("list       {}", list.size());

		{
			var validNameList = list.stream().map(o -> o.stockCode).toList();
			StorageStockJP.StockDiv.delistUnknownFile(validNameList);
		}

		int countETF    = 0;
		int countREIT   = 0;

		int countFundJP = 0;
		int countJREIT  = 0;
		for(var e: list) {
			var type      = e.type;
			var stockCode = e.stockCode;

			var divList = StorageJPX.StockDiv.getList(stockCode);
			if (type.isETF()) {
				countETF++;
				// take value from fund jp
				var divListFundJP = yokwe.finance.data.fund.jp.StorageFundJP.FundDiv.getList(e.isinCode);
//				logger.info("ETF   {}  {}  {}  {}", e.stockCode, e.isinCode, divList.size(), divListJITA.size());
				if (divList.size() < divListFundJP.size()) {
					countFundJP++;
					divList = divListFundJP;
				}
			} else if (type.isREIT() || type.isInfra()) {
				countREIT++;
				var divListJREIT = StorageJREIT.JREITDiv.getList(stockCode);
//				logger.info("JREIT {}  {}  {}", stockCode, divList.size(), divListJREIT.size());
				if (divList.size() < divListJREIT.size()) {
					countJREIT++;
					divList = divListJREIT;
				}
			}

			StorageStockJP.StockDiv.save(stockCode, divList);
		}

		logger.info("countETF    {}", countETF);
		logger.info("countFundJP {}", countFundJP);
		logger.info("countREIT   {}", countREIT);
		logger.info("countJREIT  {}", countJREIT);

		// touch file
		StorageStockJP.StockDiv.touch();
	}
}
