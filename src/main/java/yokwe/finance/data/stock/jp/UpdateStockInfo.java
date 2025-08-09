package yokwe.finance.data.stock.jp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.jita.StorageJITA;
import yokwe.finance.data.provider.jpx.StockDetail;
import yokwe.finance.data.provider.jpx.StorageJPX;
import yokwe.finance.data.provider.jreit.StorageJREIT;
import yokwe.finance.data.provider.yahoo.StorageYahoo;
import yokwe.finance.data.type.StockCodeJP;
import yokwe.finance.data.type.StockInfoJP;
import yokwe.finance.data.type.StockInfoJP.Type;
import yokwe.util.Makefile;
import yokwe.util.UnexpectedException;
import yokwe.util.json.JSON;
import yokwe.util.update.UpdateBase;

public class UpdateStockInfo extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	protected static Makefile MAKEFILE = Makefile.builder().
		input(StorageJPX.StockCodeName, StorageJITA.FundInfo, StorageJREIT.JREITInfo, StorageYahoo.CompanyInfoJP).
		output(StorageJP.StockInfo).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		var stockList      = StorageJPX.StockCodeName.getList();
		var fundMap        = StorageJITA.FundInfo.getList().stream().filter(o -> !o.stockCode.isEmpty()).collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
		var jreitMap       = StorageJREIT.JREITInfo.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
		var companyInfoMap = StorageYahoo.CompanyInfoJP.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
		
		
		var list = new ArrayList<StockInfoJP>(stockList.size());
		
		int count = 0;
		for(var stock: stockList) {
			if ((++count % 1000) == 1) logger.info("{}  /  {}", count, stockList.size());

			var string = StorageJPX.StockDetailJSON.load(stock.stockCode);
			var result = JSON.unmarshal(StockDetail.class, string);
			
			if (result.section1.data == null) {
				logger.warn("data is null  {}  {}", stock.stockCode, stock.name);
			} else {
				for(var data: result.section1.data.values()) {
					// skip Tokyo Pro Markets
					if (data.LISS_CNV.equals("TPM")) continue;

					String stockCode = StockCodeJP.toStockCode5(data.TTCODE2);
					
					// sanity check
					if (stockCode.equals(stock.stockCode)) {
						String isinCode  = stock.isinCode;
						int    tradeUnit = Integer.parseInt(data.LOSH.replace(",", ""));
						Type   type      = stock.type;
						String sector    = type.simpleType.toString(); // set default value for now
						String industry  = type.simpleType.toString(); // set default value for now
						String name      = stock.name;

						// update sector and industry
						if (type.isETF() || type.isETN()) {
							var fundInfo = fundMap.get(stockCode);
							if (fundInfo == null) {
								if (!isinCode.startsWith("JP")) industry = "ETF-外国籍";
							} else {
								sector   = "ETF-" + fundInfo.investingAsset;
								industry = "ETF-" + fundInfo.investingArea;
							}
						} else if (type.isREIT() || type.isInfra()) {
							var jreitInfo = jreitMap.get(stockCode);
							if (jreitInfo != null) {
								industry = "REIT-" + jreitInfo.category.replace(" ", "");
							}
						} else if (StockCodeJP.isPreferredStock(stockCode)) {
							sector    = "PREF";
							industry  = "PREF";
						} else {
							var companyInfo = companyInfoMap.get(stockCode);
							if (companyInfo != null) {
								sector    = companyInfo.sector;
								industry  = companyInfo.industry;
							} else {
								sector    = "*" + type + "*";
								industry  = "*" + type + "*";
							}
						}
						
						var stockInfo = new StockInfoJP(stockCode, isinCode, tradeUnit, type, sector, industry, name);
						list.add(stockInfo);
					} else {
						logger.error("Unepected stockCode");
						logger.error("  stock      {}", stock.toString());
						logger.error("  stockCode  {}", stockCode);
						throw new UnexpectedException("Unepected stockCode");
					}
				}
			}
		}
		
		save(list, StorageJP.StockInfo); // use save for make
	}
	
	
	private static final Map<String, Type> typeMap = new HashMap<>();
	//                       code
	static {
		StorageJPX.ETF.  getList().stream().forEach(o -> typeMap.put(o.code, StockInfoJP.Type.ETF));
		StorageJPX.ETN.  getList().stream().forEach(o -> typeMap.put(o.code, StockInfoJP.Type.ETN));
		StorageJPX.Infra.getList().stream().forEach(o -> typeMap.put(o.code, StockInfoJP.Type.INFRA));
		StorageJPX.REIT. getList().stream().forEach(o -> typeMap.put(o.code, StockInfoJP.Type.REIT));
		typeMap.put("83010", Type.CERTIFICATE); // 日本銀行
		typeMap.put("84210", Type.CERTIFICATE); // 信金中央金庫
		
		typeMap.put("グロース",           Type.DOMESTIC_GROWTH);
		typeMap.put("プライム",           Type.DOMESTIC_PRIME);
		typeMap.put("スタンダード",       Type.DOMESTIC_STANDARD);
		typeMap.put("外国株グロース",     Type.FOREIGN_GROWTH);
		typeMap.put("外国株プライム",     Type.FOREIGN_PRIME);
		typeMap.put("外国株スタンダード", Type.FOREIGN_STANDARD);
	}

}
