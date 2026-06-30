package yokwe.finance.data.fund.jp;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.jita.StorageJITA;
import yokwe.finance.data.provider.moneybu.StorageMoneybu;
import yokwe.util.Makefile;
import yokwe.util.UnexpectedException;
import yokwe.util.update.UpdateBase;

public class UpdateFundDiv extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	protected static Makefile MAKEFILE = Makefile.builder().
		input(StorageJP.FundInfo, StorageJITA.FundDiv, StorageMoneybu.StockInfo).
		output(StorageJP.FundDiv).
		build();

	public static void main(String[] args) {
		callUpdate();
	}

	@Override
	public void update() {
		var fundList     = StorageJP.FundInfo.getList();
		var stockInfoMap = StorageMoneybu.StockInfo.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));

		for(var e: fundList) {
			var stockCode = e.stockCode;
			if (stockCode.isEmpty()) {
				continue; // not ETF
			}
			var stockInfo = stockInfoMap.get(stockCode);
			if (stockInfo == null) {
				// must be delisted etf
				logger.warn("Unexpected stockCode  {}  {}", e.stockCode, e.name);
				continue;
			}

			var isinCode  = e.isinCode;
			var divList   = StorageJITA.FundDiv.getList(isinCode);

			// quick check
			BigDecimal divValue = null;
			if (divList.isEmpty() || stockInfo.hasZeroDivValue()) {
				// OK
			} else {
				for(var ee: divList) {
					if (ee.date.isEqual(stockInfo.lastDivDate)) {
						divValue = ee.value;
						break;
					}
				}
			}

			if (divValue != null) {
				// sanity check of stockOnfo
				if (!stockInfo.hasValidDivDate()) {
					logger.error("Unexpected !hasValidDivDate");
					logger.error("  {}  {}  {}", stockInfo.stockCode, stockInfo.isinCode, stockInfo.name);
					throw new UnexpectedException("Unexpected !hasValidDivDate");
				}
				if (!stockInfo.hasValidDivValue()) {
					logger.error("Unexpected !hasValidDivValue");
					logger.error("  {}  {}  {}", stockInfo.stockCode, stockInfo.isinCode, stockInfo.name);
					throw new UnexpectedException("Unexpected !hasValidDivValue");
				}

				// adjust divList
				BigDecimal myValue = null;
				for(var ee: divList) {
					if (ee.date.isEqual(stockInfo.lastDivDate)) {
						myValue = ee.value;
					}
				}
				if (myValue == null) {
					logger.error("no lastDivDate");
					logger.error("  {}  {}  {}", stockInfo.stockCode, stockInfo.isinCode, stockInfo.name);
					logger.error("  {}", stockInfo.lastDivDate);
					throw new UnexpectedException("no lastDivDate");
				}

				var factor = stockInfo.lastDivValue.divide(myValue);
//				logger.info("{}  {}  {}  {}  {}", stockInfo.stockCode, stockInfo.isinCode, factor.toPlainString(), stockInfo.divYield, stockInfo.name);
				// modify divList with factor
				for(var ee: divList) {
					ee.value = ee.value.multiply(factor);
				}
			}

			StorageJP.FundDiv.save(isinCode, divList);
		}

		StorageJP.FundDiv.touch();
	}
}
