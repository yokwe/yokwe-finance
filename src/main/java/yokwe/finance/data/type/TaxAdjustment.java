package yokwe.finance.data.type;

import yokwe.util.ToString;

public class TaxAdjustment implements Comparable<TaxAdjustment> {
	// 二重課税調整制度の対象
	public static String NO_VALUE = "#N/A";

	public String stockCode;
	public String isinCode;
	public String area;
	public String sector;
	public String name;

	public boolean hasValue() {
		return !isinCode.equals(NO_VALUE);
	}

	@Override
	public int compareTo(TaxAdjustment that) {
		return this.stockCode.compareTo(that.stockCode);
	}

	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}
