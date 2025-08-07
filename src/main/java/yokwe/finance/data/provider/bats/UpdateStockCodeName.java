package yokwe.finance.data.provider.bats;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import yokwe.finance.data.type.StockCodeNameUS;
import yokwe.finance.data.type.StockInfoUS.Market;
import yokwe.finance.data.type.StockInfoUS.Type;
import yokwe.util.CSVUtil;
import yokwe.util.FTPUtil;
import yokwe.util.MarketHoliday;
import yokwe.util.UnexpectedException;
import yokwe.util.makefile.Makefile;
import yokwe.util.update.UpdateList;

public class UpdateStockCodeName extends UpdateList<ListedSecurityReport> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input().
		output(StorageBATS.StockCodeName).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	protected List<ListedSecurityReport> downloadFile() {
		var lastTradingDate = MarketHoliday.US.getLastTradingDate();
		logger.info("lastTradingDate  {}", lastTradingDate);

		String urlString = String.format(
			"ftp://ftp.batstrading.com/bzx-equities/listed-securities/bzx_equities_listed_security_rpt_%d%02d%02d.txt",
			lastTradingDate.getYear(), lastTradingDate.getMonthValue(), lastTradingDate.getDayOfMonth());
		
		var dataString = FTPUtil.downloadString(urlString);
		if (dataString == null) {
			logger.error("Download failed  {}", urlString);
			throw new UnexpectedException("Download failed");
		}
		
		String[] lines = dataString.split("[\\r\\n]+");
		
		// remove first line
		var string = String.join("\n", Arrays.copyOfRange(lines, 1, lines.length)) + "\n";

		logger.info("string  {}", string.length());
		
		var list = CSVUtil.read(ListedSecurityReport.class).withSeparator('|').file(new StringReader(string));
		StorageBATS.ListedSecurityReport.save(list);
		
		return list;
	}
	private static final Map<String, Type> typeMap = Map.ofEntries(
		Map.entry("Commodity-Based Trust Shares",   Type.ETF),
		Map.entry("Exchange-Traded Fund Shares",    Type.ETF),
		Map.entry("Managed Portfolio Shares",       Type.ETF),
		Map.entry("Tracking Fund Shares",           Type.ETF),
		Map.entry("Trust Issued Receipts",          Type.ETF),
		//
		Map.entry("Equity Index-Linked Securities", Type.ETN),
		Map.entry("Futures-Linked Securities",      Type.ETN),
		//
		Map.entry("Primary Equity",                 Type.COMMON),
		//
		Map.entry("Warrant",                        Type.WARRANT)
	);
	
	@Override
	protected void updateFile(List<ListedSecurityReport> reportList) {		
		var stockList = new ArrayList<StockCodeNameUS>(reportList.size());
		
		int countSkip = 0;
		
		for(var e: reportList) {
			if (e.isTestSymbol()) {
				logger.info("skip  test symbol          {}  {}", e.symbol, e.issueName);
				countSkip++;
				continue;
			}
//			if (!e.isFinancialNormal()) {
//				logger.info("skip  financial status  {}  {}  {}", e.financialStatus, e.symbol, e.issueName);
//				countSkip++;
//				continue;
//			}
			
			String symbol   = e.symbol;
			Market market   = Market.BATS;
			Type   type     = typeMap.get(e.issueType);
			String name     = e.issueName.replace(",", "").toUpperCase(); // use upper case
			
			if (type == null) {
				logger.error("Unexpected issueType");
				logger.error("  symbol     {}!", e.symbol);
				logger.error("  issueType  {}!", e.issueType);
				logger.error("  issueName  {}!", e.issueName);
				throw new UnexpectedException("Unexpected issueType");
			}

			stockList.add(new StockCodeNameUS(symbol, market, type, name));
		}
		logger.info("skip  {}", countSkip);
		
		// sanity check
		checkDuplicateKey(stockList, o -> o.stockCode);
		
		// save csv file
		save(stockList, StorageBATS.StockCodeName); // use save for make
	}
}
