package yokwe.finance.data.stock.us;

import java.util.stream.Collectors;

import yokwe.finance.data.provider.nasdaq.StorageNASDAQ;
import yokwe.finance.data.provider.rakuten.StorageRakuten;
import yokwe.util.Makefile;
import yokwe.util.update.UpdateBase;

public class UpdateStockCodeName extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	protected static Makefile MAKEFILE = Makefile.builder().
		input(StorageRakuten.TradingStockUSRakuten, StorageNASDAQ.StockCodeNameNASDAQ).
		output(StorageUS.StockCodeNameUS).
		build();

	public static void main(String[] args) {
		callUpdate();
	}

	@Override
	public void update() {
		var list = StorageNASDAQ.StockCodeNameNASDAQ.getList();
		logger.info("list     {}", list.size());

		var tradingSet = StorageRakuten.TradingStockUSRakuten.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
		logger.info("trading  {}", tradingSet.size());

		list.removeIf(o -> !tradingSet.contains(o.stockCode));
		logger.info("list     {}", list.size());

		save(list, StorageUS.StockCodeNameUS); // use save for make
	}
}
