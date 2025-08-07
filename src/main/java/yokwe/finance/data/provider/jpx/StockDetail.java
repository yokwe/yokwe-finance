package yokwe.finance.data.provider.jpx;

import java.util.Map;

import yokwe.util.ToString;
import yokwe.util.json.JSON.Ignore;

public class StockDetail {
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
	
	public static class Index {
        public String YEAR;     // "2024/09"
        public String FY;       // "中間"
        public String FYE;      // "Company / Interim"
        public String EPS;      // "-"
        public String CPTLPSTK; // "-"
        public String ROE;      // "-"
        public String PER;      // "-"
        public String PBR;      // "-"
        public String DIVD;     // "0.00"
        public String KABU;     // "-"
	}
	
	public static class Data {
		@Ignore
		public String A_CALC027; // "-"
		@Ignore
        public String A_CALC028; // "-"
		@Ignore
        public String A_CALC029; // "-"
        public String A_HISTDAYL; // "2023/09/06,3865.0,3920.0,3865.0,3915.0,20900,\n"
        public String DHP;  // 高値 "4,120"
        public String DHPT; // 高値 時刻 "09:00"
		@Ignore
        public String DJ;   // 売買代金 "39,077,500"
        public String DLP;  // 低値 "4,100"
        public String DLPT; // 低値 時刻 "09:01"
        public String DOP;  // 始値 "4,120"
        public String DOPT; // 始値 時刻 "09:00"
        public String DPP;  // 現在値 "4,105"
        public String DPPT; // 現在値 時刻 "10:41"
        public String DV;   // 売買高 "9,500"
		@Ignore
        public String DYRP; // 前日比 パーセント "-0.72"
		@Ignore
        public String DYWP; // 前日比 "-30"
		@Ignore
        public String EXCC; // "T"
		@Ignore
        public String EXDV; // "0000"
        public String FLLN; // 名前 "極洋"
		@Ignore
        public String FLLNE; // "KYOKUYO CO., LTD."
		@Ignore
        public String FTRTS; // ""
        public String ISIN; // "JP3257200000"
		@Ignore
        public String JDMKCP; // "-"
		@Ignore
        public String JDSHRK; // "-"
		@Ignore
        public String JSEC; // sector33Code"0050"
        public String LISS; // "ﾌﾟﾗｲﾑ"
		@Ignore
        public String LISSE; // ""
        public String LOSH; // 売買単位 "100"
        public String MKCP; // 時価総額 "49,943,700,205.0"
		@Ignore
        public String NAME; // "極　洋"
		@Ignore
        public String NAMEE; // "KYOKUYO"
        public String PRP; // 前日終値 "4,135"
		@Ignore
        public String PRSS; // "C"
		@Ignore
        public String PSTS; // ""
        public String QAP;  // 売気配 "4,115"
        public String QAPT; // 売気配 時刻"10:43"
        public String QBP;  // 買気配 "4,105"
        public String QBPT; // 買気配 時刻 "10:43"
        public String SHRK; // 発行済株式数 "12,078,283"
		@Ignore
        public String TTCODE;  // "1301/T"
        public String TTCODE2; // "1301"
        public String YHPD; // 年初来高値 日付 "2024/09/24"
        public String YHPR; // 年初来高値 "4,600"
        public String YLPD; // 年初来安値 日付 "2024/08/05"
        public String YLPR; // 年初来安値 "3,400"
        public String ZXD;  // 売買日付 "2025/02/28"
		@Ignore
        public int    DYWP_FLG; // -1
		@Ignore
        public int    DYRP_FLG; // -1
		@Ignore
        public int    A_CALC029_FLG; // 0
		@Ignore
        public String EXDV_CNV; // ""
		@Ignore
        public String EXDVE_CNV; // ""
		@Ignore
        public Index[] INDEX_INFOMATION; // 0
        public String JSEC_CNV;  // sector33 日本顔 "水産・農林業"
		@Ignore
        public String JSECE_CNV; // sector33 英語 "Fishery, Agriculture & Forestry"
		@Ignore
        public String PSTSE; // ""
        public String LISS_CNV; // "プライム"
		@Ignore
        public String LISSE_CNV; // "Prime"
        
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
	public String   cputime;
	public Section1 section1;
	public int      status;
	public String   ver;
	@Ignore
	public Object   urlparam;
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}