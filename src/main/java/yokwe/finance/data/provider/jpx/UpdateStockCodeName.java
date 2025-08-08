package yokwe.finance.data.provider.jpx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import yokwe.finance.data.type.StockCodeJP;
import yokwe.finance.data.type.StockInfoJP;
import yokwe.finance.data.type.StockInfoJP.Type;
import yokwe.finance.tool.Makefile;
import yokwe.util.FileUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.json.JSON;
import yokwe.util.update.UpdateBase;

public class UpdateStockCodeName extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input(StorageJPX.StockDetailJSON, StorageJPX.ETF, StorageJPX.ETN, StorageJPX.Infra, StorageJPX.REIT).
		output(StorageJPX.StockCodeName).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		var jsonFileList = UpdateStockDetailJSON.getJSONFileList();
		
		var list = new ArrayList<StockCodeName>();
		// build list from jsonFileList
		for(var file: jsonFileList) {
			var string = FileUtil.read().file(file);
			var stockDetail = JSON.unmarshal(StockDetail.class, string);
			if (stockDetail.section1 == null || stockDetail.section1.data == null) continue;
			for(var e: stockDetail.section1.data.values()) {
				// skip Tokyo Pro Markets
				if (e.LISS_CNV.equals("TPM")) continue;
				
				var stockCode = StockCodeJP.toStockCode5(e.TTCODE2);
				var isinCode  = e.ISIN;
				var name      = StringUtil.toFullWidth(e.FLLN).replace("　　", "　");
				
				var type = typeMap.get(stockCode);
				if (type == null) type = typeMap.get(e.LISS_CNV);
				if (type == null) {
					logger.error("Unexpected type");
					logger.error("  stockCode  {}", stockCode);
					logger.error("  LISS_CNV   {}", e.LISS_CNV);
					throw new UnexpectedException("Unexpected type");
				}
				
				list.add(new StockCodeName(stockCode, isinCode, type, name));
			}
		}
		
		// sanity check
		checkDuplicateKey(list, o -> o.stockCode);
		checkDuplicateKey(list, o -> o.isinCode);
		
		save(list, StorageJPX.StockCodeName); // use save for make
		
		// sanity check
		StockCodeName.checkStockCodeName();
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
