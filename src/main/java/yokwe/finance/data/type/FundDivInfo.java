package yokwe.finance.data.type;

import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.util.ToString;

public class FundDivInfo implements Comparable<FundDivInfo> {
	public static final BigDecimal NO_VALUE = BigDecimal.ONE.negate();

	public static boolean isValid(BigDecimal value) {
		return value.compareTo(NO_VALUE) != 0;
	}

	public static final LocalDate NO_DATE = LocalDate.of(2099, 1, 1);

	public static boolean isValid(LocalDate value) {
		return value.compareTo(NO_DATE) != 0;
	}


	public String     isinCode;
	public String     fundCode;
	public String     stockCode;
	public BigDecimal score1Y;
	public BigDecimal score3Y;
	public BigDecimal score5Y;
	public BigDecimal score10Y;
	public LocalDate  divDate;
	public BigDecimal divValue;
	public BigDecimal divPrice;
	public BigDecimal divYield;
	public String     name;

	public FundDivInfo(
		String isinCode, String fundCode, String stockCode,
		BigDecimal score1Y, BigDecimal socre3Y, BigDecimal socre5Y, BigDecimal score10Y,
		LocalDate divDate, BigDecimal divValue, BigDecimal divPrice, BigDecimal divYield,
		String name) {
		this.isinCode  = isinCode;
		this.fundCode  = fundCode;
		this.stockCode = stockCode;

		this.score1Y  = score1Y;
		this.score3Y  = socre3Y;
		this.score5Y  = socre5Y;
		this.score10Y = score10Y;

		this.divDate  = divDate;
		this.divValue = divValue;
		this.divPrice = divPrice;
		this.divYield = divYield;

		this.name      = name;
	}

	public boolean hasScore1Y() {
		return isValid(score1Y);
	}
	public boolean hasScore3Y() {
		return isValid(score3Y);
	}
	public boolean hasScore5Y() {
		return isValid(score5Y);
	}
	public boolean hasScore10Y() {
		return isValid(score10Y);
	}

	public boolean hasDivDate() {
		return isValid(divDate);
	}
	public boolean hasDivValue() {
		return isValid(divValue);
	}
	public boolean hasDivPrice() {
		return isValid(divPrice);
	}
	public boolean hasDivYield() {
		return isValid(divYield);
	}

	@Override
	public int compareTo(FundDivInfo that) {
		return this.isinCode.compareTo(that.isinCode);
	}

	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}
