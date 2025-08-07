package yokwe.finance.data.provider.rakuten;

import yokwe.util.ToString;

public class FundCodeName implements Comparable<FundCodeName> {
	public String isinCode; // isin code
	public String name;
	
	public FundCodeName(String isinCode, String name) {
		this.isinCode = isinCode;
		this.name     = name;
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	@Override
	public int compareTo(FundCodeName that) {
		return this.isinCode.compareTo(that.isinCode);
	}
}
