package yokwe.finance.data.provider.jpx;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OHLCVDateTime implements Comparable<OHLCVDateTime> {
	public LocalDateTime dateTime;
	public BigDecimal    open;
	public BigDecimal    high;
	public BigDecimal    low;
	public BigDecimal    close;
	public long          volume;
	
	public OHLCVDateTime(LocalDateTime dateTime, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, long volume) {
		this.dateTime = dateTime;
		this.open     = open;
		this.high     = high;
		this.low      = low;
		this.close    = close;
		this.volume   = volume;
	}
	
	public boolean isEmpty() {
		return
			open.compareTo(BigDecimal.ZERO) == 0 &&
			high.compareTo(BigDecimal.ZERO) == 0 &&
			low.compareTo(BigDecimal.ZERO) == 0 &&
			close.compareTo(BigDecimal.ZERO) == 0 &&
			volume == 0;
	}
	
	@Override
	public String toString() {
		return String.format("{%s %s %s %s %s %d}",
			dateTime.toString(), open.toPlainString(), high.toPlainString(), low.toPlainString(), close.toPlainString(), volume);
	}
	@Override
	public int compareTo(OHLCVDateTime that) {
		return this.dateTime.compareTo(that.dateTime);
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof OHLCVDateTime) {
			var that = (OHLCVDateTime)o;
			return
				this.dateTime.equals(that.dateTime) &&
				this.open.equals(that.open) &&
				this.high.equals(that.high) &&
				this.low.equals(that.low) &&
				this.close.equals(that.close) &&
				this.volume == that.volume;
		} else {
			return false;
		}
	}
}
