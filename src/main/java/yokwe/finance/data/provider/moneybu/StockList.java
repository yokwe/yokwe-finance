package yokwe.finance.data.provider.moneybu;

import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.util.ToString;

public class StockList implements Comparable<StockList> {
	public String     code;
	public String     name;

	public String     exType;        // "ETF" or "ETN"
	public BigDecimal managementFee; // 信託報酬 unit is percent
	public int        dividendNum;
	public String     category;      // 国内株ETF
	public String     productType;   // TOPIX
	public String     targetIndex;   // ＴＯＰＩＸ（配当込み）
	public int        tradeUnit;     // 売買単位
	public int        marketMake;    // 0 => NO  1 => YES

	public LocalDate  date;
	public BigDecimal price;
	public BigDecimal dividendYield; // 分配金利回り unit is percent
	public BigDecimal netAssets;     // 14,512,167,025,398  145,121.7億円
	public BigDecimal y1return;
	public BigDecimal deviation;     // 乖離率 unit is percent

	@Override
	public int compareTo(StockList that) {
		return this.code.compareTo(that.code);
	}

	@Override
	public String toString() {
	    return ToString.withFieldName(this);
	}
}
