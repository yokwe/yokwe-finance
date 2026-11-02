package yokwe.finance.data.provider.prestia;

import java.math.BigDecimal;

import yokwe.finance.data.type.Currency;
import yokwe.util.ToString;

public class TradingFundPrestia implements Comparable<TradingFundPrestia> {
	public String fundCode;  // prestia fund code

	public Currency currency;

	public String investArea;
	public String investTarget;

	public int buyable;
	public int buyableOnly;

	public BigDecimal totalNetAsset;

	public BigDecimal purchaseFee;  // 申込手数料
    public BigDecimal trustFee;     // 信託/管理報酬等
    public BigDecimal trustReserve; // 信託財産留保額
    public String     successFee;   // 成功報酬

	public String name;

	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}

	@Override
	public int compareTo(TradingFundPrestia that) {
		return this.fundCode.compareTo(that.fundCode);
	}

}
