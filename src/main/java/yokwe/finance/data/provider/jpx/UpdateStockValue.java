package yokwe.finance.data.provider.jpx;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import yokwe.finance.data.type.StockCodeJP;
import yokwe.finance.data.type.StockValueJP;
import yokwe.util.json.JSON;
import yokwe.util.makefile.Makefile;
import yokwe.util.update.UpdateBase;

public class UpdateStockValue extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input(StorageJPX.StockDetailJSON).
		output(StorageJPX.StockValue).
		build();
		
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		var stockCodeList = UpdateStockDetailJSON.getJSONFileList().stream().map(o -> o.getName().replace(".json", "")).toList();
		
		logger.info("stockCodeList  {}", stockCodeList.size());
		
		var dataList = new ArrayList<StockDetail.Data>();
		
		for(var stockCode: stockCodeList) {
			var string = StorageJPX.StockDetailJSON.load(stockCode);
			var result = JSON.unmarshal(StockDetail.class, string);
			
			if (result.section1.data == null) continue;
			
			dataList.addAll(result.section1.data.values());
		}
		logger.info("dataList  {}", dataList.size());
		
		var list = toStockValueList(dataList);
		save(list, StorageJPX.StockValue); // use save for make
	}
	
	private List<StockValueJP> toStockValueList(List<StockDetail.Data> dataList) {
		var list = new ArrayList<StockValueJP>(dataList.size());
		
		for(var data: dataList) {
			StockValueJP stockValue = new StockValueJP();
			
			// from stock detail
			stockValue.stockCode = StockCodeJP.toStockCode5(data.TTCODE2);
			stockValue.date      = toLocalDate(data.ZXD);   // ZXD
			stockValue.open      = toBigDecimal(data.DOP);	// DOP
			stockValue.openTime  = toLocalTime(data.DOPT);  // DOPT
			stockValue.high      = toBigDecimal(data.DHP);	// DHP
			stockValue.highTime  = toLocalTime(data.DHPT);  // DHPT
			stockValue.low       = toBigDecimal(data.DLP);	// DLP
			stockValue.lowTime   = toLocalTime(data.DLPT);	// DLPT
			stockValue.price     = toBigDecimal(data.DPP);	// DPP
			stockValue.priceTime = toLocalTime(data.DPPT);  // DPPT
			stockValue.volume    = toLong(data.DV);			// DV
			stockValue.issued    = toLong(data.SHRK);		// SHRK
			stockValue.bid       = toBigDecimal(data.QBP);	// QBP
			stockValue.bidTime   = toLocalTime(data.QBPT);	// QBPT
			stockValue.ask       = toBigDecimal(data.QAP);	// QAP
			stockValue.askTime   = toLocalTime(data.QAPT);	// QAPT
			stockValue.previous  = toBigDecimal(data.PRP);	// PRP  -- previous close
			
			list.add(stockValue);
		}
		
		return list;
	}
	private static LocalDate toLocalDate(String string) {
		return string.equals("") ? ZERO_DATE : LocalDate.parse(string.replace("/", "-"));
	}
	private static LocalTime toLocalTime(String string) {
		return string.equals("-") ? ZERO_TIME : LocalTime.parse(string);
	}
	private static BigDecimal toBigDecimal(String string) {
		return (string.equals("-") || string.isEmpty()) ? BigDecimal.ZERO : new BigDecimal(string.replace(",", ""));
	}
	private static long toLong(String string) {
		return string.equals("-") ? 0 : Long.valueOf(string.replace(",", ""));
	}
	
	private static final LocalDate ZERO_DATE = LocalDate.of(2000, 1, 1);
	private static final LocalTime ZERO_TIME = LocalTime.of(0, 0);

}
