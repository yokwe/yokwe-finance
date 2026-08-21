package yokwe.finance.data.provider.webull;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.nasdaq.StorageNASDAQ;
import yokwe.finance.data.type.StockCodeNameUS;
import yokwe.util.Makefile;
import yokwe.util.UnexpectedException;
import yokwe.util.update.UpdateBase;

public class UpdateTradingStockUS extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static Makefile MAKEFILE = Makefile.builder().
			input(StorageNASDAQ.StockCodeNameNASDAQ).
			output(StorageWebull.TradingStockUSWebull).
			build();

	public static void main(String[] args) {
		callUpdate();
	}

	@Override
	public void update() {
		var map = StorageNASDAQ.StockCodeNameNASDAQ.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
		logger.info("map            {}", map.size());
		var stockInfoList = StorageWebull.StockInfoWebull.getList();
		logger.info("stockInfoList  {}", stockInfoList.size());

		var list = new ArrayList<StockCodeNameUS>();

		for(var e: stockInfoList) {
			var code = e.code;

			var pos = code.indexOf(" ");
			if (pos != -1) {
				// fix code
				var base = code.substring(0, pos);
				var suffix = code.substring(pos + 1);

				var newSuffix = SUFFIX_MAP.get(suffix);
				if (newSuffix == null) {
					logger.error("Unexpected suffix");
					logger.error("  {}", e);
					throw new UnexpectedException("Unexpected suffix");
				}
				code = base + newSuffix;
			}

			if (map.containsKey(code)) {
				// expected
			} else {
				// skip rights, units and warrants
				if (e.exchange.equals("NASDAQ") && code.length() == 5) {
					var letter5 = code.substring(4, 5);
					if (letter5.equals("R")) {
						// rights
						logger.info("skip rights    {}", e);
//						continue;
					}
					if (letter5.equals("U")) {
						// units
//						logger.info("skip units     {}", e);
						continue;
					}
					if (letter5.equals("W")) {
						// warrants
//						logger.info("skip warrants  {}", e);
						continue;
					}
				}

				// unexpected
				logger.warn("  Unexpecetd code  {}", e);
				continue;
			}

			list.add(map.get(code));
		}

		save(list, StorageWebull.TradingStockUSWebull); // use save for make
	}

	private static Map<String, String> SUFFIX_MAP = Map.ofEntries(
			Map.entry("A", ".A"),
			Map.entry("B", ".B"),
			Map.entry("V", ".V"),
			Map.entry("PR",  "-"),
			Map.entry("PRA", "-A"),
			Map.entry("PRB", "-B"),
			Map.entry("PRC", "-C"),
			Map.entry("PRD", "-D"),
			Map.entry("PRE", "-E"),
			Map.entry("PRF", "-F"),
			Map.entry("PRG", "-G"),
			Map.entry("PRH", "-H"),
			Map.entry("PRI", "-I"),
			Map.entry("PRJ", "-J"),
			Map.entry("PRK", "-K"),
			Map.entry("PRL", "-L"),
			Map.entry("PRM", "-M"),
			Map.entry("PRN", "-N"),
			Map.entry("PRO", "-O"),
			Map.entry("PRP", "-P"),
			Map.entry("PRQ", "-Q"),
			Map.entry("PRR", "-R"),
			Map.entry("PRS", "-S"),
			Map.entry("PRU", "-U"),
			Map.entry("PRV", "-V"),
			Map.entry("PRX", "-X"),
			Map.entry("PRY", "-Y"),
			Map.entry("PRZ", "-Z")
		);

}

