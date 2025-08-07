package yokwe.finance.data.provider.nasdaq.api;

import yokwe.util.ToString;

public final class LabelValue {
	public String label;
	public String value;
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}