package yokwe.finance.data.stock.us;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.yahoo.StorageYahoo;
import yokwe.finance.data.type.StockInfoUS;
import yokwe.finance.data.type.StockInfoUS.Market;
import yokwe.finance.data.type.StockInfoUS.Type;
import yokwe.util.Makefile;
import yokwe.util.update.UpdateBase;

public class UpdateStockInfo extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	protected static Makefile MAKEFILE = Makefile.builder().
		input(StorageUS.StockCodeName, StorageYahoo.CompanyInfoUS).
		output(StorageUS.StockInfo).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		var stockList = StorageUS.StockCodeName.getList();
		logger.info("stockList       {}", stockList.size());
		
		var companyInfoMap = StorageYahoo.CompanyInfoUS.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
		logger.info("companyInfoMap  {}", companyInfoMap.size());

		var list = new ArrayList<StockInfoUS>();
		for(var e: stockList) {
			var companyInfo = companyInfoMap.get(e.stockCode);
			
			String stockCode = e.stockCode;
			Market market    = e.market;
			Type   type      = e.type;
			String industry  = companyInfo == null ? "*" + e.type + "*" : companyInfo.industry;
			String sector    = companyInfo == null ? "*" + e.type + "*" : companyInfo.sector;
			String name      = e.name;
			list.add(new StockInfoUS(stockCode, market, type, industry, sector, name));
		}
		
		logger.info("list  {}", list.size());
		save(list, StorageUS.StockInfo); // use save for make
	}
}
