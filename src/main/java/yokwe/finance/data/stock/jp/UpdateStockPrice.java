package yokwe.finance.data.stock.jp;

import java.util.stream.Collectors;

import yokwe.finance.data.provider.jita.StorageJITA;
import yokwe.finance.data.provider.jpx.StorageJPX;
import yokwe.finance.data.type.DailyValue;
import yokwe.util.Makefile;
import yokwe.util.update.UpdateBase;

public class UpdateStockPrice extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	protected static Makefile MAKEFILE = Makefile.builder().
		input(StorageJP.StockInfo, StorageJPX.StockPriceOHLCV, StorageJITA.FundPrice).
		output(StorageJP.StockPrice).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		var list = StorageJP.StockInfo.getList();
		logger.info("list      {}", list.size());
		
		{
			var validNameList = list.stream().map(o -> o.stockCode).toList();
			StorageJP.StockPrice.delistUnknownFile(validNameList);
		}
		
		int countETF  = 0;
		int countJITA = 0;
		for(var e: list) {
			var type      = e.type;
			var stockCode = e.stockCode;
			var priceList = StorageJPX.StockPriceOHLCV.getList(stockCode).stream().map(o -> new DailyValue(o.date, o.close)).collect(Collectors.toList());
			
			if (type.isETF()) {
				countETF++;
				// take value from JITA
				var priceListJITA = StorageJITA.FundPrice.getList(e.isinCode).stream().map(o -> new DailyValue(o.date, o.price)).collect(Collectors.toList());
//				logger.info("ETF   {}  {}  {}", stockCode, priceList.size(), priceListJITA.size());
				if (priceList.size() < priceListJITA.size()) {
					priceList = priceListJITA;
					countJITA++;
				}
			}
			StorageJP.StockPrice.save(stockCode, priceList);
		}
		logger.info("countETF   {}", countETF);
		logger.info("countJITA  {}", countJITA);
		
		// touch file
		StorageJP.StockPrice.touch();
	}
}
