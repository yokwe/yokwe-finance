package yokwe.finance.data.provider.yahoo;

import java.util.Collections;
import java.util.stream.Collectors;

import yokwe.finance.data.stock.us.StorageUS;
import yokwe.finance.data.type.CompanyInfo;
import yokwe.finance.data.type.StockInfoUS;
import yokwe.util.Storage;
import yokwe.util.ThreadUtil;
import yokwe.util.makefile.Makefile;
import yokwe.util.update.UpdateBase;


public class UpdateCompanyInfoUS extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input(StorageUS.StockCodeName). // FIXME circular dependency
		output(StorageYahoo.CompanyInfoUS).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		Storage.initialize();
		
		for(int take = 1; take < 9; take++) {
			logger.info("start take {}", take);
			int countMod = updateCompanyInfo();
			if (countMod == 0) break;
			
			ThreadUtil.sleep(SLEEP_IN_MILLI);
		}
	}
	private static final long    SLEEP_IN_MILLI = 1500;
	
	private int updateCompanyInfo() {
		// set of required stockCode
		var stockList = StorageUS.StockCodeName.getList();
		logger.info("stockList    {}", stockList.size());
		stockList.removeIf(o -> o.type.isETF());
		logger.info("stockList    {}  after remove ETF", stockList.size());

		var list = StorageYahoo.CompanyInfoUS.getList();
		var existingSet = list.stream().map(o -> o.stockCode).collect(Collectors.toSet());
		stockList.removeIf(o -> existingSet.contains(o.stockCode));
		logger.info("stockInfo    {}  after remove existing stock", stockList.size());
		
		Collections.shuffle(stockList);
		int count = 0;
		int countMod = 0;
		for(var stockInfo: stockList) {
			if ((++count % 100) == 1) logger.info("{}  /  {}", count, stockList.size());
			
			ThreadUtil.sleep(SLEEP_IN_MILLI);
			
			var stockCode = stockInfo.stockCode;
			
			var companyInfo = CompanyInfoYahoo.getInstance(StockInfoUS.toYahooSymbol(stockCode));
			if (companyInfo == null) continue;
			
			var sector   = companyInfo.sector.replace(",", "").replace("—", "-");
			var industry = companyInfo.industry.replace(",", "").replace("—", "-");
			
			// skip if sector or industry is empty
			if (sector.isEmpty() || industry.isEmpty()) continue;
			
			list.add(new CompanyInfo(stockCode, sector, industry));
			countMod++;
			
			if ((countMod % 10) == 1) StorageYahoo.CompanyInfoUS.save(list);
		}
		
		logger.info("countMod  {}", countMod);
		checkDuplicateKey(list, o -> o.stockCode);
		save(list, StorageYahoo.CompanyInfoUS); // use save for make
		return countMod;
	}
}
