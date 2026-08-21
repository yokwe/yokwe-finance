package yokwe.finance.data.provider.nasdaq;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import yokwe.finance.data.type.StockCodeNameUS;
import yokwe.finance.data.type.StockInfoUS.Market;
import yokwe.finance.data.type.StockInfoUS.Type;
import yokwe.util.CSVUtil;
import yokwe.util.Makefile;
import yokwe.util.Storage;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.update.UpdateBase;

public class UpdateStockCodeName extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static Makefile MAKEFILE = Makefile.builder().
		input().
		output(StorageNASDAQ.StockCodeNameNASDAQ, StorageNASDAQ.NasdaqListed, StorageNASDAQ.OtherListed).
		build();

	public static void main(String[] args) {
		callUpdate();
	}

	@Override
	public void update() {
		// update NasdaqListedText if needed
		if (needsUpdate(StorageNASDAQ.NasdaqListed.getFile())) {
			logger.info("download nasdaq listed");
			var list = download(URL_NASDAQLISTED, StorageNASDAQ.NasdaqListedText, NasdaqListed.class);
			// sanity check
			checkDuplicateKey(list, o -> o.symbol);

			for(var e: list) {
				e.name = e.name.replace(",", "").toUpperCase();
			}
			save(list, StorageNASDAQ.NasdaqListed);
		}

		// update OtherListedText if needed
		if (needsUpdate(StorageNASDAQ.OtherListed.getFile())) {
			logger.info("download other listed");
			var list = download(URL_OTHERLISTED,  StorageNASDAQ.OtherListedText,  OtherListed.class);
			// sanity check
			checkDuplicateKey(list, o -> o.symbol);

			for(var e: list) {
				e.name = e.name.replace(",", "").toUpperCase();
			}
			save(list, StorageNASDAQ.OtherListed);
		}

		// sanity check
		var nasdaqList = StorageNASDAQ.NasdaqListed.getList();
		var otherList  = StorageNASDAQ.OtherListed.getList();

		var list = new ArrayList<StockCodeNameUS>(nasdaqList.size() + otherList.size());

		int countTotal = 0;
		int countSkip  = 0;

		// add NASDAQ
		for(var e: nasdaqList) {
			countTotal++;

			// skip test issue, right, unit and warrant
			if (e.isTestIssue() || e.isRights() || e.isUnits() || e.isWarrant()) {
				countSkip++;
				continue;
			}
			// skip warrant and beneficial interest
			var upperCaseName = e.name.toUpperCase();
			if (upperCaseName.contains("WARRANT") || upperCaseName.contains("BENEFICIAL INTEREST")) {
				countSkip++;
				continue;
			}
			// skip financial status is not normal
//			if (!e.isFinancialNormal()) {
//				logger.info("skip  financial status  {}  {}  {}", e.financialStatus, e.symbol, e.name);
//				countSkip++;
//				continue;
//			}

			String symbol   = e.symbol;
			Market market   = Market.NASDAQ;
			Type   type     = e.etf.equals("Y") ? Type.ETF : Type.COMMON; // just ETF or COMMON for now
			String name     = e.name;

			list.add(new StockCodeNameUS(symbol, market, type, name));
		}
		// add other
		for(var e: otherList) {
			countTotal++;

			// skip test issue, right, unit and warrant
			if (e.isTestIssue() || e.isRights() || e.isUnits() || e.isWarrant()) {
				countSkip++;
				continue;
			}
			// skip warrant and beneficial interest
			var upperCaseName = e.name.toUpperCase();
//			if (upperCaseName.contains("WARRANT") || upperCaseName.contains("BENEFICIAL INTEREST")) {
			if (upperCaseName.contains("WARRANT")) {
				countSkip++;
				continue;
			}
			// skip financial status is not normal
//			if (!e.isFinancialNormal()) {
//				logger.info("skip  financial status  {}  {}  {}", e.financialStatus, e.symbol, e.name);
//				countSkip++;
//				continue;
//			}

			String symbol = e.symbol;
			Market market = toMarket(e.exchange);
			Type   type   = e.etf.equals("Y") ? Type.ETF : Type.COMMON; // just ETF or COMMON for now
			String name   = e.name;

			list.add(new StockCodeNameUS(symbol, market, type, name));
		}

		logger.info("total  {}", countTotal);
		logger.info("skip   {}", countSkip);

		// sanity check
		checkDuplicateKey(list, o -> o.stockCode);

		// save
		save(list, StorageNASDAQ.StockCodeNameNASDAQ); // use save for make
	}

	private static Map<String, Market> marketMap = Map.ofEntries(
		//   A = NYSE MKT
		//   N = New York Stock Exchange (NYSE)
		//   P = NYSE ARCA
		//   Z = BATS Global Markets (BATS)
		//   V = Investors' Exchange, LLC (IEXG)
		Map.entry("A", Market.NYSE),
		Map.entry("N", Market.NYSE),
		Map.entry("P", Market.NYSE),
		Map.entry("Z", Market.BATS),
		Map.entry("V", Market.IEXG)
	);

	private static Market toMarket(String exchange) {
		if (marketMap.containsKey(exchange)) {
			return marketMap.get(exchange);
		}
		logger.error("Unexpected exchange");
		throw new UnexpectedException("Unexpected exchange");
	}

	private <E extends Comparable<E>> List<E> download(String url, Storage.LoadSaveFileString loadSaveText, Class<E> clazz) {
		String string;
		{
			byte[] data = HttpUtil.getInstance().downloadRaw(url);
			if (data == null) {
				logger.error("Download failed  {}", url);
				throw new UnexpectedException("Download failed");
			}

			string = new String(data, StandardCharsets.US_ASCII);

			// save txt file
			save(string, loadSaveText);
		}
		List<E>	list;
		{
			String[] lines = string.split("[\\r\\n]+");

			// remove last line
			String csvString = String.join("\n", Arrays.copyOfRange(lines, 0, lines.length - 1)) + "\n";
			// read string as csv file
			list = CSVUtil.read(clazz).withSeparator('|').file(new StringReader(csvString));
		}
		return list;
	}
	public static final String URL_NASDAQLISTED = "https://www.nasdaqtrader.com/dynamic/symdir/nasdaqlisted.txt";
	public static final String URL_OTHERLISTED  = "https://www.nasdaqtrader.com/dynamic/symdir/otherlisted.txt";
}
