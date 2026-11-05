package yokwe.finance.data.type;

import java.util.regex.Pattern;

import yokwe.finance.data.stock.us.StorageStockUS;

public class StockCodeUS {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static boolean isValidCode(String string) {
		if (string == null) {
			return false;
		} else {
			var m = PAT_STOCK_CODE.matcher(string);
			return m.matches();
		}
	}

	private static Pattern PAT_STOCK_CODE = Pattern.compile("(([A-Z]{1,5})|([A-Z]{1,4}\\.[A-Z])|([A-Z]{1,4}-[A-Z]?))");
	public static void main(String[] args) {
		logger.info("START");

		{
			for(var e: StorageStockUS.StockCodeNameUS.getList()) {
				if (!isValidCode(e.stockCode)) {
					logger.info("XX  {}  {}", e.stockCode, e.name);
				}
			}
		}

		logger.info("STOP");
	}

}
