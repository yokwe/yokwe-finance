package yokwe.finance.data.provider.jita;

import yokwe.util.CSVUtil;
import yokwe.util.ToString;

public class FundDivPrice implements Comparable<FundDivPrice> {
	// 年月日	        基準価額(円)	純資産総額（百万円）	分配金	決算期
	// 2022年04月25日	19239	        1178200	                0       4
	// 2022年04月26日	19167	        1174580   
	@CSVUtil.ColumnName("年月日")
	public String date;
	@CSVUtil.ColumnName("基準価額(円)")
	public String price;
	@CSVUtil.ColumnName("純資産総額（百万円）")
	public String nav;
	@CSVUtil.ColumnName("分配金")
	public String div;
	@CSVUtil.ColumnName("決算期")
	public String period;
	
	@Override
	public int compareTo(FundDivPrice that) {
		return this.date.compareTo(that.date);
	}
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}