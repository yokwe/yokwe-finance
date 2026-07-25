package yokwe.finance.data.provider.bats;

import java.util.ArrayList;
import java.util.List;

import yokwe.finance.data.provider.nasdaq.OtherListed;
import yokwe.finance.data.provider.nasdaq.StorageNASDAQ;
import yokwe.finance.data.type.StockCodeNameUS;
import yokwe.finance.data.type.StockInfoUS.Market;
import yokwe.finance.data.type.StockInfoUS.Type;
import yokwe.util.update.UpdateList;

public class UpdateStockCodeName extends UpdateList<OtherListed> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	// NOTE  Daily Listed Securities Report become not accessible.
	// NOTE  Use StockCodeName in NASDAQ.

//	public static Makefile MAKEFILE = Makefile.builder().
//		input(StorageNASDAQ.OtherListed).
//		output(StorageBATS.StockCodeName).
//		build();

	public static void main(String[] args) {
		callUpdate();
	}

	@Override
	protected List<OtherListed> downloadFile() {
		var list = StorageNASDAQ.OtherListed.getList();
		logger.info("list  {}", list.size());
		return list;
	}

	@Override
	protected void updateFile(List<OtherListed> ohterListed) {
		var stockList = new ArrayList<StockCodeNameUS>(ohterListed.size());

		int countTotal = 0;
		int countSkip  = 0;

		for(var e: ohterListed) {
			countTotal++;

			// only BATS
			if (!e.isBATS()) {
				countSkip++;
				continue;
			}

			if (e.isTestIssue() || e.isRights() || e.isUnits() || e.isWarrant()) {
				countSkip++;
				continue;
			}
			// skip warrant and beneficial interest
			var upperCaseName = e.name.toUpperCase();
			if (upperCaseName.contains("WARRANT") || upperCaseName.contains("BENEFICIAL INTEREST")) {
				countSkip++;
				continue;
			}

			String symbol   = e.symbol;
			Market market   = Market.BATS;
			Type   type     = e.etf.equals("Y") ? Type.ETF : Type.COMMON; // just ETF or COMMON for now
			String name     = e.name;

			stockList.add(new StockCodeNameUS(symbol, market, type, name));
		}
		logger.info("total  {}", countTotal);
		logger.info("skip   {}", countSkip);

		// sanity check
		checkDuplicateKey(stockList, o -> o.stockCode);

		// save csv file
		save(stockList, StorageBATS.StockCodeName); // use save for make
	}
}
