package yokwe.finance.data.provider.webull;

import yokwe.util.CSVUtil;
import yokwe.util.ToString;

public class StockInfo implements Comparable<StockInfo>{
	// 銘柄コード,銘柄名,取引所
	@CSVUtil.ColumnName("銘柄コード")
	public String code;
	@CSVUtil.ColumnName("銘柄名")
	public String name;
	@CSVUtil.ColumnName("取引所")
	public String exchange;

	public StockInfo(String code, String name, String exchange) {
		this.code     = code;
		this.name     = name;
		this.exchange = exchange;
	}

    @Override
    public String toString() {
        return ToString.withFieldName(this);
    }

	@Override
	public int compareTo(StockInfo that) {
		return this.code.compareTo(that.code);
	}
}
