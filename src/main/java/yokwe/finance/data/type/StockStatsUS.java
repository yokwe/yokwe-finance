package yokwe.finance.data.type;

import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.util.ToString;

public class StockStatsUS implements Comparable<StockStatsUS> {
	public String	  stockCode;    // normalized symbol like TRNT-A and RDS.A not like TRTN^A and RDS/A

	public String     stockType;    // STOCK ETF ADR

	// dividend
    public BigDecimal dividend;     // "0.147200"
    public LocalDate  divDate;      // "Wednesday July 01, 2026",
    public BigDecimal divYield;     // "7.02529"
    public int        divInt;       // 0  30  90  180  365

    // stats
    public BigDecimal beta;         // "0.2847"
    public BigDecimal volatility;   // ".039000"
    public BigDecimal rsi;          // "44.70347"

    // total return
    public BigDecimal oneMonth;     // "-0.04"
    public BigDecimal threeMonth;   // "-0.756"
    public BigDecimal sixMonth;     // "-1.966"
    public BigDecimal fiftyTwoWeek; // "-1.618"
    public BigDecimal threeYear;    // "0.609"

	public String	  name;

	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}

	@Override
	public int compareTo(StockStatsUS that) {
		return this.stockCode.compareTo(that.stockCode);
	}

}
