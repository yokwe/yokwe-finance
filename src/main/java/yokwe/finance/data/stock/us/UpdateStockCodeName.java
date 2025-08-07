package yokwe.finance.data.stock.us;

import java.util.HashMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.bats.StorageBATS;
import yokwe.finance.data.provider.nasdaq.StorageNASDAQ;
import yokwe.finance.data.provider.nyse.StorageNYSE;
import yokwe.finance.data.provider.rakuten.StorageRakuten;
import yokwe.finance.data.type.StockCodeNameUS;
import yokwe.util.makefile.Makefile;
import yokwe.util.update.UpdateBase;

public class UpdateStockCodeName extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	protected static Makefile MAKEFILE = Makefile.builder().
		input(
			StorageRakuten.TradingStockUS, StorageBATS.StockCodeName, StorageNASDAQ.StockCodeName, StorageNYSE.StockCodeName,
			StorageNASDAQ.NasdaqListed, StorageNASDAQ.OtherListed).
		output(StorageUS.StockCodeName).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		var tradingSet = StorageRakuten.TradingStockUS.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
		logger.info("tradingSet  {}", tradingSet.size());

		var listBATS   = StorageBATS.StockCodeName.getList();
		var listNASDAQ = StorageNASDAQ.StockCodeName.getList();
		var listNYSE   = StorageNYSE.StockCodeName.getList();
		logger.info("BATS    {}", listBATS.size());
		logger.info("NASDAQ  {}", listNASDAQ.size());
		logger.info("NYSE    {}", listNYSE.size());
		
		var setBATS   = listBATS.stream().map(o -> o.stockCode).collect(Collectors.toSet());
		var setNASDAQ = listNASDAQ.stream().map(o -> o.stockCode).collect(Collectors.toSet());
		listNYSE.removeIf(o -> setBATS.contains(o.stockCode));
		listNYSE.removeIf(o -> setNASDAQ.contains(o.stockCode));
		logger.info("NYSE    {}  after remove of BATS and NASDAQ", listNYSE.size());
		
		listBATS.  removeIf(o -> !tradingSet.contains(o.stockCode));
		listNASDAQ.removeIf(o -> !tradingSet.contains(o.stockCode));
		listNYSE.  removeIf(o -> !tradingSet.contains(o.stockCode));
		logger.info("BATS    {}  after remove of not trading stock", listBATS.size());
		logger.info("NASDAQ  {}  after remove of not trading stock", listNASDAQ.size());
		logger.info("NYSE    {}  after remove of not trading stock", listNYSE.size());
		
		var set = new TreeSet<StockCodeNameUS>();
		set.addAll(listNYSE); // listNYSE contains BATS and NASDAQ
		set.addAll(listBATS);
		set.addAll(listNASDAQ);
		logger.info("set     {}", set.size());
		
		// use nasdaq name if possile
		var map = new HashMap<String, String>(set.size());
		//                    code    name
		StorageNASDAQ.NasdaqListed.getList().stream().forEach(o -> map.put(o.symbol, o.name));
		StorageNASDAQ.OtherListed. getList().stream().forEach(o -> map.put(o.symbol, o.name));
		for(var e: set) {
			var code = e.stockCode;
			var name = map.get(code);
			if (name != null) e.name = name;
		}
		
		save(set, StorageUS.StockCodeName); // use save for make
	}
}
