package yokwe.finance.data.type;

public class NISAInfo implements Comparable<NISAInfo> {
	public String  isinCode;
	public boolean tsumitate;
	public String  name;
	
	public NISAInfo(String stockCode, boolean tsumitate, String name) {
		this.isinCode  = stockCode;
		this.tsumitate = tsumitate;
		this.name      = name;
	}
	public NISAInfo() {}
	
	@Override
	public String toString() {
		return String.format("{%s  %s  %s}", isinCode, tsumitate ? "1" : "0", name);
	}
	@Override
	public int compareTo(NISAInfo that) {
		return this.isinCode.compareTo(that.isinCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof NISAInfo) {
			NISAInfo that = (NISAInfo)o;
			return
				this.isinCode.equals(that.isinCode) &&
				this.tsumitate == that.tsumitate;
			// ignore name
		} else {
			return false;
		}
	}
}
