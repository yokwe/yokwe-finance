package yokwe.finance.data.provider.jpx;

import yokwe.util.ToString;
import yokwe.util.json.JSON.Optional;

public class Kessan {
	public static class Section0 {
		@Optional
		public String FLLN;     //"極洋",
		@Optional
		public String FLLNE;    // "KYOKUYO CO., LTD.",
		@Optional
		public String K_KUBUN;  // "1",
		@Optional
		public String TTCODE2;  // "1301",
		public int    hitcount; // 1
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	public static class Section1 {
		@Optional
		public Data[] data;
		public int    hitcount; // 1
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	public static class Data implements Comparable<Data> {
        public String ALK_EDDATE_Y;    // 2023/03      年度末月
        public String ALK_EDDATEM;     // 2023/03      当期期末月
        //
        public String ALK_CASHVAL;     // 1,196,230    現金等期末残高（百万円）
        public String ALK_CPTL;        // 431,119      純資産（百万円）
        public String ALK_CPTLPSTK;    // 3,751.95     1株当たり純資産（円）
        public String ALK_DIVD;        // 154.00       1株当たり配当金（円）
        public String ALK_DIVDH;       // 40.00        四半期末配当金（円）
        public String ALK_EPS;         // 74.67        1株当たり当期純利益（円）
        public String ALK_FINCCF;      // -18,068      財務キャッシュフロー（百万円）
        public String ALK_FREECF;      // 152,557      営業+投資キャッシュフロー（百万円）
        public String ALK_INVCF;       // 213,939      投資キャッシュフロー（百万円）
        public String ALK_KIKAN2;      // 9            ？
        public String ALK_KIKAN2E_CNV; // full-year    ？
        public String ALK_KIKAN2_CNV;  // 通期         決算期
        public String ALK_NETP;        // 8,719        当期純利益（百万円）
        public String ALK_OPESALE;     // -            経常収益（百万円）
        public String ALK_OPRT;        // -            ？
        public String ALK_ORDP;        // 7,356        経常利益（百万円）
        public String ALK_ORDSALE;     // 4.0          ？
        public String ALK_ROE;         // 1.90         自己資本利益率
        public String ALK_SALE;        // 183,292      経常収益（百万円）
        public String ALK_SALECF;      // -61,382      営業キャッシュフロー（百万円）
        public String ALK_TOTLASET;    // 7,184,070    純資産
        public String ALS_SECC;        // 1            ？
        public String ALS_SECCE_CNV;   // Consolidated ？
        public String ALS_SECC_CNV;    // 連結         連単種別
        
		@Override
		public int compareTo(Data that) {
			int ret = this.ALK_EDDATE_Y.compareTo(that.ALK_EDDATE_Y);
			if (ret == 0) ret = this.ALK_EDDATEM.compareTo(that.ALK_EDDATEM);
			return ret;
		}
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
	public Section0 section0;
	public Section1 section1;
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	public boolean isEmpty() {
		return section0.hitcount == 0 && section1.hitcount == 0;
	}
}