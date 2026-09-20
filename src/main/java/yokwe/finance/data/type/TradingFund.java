package yokwe.finance.data.type;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import yokwe.finance.data.provider.click.StorageClick;
import yokwe.finance.data.provider.nikko.StorageNikko;
import yokwe.finance.data.provider.rakuten.StorageRakuten;
import yokwe.finance.data.provider.smtb.StorageSMTB;
import yokwe.finance.data.provider.sony.StorageSony;

public class TradingFund implements Comparable<TradingFund> {
	public static final BigDecimal SALES_FEE_UNKNOWN = BigDecimal.valueOf(-1);


	private static Set<String> isinSet = isinCodeSet();
	private static Set<String> isinCodeSet() {
		var set = new HashSet<String>();

		set.addAll(StorageClick.TradingFundJPClick.getList().stream().map(o -> o.isinCode).toList());
		set.addAll(StorageNikko.TradingFundJPNikko.getList().stream().map(o -> o.isinCode).toList());
		set.addAll(StorageRakuten.TradingFundJPRakuten.getList().stream().map(o -> o.isinCode).toList());
		set.addAll(StorageSMTB.TradingFundJPSMTB.getList().stream().map(o -> o.isinCode).toList());
		set.addAll(StorageSony.TradingFundJPSony.getList().stream().map(o -> o.isinCode).toList());

		return set;
	}
	public static boolean isTradingFund(String isinCode) {
		return isinSet.contains(isinCode);
	}


	public String     isinCode;
	public BigDecimal salesFee;  // 0 for no load
	public String     name;

	public TradingFund(String isinCode, BigDecimal salesFee, String name) {
		this.isinCode = isinCode;
		this.salesFee = salesFee;
		this.name     = name;
	}

	@Override
	public String toString() {
		return String.format("{%s  %s  %s}", isinCode, salesFee, name);
	}
	@Override
	public int compareTo(TradingFund that) {
		return this.isinCode.compareTo(that.isinCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof TradingFund) {
			var that = (TradingFund)o;
			return
				this.isinCode.equals(that.isinCode) &&
				this.salesFee.compareTo(that.salesFee) == 0;
			// ignore name
		}
		return false;
	}

	@Override
	public int hashCode() {
		return isinCode.hashCode();
	}
}
