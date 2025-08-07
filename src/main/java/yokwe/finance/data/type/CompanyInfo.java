package yokwe.finance.data.type;

import yokwe.util.ToString;

public class CompanyInfo implements Comparable<CompanyInfo> {
	public String stockCode;
	public String sector;
	public String industry;	
	
	public CompanyInfo(String stockCode, String sector, String industry) {
		this.stockCode = stockCode;
		this.sector    = sector;
		this.industry  = industry;
	}
	public CompanyInfo() {}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	@Override
	public int compareTo(CompanyInfo that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof CompanyInfo) {
			CompanyInfo that = (CompanyInfo)o;
			return
				this.stockCode.equals(that.stockCode) &&
				this.sector.equals(that.sector) &&
				this.industry.equals(that.industry);
		} else {
			return false;
		}
	}

}
