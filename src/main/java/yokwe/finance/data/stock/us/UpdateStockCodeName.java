package yokwe.finance.data.stock.us;

import java.util.stream.Collectors;

import yokwe.finance.data.provider.nasdaq.StorageNASDAQ;
import yokwe.finance.data.provider.webull.StorageWebull;
import yokwe.util.Makefile;
import yokwe.util.update.UpdateBase;

public class UpdateStockCodeName extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	protected static Makefile MAKEFILE = Makefile.builder().
		input(StorageWebull.TradingStockUSWebull, StorageNASDAQ.StockCodeNameNASDAQ).
		output(StorageStockUS.StockCodeNameUS).
		build();

	public static void main(String[] args) {
		callUpdate();
	}

	@Override
	public void update() {
		var list = StorageNASDAQ.StockCodeNameNASDAQ.getList();
		logger.info("list     {}", list.size());

		var tradingSet = StorageWebull.TradingStockUSWebull.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
		logger.info("trading  {}", tradingSet.size());

		list.removeIf(o -> !tradingSet.contains(o.stockCode));
		logger.info("list     {}", list.size());

		save(list, StorageStockUS.StockCodeNameUS); // use save for make
	}
}
