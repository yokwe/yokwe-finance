package yokwe.finance.data.type;

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
}
