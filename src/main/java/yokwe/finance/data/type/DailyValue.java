package yokwe.finance.data.type;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailyValue implements Comparable<DailyValue> {
	public LocalDate  date;
	public BigDecimal value;
	
	public DailyValue(LocalDate date, BigDecimal value) {
		this.date  = date;
		this.value = value;
	}
	
	public LocalDate getKey() {
		return date;
	}
	
	@Override
	public String toString() {
		return String.format("{%s %s}", date.toString(), value.toPlainString());
	}
	@Override
	public int compareTo(DailyValue that) {
		return this.date.compareTo(that.date);
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof DailyValue) {
			DailyValue that = (DailyValue)o;
			return
				this.date.equals(that.date) &&
				this.value.equals(that.value);
		} else {
			return false;
		}
	}
}
