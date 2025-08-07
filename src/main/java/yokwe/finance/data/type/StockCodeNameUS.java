package yokwe.finance.data.type;

import yokwe.finance.data.type.StockInfoUS.Market;
import yokwe.finance.data.type.StockInfoUS.Type;
import yokwe.util.ToString;

public class StockCodeNameUS implements Comparable<StockCodeNameUS> {
	public String	stockCode; // normalized symbol like TRNT-A and RDS.A not like TRTN^A and RDS/A
	public Market	market;
	public Type		type;
	public String	name;
	
	public StockCodeNameUS(String stockCode, Market market, Type type, String name) {
		this.stockCode = stockCode;
		this.market    = market;
		this.type      = type;
		this.name      = name;
	}

	@Override
	public int compareTo(StockCodeNameUS that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	

}
