package yokwe.finance.data.fund.jp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.jita.StorageJITA;
import yokwe.finance.data.provider.moneybu.StorageMoneybu;
import yokwe.util.Makefile;
import yokwe.util.UnexpectedException;
import yokwe.util.update.UpdateBase;

public class UpdateFundDivPrice extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	protected static Makefile MAKEFILE = Makefile.builder().
		input(StorageJITA.FundInfoJITA, StorageJITA.FundDiv, StorageJITA.FundPrice, StorageMoneybu.StockInfoMoneybu).
		output(StorageFundJP.FundDiv, StorageFundJP.FundPrice).
		build();

	public static void main(String[] args) {
		callUpdate();
	}

	@Override
	public void update() {
		update_mix();
	}

	public void update_mix() {
		var moneybuMap = StorageMoneybu.StockInfoMoneybu.getList().stream().collect(Collectors.toMap(o -> o.isinCode, Function.identity()));

		int countA = 0;
		int countB = 0;
		int countC = 0;
		int countD = 0;

		var fundInfoList = StorageJITA.FundInfoJITA.getList();
		logger.info("fundInfoList  {}", fundInfoList.size());

		for(var fundInfo: fundInfoList) {
			var isinCode  = fundInfo.isinCode;
			var stockCode = fundInfo.stockCode;
			var name      = fundInfo.name;

			var priceList = StorageJITA.FundPrice.getList(isinCode);
			var divList   = StorageJITA.FundDiv.getList(isinCode);

			if (stockCode.isEmpty()) {
				// FUND
				countA++;
			} else {
				// ETF
				var moneybu   = moneybuMap.get(isinCode);
				if (moneybu == null) {
					countB++;
				} else {
					//
					var priceDate  = moneybu.priceDate;
					var priceValue = moneybu.priceValue;

					var myPrice = priceList.stream().filter(o -> o.date.equals(priceDate)).findAny().orElse(null);
					if (myPrice == null) {
						//
						logger.error("Unexpected priceDate");
						logger.error("  {}  {}  {}  {}  {}", isinCode, stockCode, priceDate, name);
						throw new UnexpectedException("Unexpected priceDate");
					}
					var myValue = myPrice.price;
					var factor = getFactor(priceValue, myValue);
					logger.info("XX  {}  {}  {}  {}  {}  {}  {}", isinCode, stockCode, priceDate, priceValue, myValue, factor, name);
					if (factor.compareTo(BigDecimal.ONE) == 0) {
						// no need to adjust
						countC++;
					} else {
						// need adjust
						countD++;
						for(var e: priceList) {
							e.price = e.price.multiply(factor);
						}
						for(var e: divList) {
							e.value = e.value.multiply(factor);
						}
					}
				}
			}

			StorageFundJP.FundDiv.save(isinCode, divList);
			StorageFundJP.FundPrice.save(isinCode, priceList);
		}

		logger.info("countA  {}", countA);
		logger.info("countB  {}", countB);
		logger.info("countC  {}", countC);
		logger.info("countD  {}", countD);

		StorageFundJP.FundDiv.touch();
		StorageFundJP.FundPrice.touch();
	}

	BigDecimal getFactor(BigDecimal targetValue, BigDecimal myValue) {
		var factor3 = targetValue.divide(myValue, 3, RoundingMode.HALF_EVEN);
		var factor2 = factor3.setScale(2, RoundingMode.HALF_EVEN);
		var factor1 = factor3.setScale(1, RoundingMode.HALF_EVEN);
		var factor0 = factor3.setScale(0, RoundingMode.HALF_EVEN);

		BigDecimal factor;
		if (factor0.compareTo(BigDecimal.ZERO) == 0) {
			if (factor1.compareTo(BigDecimal.ZERO) == 0) {
				if (factor2.compareTo(BigDecimal.ZERO) == 0) {
					factor = factor3;
				} else {
					factor = factor2;
				}
			} else {
				factor = factor1;
			}
		} else {
			factor = factor0;
		}
		return factor;
	}
}