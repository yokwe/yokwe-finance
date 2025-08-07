package yokwe.finance.data.provider.jpx;

import yokwe.util.json.JSON.Ignore;

public class StockList {
	public static class Data {
        public String BICD;      // "1301"
        public String DPP;       // 現在値 "4,110"
        public String DPPT;      // 売買時刻 "15:30"
        public String DV;        // 出来高 1000株 "31.8"
        @Ignore
        public String DYRP;      // 前日比パーセント "+0.12"
        @Ignore
        public String DYWP;      // 前日比 "+5"
        @Ignore
        public String EXDV;      // "0000"
        public String FLLN;      // 名前 "極洋"
        @Ignore
        public String FLLNE;     // 名前英語 "KYOKUYO CO., LTD."
        @Ignore
        public String JSEC;      // sector33Code "50"
        public String LISS;      // "ﾌﾟﾗｲﾑ"
        @Ignore
        public String LISSE;     // "Prime"
        @Ignore
        public String PSTS;      // ""
        @Ignore
        public String TTCODE;    // "1301/T"
        @Ignore
        public String TTCODE2;   // "1301"
        public String ZXD;       // 売買日付 "2025/02/26"
        @Ignore
        public int    line_no;   // レコード番号 1
        @Ignore
        public String JSEC_CNV;  // sector33 "水産・農林業"
        @Ignore
        public String JSECE_CNV; // sector33英語 "Fishery, Agriculture & Forestry"
        @Ignore
        public int    DYWP_FLG;  // 1
        @Ignore
        public int    DYRP_FLG;  // 1
        @Ignore
        public String LISS_CNV;  // 市場 "プライム"
        @Ignore
        public String LISSE_CNV; // 市場英語 "Prime"
        @Ignore
        public String EXDV_CNV;  // ""
        @Ignore
        public String EXDVE_CNV; // ""
        @Ignore
        public String PSTSE;     // ""
        public String ROE;       // "11.1"
        public String PER;       // "7.48"
        public String PBR;       // "0.83"
	}
	
	public static class Section1 {
		public int    currentpage;
		public Data[] data;
		public int    hitcount;
		public int    pagecount;
		public int    recordcount;
		public int    status;
		public String type;
	}

	public String   cputime;
	public Section1 section1;
	public int      status;
	public String   ver;
	@Ignore
	public Object   urlparam;
}