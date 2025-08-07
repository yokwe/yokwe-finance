package yokwe.finance.data.provider.nasdaq.api;

import java.io.File;

import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public final class API {	
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	// https://api.nasdaq.com/api/quote/YYY/dividends?assetclass=etf
	// https://api.nasdaq.com/api/quote/LMT/dividends?assetclass=stocks
	
	// https://api.nasdaq.com/api/quote/LMT/realtime-trades?&limit=5
	
	// https://api.nasdaq.com/api/quote/LMT/extended-trading?assetclass=stocks&markettype=post
	
	// https://api.nasdaq.com/api/quote/LMT/extended-trading?assetclass=stocks&markettype=pre
	
	// https://api.nasdaq.com/api/quote/FR10UK/chart?assetclass=index
	// https://api.nasdaq.com/api/quote/FR10UK/info?assetclass=index
	
	public static final String NOT_AVAILABLE = "N/A";
	
	public static String normalizeSymbol(String symbol) {
		// TRTN^A => TRTN-A
		// BRK/A  => BRK.A
		return symbol.replace('^', '-').replace('/', '.');
	}
	
	public static String encodeSymbolForURL(String symbol) {
		// TRTN-A => TRTN%5EA
		// RDS.B  => RDS%25sl%25B
		return symbol.replace("-", "%5E").replace(".", "%25sl%25");
	}
	
	public static String convertDate(String string) {
		string = string.trim().replace(NOT_AVAILABLE, "");
		if (string.isEmpty()) return "";
		
		// 12/27/2021 => 2021-12-07
		String[] mdy = string.split("\\/");
		if (mdy.length != 3) {
			logger.error("Unpexpected");
			logger.error("  date {}", string);
			throw new UnexpectedException("Unpexpected");
		}
		return mdy[2] + "-" + mdy[0] + "-" + mdy[1];
	}
	
	public static <E> E getInstance(Class<E> clazz, String url) {
		var string = HttpUtil.getInstance().downloadString(url);
		return JSON.unmarshal(clazz, string);
	}
	
	public static String download(String url, File file) {
		String ret;
		if (file.exists()) {
			ret = FileUtil.read().file(file);
		} else {
			ret = HttpUtil.getInstance().downloadString(url);
			FileUtil.write().file(file, ret);
		}
		return ret;
	}
	public static String download(String url, String path) {
		return download(url, path == null ? null : new File(path));
	}
	public static <E> E getInstance(Class<E> clazz, String url, String path) {
		String string = download(url, path);
		return string == null ? null : JSON.unmarshal(clazz, string);
	}
}
