package yokwe.finance.data.provider.yahoo;

import java.util.Collections;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.jpx.StorageJPX;
import yokwe.finance.data.type.CompanyInfo;
import yokwe.finance.data.type.StockCodeJP;
import yokwe.finance.tool.Makefile;
import yokwe.util.Storage;
import yokwe.util.update.UpdateBase;


public class UpdateCompanyInfoJP extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input(StorageJPX.StockCodeName).  // FIXME circular dependency
		output(StorageYahoo.CompanyInfoJP).
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
			try {
				Thread.sleep(SLEEP_IN_MILLI);
			} catch (InterruptedException e) {
				//
			}
		}
	}
	private static final long    SLEEP_IN_MILLI = 1500;


	private int updateCompanyInfo() {
		// read existing data
		var list = StorageYahoo.CompanyInfoJP.getList().stream().collect(Collectors.toList());
		logger.info("companyInfo  {}", list.size());
		// remove if sector or industry is empty
		list.removeIf(o -> o.sector.isEmpty() || o.industry.isEmpty());
		logger.info("companyInfo  {}  remove if sector or industry is empty", list.size());

		// set of required stockCode
		var stockList = StorageJPX.StockCodeName.getList();
		logger.info("stockList    {}", stockList.size());
		// remove if not stock
		stockList.removeIf(o -> !o.type.isStock());
		logger.info("stockList    {}  remove if not stock", stockList.size());
		// remove if preferred stock
		stockList.removeIf(o -> o.isPreferredStock());
		logger.info("stockInfo    {}  remove if preferred stock", list.size());
		// remove if already processed
		{
			var set = list.stream().map(o -> o.stockCode).collect(Collectors.toSet());
			stockList.removeIf(o -> set.contains(o.stockCode));
		}
		logger.info("stockList    {}", stockList.size());
		
		Collections.shuffle(stockList);
		int count = 0;
		int countMod = 0;
		for(var stock: stockList) {
			if ((++count % 100) == 1) logger.info("{}  /  {}", count, stockList.size());
			
			try {
				Thread.sleep(SLEEP_IN_MILLI);
			} catch (InterruptedException e) {
				//
			}
			
			var stockCode = stock.stockCode;
			logger.info("stockCode  {}", stockCode);
			var companyInfo = CompanyInfoYahoo.getInstance(StockCodeJP.toYahooSymbol(stockCode));
			if (companyInfo == null) continue;
			
			var sector   = companyInfo.sector.replace(",", "").replace("—", "-");
			var industry = companyInfo.industry.replace(",", "").replace("—", "-");
			
			// skipe if sector or industry is empty
			if (sector.isEmpty() || industry.isEmpty()) continue;
			
			list.add(new CompanyInfo(stockCode, sector, industry));
			countMod++;
			
			if ((countMod % 10) == 1) StorageYahoo.CompanyInfoJP.save(list);
		}
		
		logger.info("countMod  {}", countMod);
		checkDuplicateKey(list, o -> o.stockCode);
		save(list, StorageYahoo.CompanyInfoJP); // use save for make
		
		return countMod;
	}
}
