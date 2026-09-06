package yokwe.finance.data.provider.jpx;

import java.util.Map;

import yokwe.util.ToString;
import yokwe.util.json.JSON.Ignore;
import yokwe.util.json.JSON.Optional;

public class StockIntra {
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

	public static class Data {
		//
        public String DPG;          // "0058"

        @Optional public String HISTMDATE1;   // "2026/08/28"
        @Optional public String HISTMDATE2;   // "2026/08/27"
        @Optional public String HISTMDATE3;   // "2026/08/26"
        @Optional public String HISTMDATE4;   // "2026/08/25"
        @Optional public String HISTMDATE5;   // "2026/08/24"
        @Optional public String HISTMDATE6;   // "2026/08/21"
        @Optional public String HISTMDATE7;   // "2026/08/20"
        @Optional public String HISTMDATE8;   // "2026/08/19"
        @Optional public String HISTMDATE9;   // "2026/08/18"
        @Optional public String HISTMDATE10;  // "2026/08/17"

        @Optional public String[][] HISTMIN1;
        @Optional public String[][] HISTMIN2;
        @Optional public String[][] HISTMIN3;
        @Optional public String[][] HISTMIN4;
        @Optional public String[][] HISTMIN5;
        @Optional public String[][] HISTMIN6;
        @Optional public String HISTMIN7;
        @Optional public String HISTMIN8;
        @Optional public String HISTMIN9;
        @Optional public String HISTMIN10;
		@Ignore   public String HISTMIN11;

        public String LOSH;     // "10"          売買単位
        public String MPFU;     // "0.1"
        public String NAME;     // "NFTOPIX"
        public String PRP;      // "429.4"       前日終値
        public String TTCODE;   // "1306/T"
        public String TTCODE2;  // "1306"
        public String TZ;       // "+09:00"
        public String ZXD;      // "2026/08/28"  売買日付

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
