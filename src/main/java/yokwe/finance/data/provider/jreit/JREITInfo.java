package yokwe.finance.data.provider.jreit;

import java.time.LocalDate;

import yokwe.finance.data.type.StockInfoJP;
import yokwe.util.ToString;

public class JREITInfo implements Comparable<JREITInfo> {
	public static final String CATEGORY_INFRA = "INFRA FUND";

	// https://www.japan-reit.com/meigara/8954/info/
	// 上場日 2002/06/12
	
	public String    stockCode;
	public LocalDate listingDate;
	public int       divFreq;
	public String    category;
	public String    name;

	public JREITInfo(String stockCode, LocalDate listingDate, int divFreq, String category, String name) {
		this.stockCode   = stockCode;
		this.listingDate = listingDate;
		this.divFreq     = divFreq;
		this.category    = category;
		this.name        = name;
	}
	public JREITInfo() {}
	
	public boolean isInfra() {
		return category.equals(CATEGORY_INFRA);
	}
	public StockInfoJP.Type toType() {
		return isInfra() ? StockInfoJP.Type.INFRA : StockInfoJP.Type.REIT;
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}

	@Override
	public int compareTo(JREITInfo that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	
	@Override
	public int hashCode() {
		return stockCode.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o != null) {
			if (o instanceof JREITInfo) {
				JREITInfo that = (JREITInfo)o;
				return
					this.stockCode.equals(that.stockCode) &&
					this.listingDate.equals(that.listingDate) &&
					this.divFreq == that.divFreq &&
					this.category.equals(that.category) &&
					this.name.equals(that.name);
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
