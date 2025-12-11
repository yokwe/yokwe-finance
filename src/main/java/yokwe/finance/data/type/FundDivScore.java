package yokwe.finance.data.type;

import java.math.BigDecimal;

import yokwe.util.ToString;

public class FundDivScore implements Comparable<FundDivScore> {
	public static BigDecimal NO_VALUE = BigDecimal.ONE.negate();
	
	public static boolean isValid(BigDecimal value) {
		return value.compareTo(NO_VALUE) != 0;
	}
	
	public String     isinCode;
	public BigDecimal score1Y;
	public BigDecimal score3Y;
	public BigDecimal score5Y;
	public BigDecimal score10Y;
	
	public FundDivScore(String isinCode, BigDecimal score1Y, BigDecimal socre3Y, BigDecimal socre5Y, BigDecimal score10Y) {
		this.isinCode = isinCode;
		this.score1Y  = score1Y;
		this.score3Y  = socre3Y;
		this.score5Y  = socre5Y;
		this.score10Y = score10Y;
	}
	public FundDivScore(String isinCode) {
		this.isinCode = isinCode;
		this.score1Y  = null;
		this.score3Y  = null;
		this.score5Y  = null;
		this.score10Y = null;
	}
	
	@Override
	public int compareTo(FundDivScore that) {
		return this.isinCode.compareTo(that.isinCode);
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}
