package yokwe.finance.data.provider.jita;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.jpx.StorageJPX;
import yokwe.finance.data.type.NISAInfo;
import yokwe.finance.data.type.StockCodeJP;
import yokwe.util.FileUtil;
import yokwe.util.Makefile;
import yokwe.util.URLUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.libreoffice.LibreOffice;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;
import yokwe.util.update.UpdateSimpleGeneric;

public class UpdateNISAInfo extends UpdateSimpleGeneric<Void> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input(StorageJPX.StockCodeName).
		output(StorageJITA.NISAInfo).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	protected Void downloadFile() {
		try {
			LibreOffice.initialize();
			
			{
				var urlString = "https://www.toushin.or.jp/files/static/486/listed_fund_for_investor.xlsx";
				var fileXLSX  = LISTED_XLSX;
				var clazz     = ListedFundForInvestor.class;
				
				var list = downloadAndExtact(urlString, fileXLSX, clazz);		
				StorageJITA.ListedFundForInvestor.save(list);
			}
			
			{
				var urlString = "https://www.toushin.or.jp/files/static/486/unlisted_fund_for_investor.xlsx";
				var fileXLSX  = UNLISTED_XLSX;
				var clazz     = UnlistedFundForInvestor.class;
				
				var list = downloadAndExtact(urlString, fileXLSX, clazz);
				
				// fix null value in redemptionDate
				for(var data: list) {
					if (data.redemptionDate == null) data.redemptionDate = "";
				}
				StorageJITA.UnlistedFundForInvestor.save(list);
			}
		} finally {
			LibreOffice.terminate();
		}
				
		return null;
	}
	private static File LISTED_XLSX   = StorageJITA.storage.getFile("listed-fund-for-investor.xlsx");
	private static File UNLISTED_XLSX = StorageJITA.storage.getFile("unlisted-fund-for-investor.xlsx");
	private <E extends Sheet & Comparable<E>> List<E> downloadAndExtact(String urlString, File fileXLSX, Class<E> clazz) {
		logger.info("download {}", urlString);
		
		byte[] rawData = HttpUtil.getInstance().downloadRaw(urlString);
		logger.info("save  {}  {}", rawData.length, fileXLSX.getPath());
		FileUtil.rawWrite().file(fileXLSX, rawData);
		
		try (SpreadSheet spreadSheet = new SpreadSheet(URLUtil.toURL(fileXLSX).toString(), true)) {
			return Sheet.extractSheet(spreadSheet, clazz);
		}
	}
	
	@Override
	protected void updateFile(Void dummy) {
		var list = new ArrayList<NISAInfo>();
		
		// build list from listed
		{
			// stockCode to isinCode
			var stockInfoMap = StorageJPX.StockCodeName.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
			
			for (var data : StorageJITA.ListedFundForInvestor.getList()) {
				var stockCode = StockCodeJP.toStockCode5(data.stockCode);
				if (stockInfoMap.containsKey(stockCode)) {
					var stockInfo = stockInfoMap.get(stockCode);
					if (stockInfo == null) {
						logger.error("Unexpected stockCode");
						logger.error("  stockCode  {}", stockCode);
						throw new UnexpectedException("Unexpected stockCode");
					}
					var isinCode  = stockInfo.isinCode;
					var tsumitate = data.isTsumitate();
					var name      = stockInfo.name;

					list.add(new NISAInfo(isinCode, tsumitate, name));
				} else {
					logger.warn("Unexpected stockCode  {}  {}  {}", data.inceptionDate, data.stockCode, data.fundName);
				}
			}
		}
		// build list from unlisted
		{
			// fundCode to isinCode
			var fundInfoMap  = StorageJITA.FundInfo.getList().stream().collect(Collectors.toMap(o -> o.fundCode, Function.identity()));
			
			for (var data : StorageJITA.UnlistedFundForInvestor.getList()) {
				var fundCode = data.fundCode;
				if (fundInfoMap.containsKey(fundCode)) {
					var fundInfo = fundInfoMap.get(fundCode);
					if (fundInfo == null) {
						logger.error("Unexpected fundCode");
						logger.error("  fundCode  {}", fundCode);
						throw new UnexpectedException("Unexpected fundCode");
					}
					var isinCode  = fundInfo.isinCode;
					var tsumitate = data.isTsumitate();
					var name      = fundInfo.name;
					list.add(new NISAInfo(isinCode, tsumitate, name));
				} else {
					logger.warn("Unexpected fundCode   {}  {}  {}", data.inceptionDate, data.fundCode, data.fundName);
				}
			}
		}
		
		checkDuplicateKey(list, o -> o.isinCode);
		save(list, StorageJITA.NISAInfo);  // use save for make
	}
}
