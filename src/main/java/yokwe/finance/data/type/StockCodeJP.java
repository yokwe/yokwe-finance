package yokwe.finance.data.type;

import java.util.regex.Pattern;

import yokwe.finance.data.provider.jpx.StorageJPX;
import yokwe.util.UnexpectedException;

public class StockCodeJP {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static String toStockCode4(String stockCode) {
		if (stockCode.length() == 4) {
			return stockCode;
		} else if (stockCode.length() == 5) {
			if (stockCode.endsWith("0")) {
				return stockCode.substring(0, 4);
			} else {
				return stockCode; // 25935 伊藤園 優先株式,市場第一部（内国株）
			}
		} else {
			logger.error("Unexpected stockCode");
			logger.error("  stockCode {}!", stockCode);
			throw new UnexpectedException("Unexpected stockCode");
		}
	}

	public static String toStockCode5(String stockCode) {
		if (stockCode.length() == 5) {
			return stockCode;
		} else if (stockCode.length() == 4) {
			return stockCode + "0";
		} else {
			logger.error("Unexpected stockCode");
			logger.error("  stockCode {}!", stockCode);
			throw new UnexpectedException("Unexpected stockCode");
		}
	}

	public static boolean isPreferredStock(String stockCode) {
		// https://www.jpx.co.jp/sicc/news/nlsgeu00000329fb-att/bessi2.pdf
		//   予備コードは次のような場合に固有名コードの末尾につけて使用する。
		//   新株式 １ 第二新株式 ２ 優先株式 ５、６ 新株予約権証券 ９

		if (stockCode.length() == 5) {
			char char5 = stockCode.charAt(4);
			return (char5 == '5' || char5 == '6');
		}
		logger.error("Unexpected stockCode");
		logger.error("  stockCode {}!", stockCode);
		throw new UnexpectedException("Unexpected stockCode");
	}

	public static String toYahooSymbol(String stockCode) {
		if (stockCode.length() == 4) {
			return stockCode + ".T";
		} else if (stockCode.length() == 5) {
			if (stockCode.endsWith("0")) {
				return stockCode.substring(0, 4) + ".T";
			}
			if (isPreferredStock(stockCode)) {
				// 25935 伊藤園 優先株式,市場第一部（内国株）
				// 25935 => 2593P.T
				return stockCode.substring(0, 4) + "P.T";
			}
		}
		logger.error("Unexpected stockCode");
		logger.error("  stockCode {}!", stockCode);
		throw new UnexpectedException("Unexpected stockCode");
	}

	// 英大文字のうち、「B」、「E」、「I」、「O」、「Q」、「V」、「Z」を除く19文字を使用します
	private static final String  DIGIT_ALPHA    = "[0-9ACDFGHJKLMNPRSTUWXY]";
	private static final Pattern PAT_STOCK_CODE_5 = Pattern.compile("[1-9]" + DIGIT_ALPHA + "[0-9]" + DIGIT_ALPHA + "[0-9]");
	private static final Pattern PAT_STOCK_CODE_4 = Pattern.compile("[1-9]" + DIGIT_ALPHA + "[0-9]" + DIGIT_ALPHA);
	public static boolean isValidCode(String string) {
		if (string == null) {
			return false;
		} else {
			var m = PAT_STOCK_CODE_5.matcher(string);
			return m.matches();
		}
	}
	public static boolean isValidCode4(String string) {
		if (string == null) {
			return false;
		} else {
			var m = PAT_STOCK_CODE_4.matcher(string);
			return m.matches();
		}
	}

	public static void main(String[] args) {
		logger.info("START");

		{
			for(var e: StorageJPX.StockCodeNameJPX.getList()) {
				if (!isValidCode(e.stockCode)) {
					logger.info("XX  {}  {}", e.stockCode, e.name);
				}
			}
		}

		logger.info("STOP");
	}

}
