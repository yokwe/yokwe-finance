package yokwe.finance.data.type;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import yokwe.util.ToString;

public class StockValueJP implements Comparable<StockValueJP> {
	public static final LocalDate ZERO_DATE = LocalDate.of(1980,  1,  1);
	public static final LocalTime ZERO_TIME = LocalTime.of(0,  0);

	public String		stockCode;						// TTCODE2
	
	// from stock list
//	public BigDecimal	roe       = BigDecimal.ZERO;	// ROE
//	public BigDecimal	per       = BigDecimal.ZERO;	// PER
//	public BigDecimal	pbr       = BigDecimal.ZERO;	// PBR
	// from stock detail
	public LocalDate	date      = ZERO_DATE;			// ZXD
	public BigDecimal	open      = BigDecimal.ZERO;	// DOP
	public LocalTime	openTime  = ZERO_TIME;			// DOPT
	public BigDecimal	high      = BigDecimal.ZERO;	// DHP
	public LocalTime	highTime  = ZERO_TIME;			// DHPT
	public BigDecimal	low       = BigDecimal.ZERO;	// DLP
	public LocalTime	lowTime   = ZERO_TIME;			// DLPT
	public BigDecimal	price     = BigDecimal.ZERO;	// DPP
	public LocalTime	priceTime = ZERO_TIME;			// DPPT
	public long			volume    = 0;					// DV
	public long			issued    = 0;					// SHRK
	public BigDecimal	bid       = BigDecimal.ZERO;	// QBP
	public LocalTime	bidTime   = ZERO_TIME;			// QBPT
	public BigDecimal	ask       = BigDecimal.ZERO;	// QAP
	public LocalTime	askTime   = ZERO_TIME;			// QAPT
	public BigDecimal	previous  = BigDecimal.ZERO;	// PRP  -- previous close
	
	public StockValueJP() {}
	public StockValueJP(String stockCode) {
		this.stockCode = stockCode;
	}
	
	@Override
	public int compareTo(StockValueJP that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}
