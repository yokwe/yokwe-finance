package yokwe.finance.data.type;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OHLCV implements Comparable<OHLCV> {
	public LocalDate  date;
	public BigDecimal open;
	public BigDecimal high;
	public BigDecimal low;
	public BigDecimal close;
	public long       volume;
	
	public OHLCV(LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, long volume) {
		this.date   = date;
		this.open   = open;
		this.high   = high;
		this.low    = low;
		this.close  = close;
		this.volume = volume;
	}
	public OHLCV() {}
	
	public boolean isEmpty() {
		return
			open.compareTo(BigDecimal.ZERO) == 0 &&
			high.compareTo(BigDecimal.ZERO) == 0 &&
			low.compareTo(BigDecimal.ZERO) == 0 &&
			close.compareTo(BigDecimal.ZERO) == 0 &&
			volume == 0;
	}
	
	public LocalDate getKey() {
		return date;
	}
	
	@Override
	public String toString() {
		return String.format("{%s %s %s %s %s %d}",
			date.toString(), open.toPlainString(), high.toPlainString(), low.toPlainString(), close.toPlainString(), volume);
	}
	@Override
	public int compareTo(OHLCV that) {
		return this.date.compareTo(that.date);
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof OHLCV) {
			OHLCV that = (OHLCV)o;
			return
				this.date.equals(that.date) &&
				this.open.equals(that.open) &&
				this.high.equals(that.high) &&
				this.low.equals(that.low) &&
				this.close.equals(that.close) &&
				this.volume == that.volume;
		} else {
			return false;
		}
	}
	public boolean equalsByValue(OHLCV that) {
		return
			this.date.equals(that.date) &&
			this.open.compareTo(that.open) == 0 &&
			this.high.compareTo(that.high) == 0 &&
			this.low.compareTo(that.low) == 0 &&
			this.close.compareTo(that.close) == 0 &&
			this.volume == that.volume;
	}
	
}
