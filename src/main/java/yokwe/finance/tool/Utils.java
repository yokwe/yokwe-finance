package yokwe.finance.tool;

import yokwe.util.UnexpectedException;

public class Utils {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static String toAntTarget(Makefile makefile) {
		var names = makefile.clazz.getTypeName().toLowerCase().split("\\.");
		var name3 = names[names.length - 3];
		var name2 = names[names.length - 2];
		var name1 = names[names.length - 1];
		
		var buffer = new StringBuilder((name2.equals("jp") || name2.equals("us")) ? (name3 + "-" + name2) : name2);
		var string = name1;
		
		for(;;) {
			boolean modified = false;
			for(var token: tokens) {
				if (!string.startsWith(token)) continue;
				buffer.append("-").append(token);
				string = string.substring(token.length());
				modified = true;
				break;
			}
			if (string.isEmpty()) break;
			if (modified) continue;
			
			logger.error("Unexpected string");
			logger.error("  clazz   {}", makefile.clazz.getTypeName());
			logger.error("  string  {}", string);
			throw new UnexpectedException("Unexpected string");
		}
		
		return buffer.toString();
	}
	private static String[] tokens = {
		"update", "stock", "info", "fund", "div", "price", "nisa", "etf", "etn", "infra",
		"kessan", "reit", "code", "name", "detail", "json", "list", "ohlcv", "value", "jreit",
		"trading", "jp", "us", "company", "all", "fx", "rate", "2", "intra", "day", "report",
	};
	
	public static String toMakeGroup(Makefile makefile) {
		return makefile.clazz.getPackageName().replace("yokwe.finance.", "").replace(".", "-");
	}
}
