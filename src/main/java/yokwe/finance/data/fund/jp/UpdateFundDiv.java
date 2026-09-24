package yokwe.finance.data.fund.jp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.jita.StorageJITA;
import yokwe.finance.data.provider.nikkei.StorageNikkei;
import yokwe.finance.data.type.DailyValue;
import yokwe.finance.data.type.FundDivInfo;
import yokwe.util.Makefile;
import yokwe.util.UnexpectedException;
import yokwe.util.update.UpdateBase;

public class UpdateFundDiv extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	protected static Makefile MAKEFILE = Makefile.builder().
		input(StorageFundJP.FundInfo, StorageJITA.FundDiv, StorageNikkei.FundDivInfo).
		output(StorageFundJP.FundDiv).
		build();

	public static void main(String[] args) {
		callUpdate();
	}

	@Override
	public void update() {
		var fundDivInfoMap = StorageNikkei.FundDivInfo.getList().stream().collect(Collectors.toMap(o -> o.isinCode, Function.identity()));

		int countA = 0;
		int countB = 0;
		int countC = 0;
		int countD = 0;
		int countE = 0;

		var fundInfoList = StorageFundJP.FundInfo.getList();
		logger.info("fundInfoList  {}", fundInfoList.size());

		for(var fundInfo: fundInfoList) {
			var divList     = StorageJITA.FundDiv.getList(fundInfo.isinCode);
			var fundDivInfo = fundDivInfoMap.get(fundInfo.isinCode);

			if (fundDivInfo == null) {
				countA++;
			} else {
				if (divList.isEmpty()) {
					countB++;
				} else if (allZero(divList)) {
					countC++;
				} else {
					var divDate  = fundDivInfo.divDate;
					var divPrice = fundDivInfo.divPrice;

					if (!FundDivInfo.isValid(divDate)) {
						logger.error("Unexpected divDate");
						logger.error("  {}  {}  {}", fundDivInfo.isinCode, fundDivInfo.stockCode, fundDivInfo.name);
						throw new UnexpectedException("Unexpected divDate");
					}
					if (!FundDivInfo.isValid(divPrice)) {
						logger.error("Unexpected divPrice");
						logger.error("  {}  {}  {}", fundDivInfo.isinCode, fundDivInfo.stockCode, fundDivInfo.name);
						throw new UnexpectedException("Unexpected divPrice");
					}

					var priceList = StorageJITA.FundPrice.getList(fundDivInfo.isinCode);
					var myPrice   = priceList.stream().filter(o -> o.date.equals(divDate)).map(o -> o.price).findFirst().orElse(null);
					if (myPrice == null) {
						logger.error("Unexpected divDate");
						logger.error("  {}  {}  {}  {}", fundDivInfo.isinCode, fundDivInfo.stockCode, divDate, fundDivInfo.name);
						throw new UnexpectedException("Unexpected divDate");
					}

					if (divPrice.compareTo(myPrice) == 0) {
						countD++;
					} else {
						countE++;
						// modify divList with factor
						var factor = divPrice.divide(myPrice, 3, RoundingMode.HALF_EVEN);
						logger.info("XX  {}  {}  {}  {}", fundInfo.isinCode, fundInfo.stockCode, factor.toPlainString(), fundInfo.name);
						// modify divList with factor
						for(var e: divList) {
							e.value = e.value.multiply(factor);
						}
					}
				}
			}

			StorageFundJP.FundDiv.save(fundInfo.isinCode, divList);
		}

		logger.info("countA  {}", countA);
		logger.info("countB  {}", countB);
		logger.info("countC  {}", countC);
		logger.info("countD  {}", countD);
		logger.info("countE  {}", countE);

		StorageFundJP.FundDiv.touch();
	}

	private boolean allZero(List<DailyValue> list) {
		for(var e: list) {
			if (e.value.compareTo(BigDecimal.ZERO) != 0) {
				return false;
			}
		}
		return true;
	}

}
