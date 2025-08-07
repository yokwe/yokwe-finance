package yokwe.finance.data.provider.jpx;

import java.util.Map;

import yokwe.util.ToString;
import yokwe.util.json.JSON.Ignore;

public class StockIntraPrice {
	public static class Data {
        public String DPG; // "0058",
        
        public String HISTMDATE1;  // "2025/04/08",
        public String HISTMDATE2;  // "2025/04/07",
        public String HISTMDATE3;  // "2025/04/04",
        public String HISTMDATE4;  // "2025/04/03",
        public String HISTMDATE5;  // "2025/04/02",
        public String HISTMDATE6;  // "2025/04/01",
        public String HISTMDATE7;  // "2025/03/31",
        public String HISTMDATE8;  // "2025/03/28",
        public String HISTMDATE9;  // "2025/03/27",
        public String HISTMDATE10; // "2025/03/26",
        
        public String[][] HISTMIN1;
        public String[][] HISTMIN2;
        public String[][] HISTMIN3;
        public String[][] HISTMIN4;
        public String[][] HISTMIN5;
        public String[][] HISTMIN6;
        public String HISTMIN7;
        public String HISTMIN8;
        public String HISTMIN9;
        public String HISTMIN10;
        @Ignore
        public String HISTMIN11;
        
        public String LOSH;    //"100",
        public String MPFU;    // "0.1",
        public String NAME;    // "ＮＴＴ",
        public String PRP;     // "138.9",
        public String TTCODE;  // "9432/T",
        public String TTCODE2; // "9432",
        public String TZ;      // "+09:00",
        public String ZXD;     //"2025/04/08"
        
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
	public static class Section1 {
		public Map<String, Data> data;
		public int               hitcount;
		public int               status;
		public String            type;
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}

	public String   cputime;
	public Section1 section1;
	public int      status;
	public String   ver;
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}