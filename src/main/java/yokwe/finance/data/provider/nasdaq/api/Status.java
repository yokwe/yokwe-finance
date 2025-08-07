package yokwe.finance.data.provider.nasdaq.api;

import yokwe.util.ToString;

public class Status {
	public static class CodeMessage {
		public int    code         = 0;
		public String errorMessage = "";
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
	public int           rCode;
	public CodeMessage[] bCodeMessage;
	public String        developerMessage;
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}
