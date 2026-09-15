package yokwe.finance.data.provider.nikko;

import java.math.BigDecimal;

import yokwe.util.ToString;

public class FundList {
	public static class Section1 {
		public int    currentpage;
		public Data[] data;
		public int    hitcount;
		public int    pagecount;
		public int    recordcount;
		public int    status;
		public String type;

		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}

	public static class Data {
        public String     DividendYears;               // "1,510.00"
        public String     ReturnYear1;                 // "+56.78"
        public String     ReturnMonth3;                // "-3.01"
        public String     NetAssetValue;               // "24,820"
        public String     TotalAssetValue;             // "862.42"
        public String     ChangeValue;                 // "+140"
        public BigDecimal CompanySort;                 // 3
        public String     NikkoCode;                   // "8328"
        public String     QuickFundRisk;               // "5"
        public String     MorningstarRating;           // "3"
        public boolean    IsIndexType;                 // false
        public BigDecimal line_no;                     // 1
        public String     FundCode;                    // "01311002"
        public String     FundType;                    // "31"
        public String     FundName;                    // "野村国内株式アクティブオープン"
        public String     DividendTimes;               // "2"
        public String     DividendProfit;              // "800.00"
        public String     TsumitateFundScore;          // "3"
        public String     Currency;                    // "--"
        public String     CompanyName;                 // "野村アセット"
        public String     CompanyCode;                 // "01"
        public boolean    HasFavoriteButton;           // true
        public boolean    HasFundCompareButton;        // true
        public String     NetAssetValueUnit;           // "円"
        public String     ChangeValueUnit;             // "円"
        public String     IsGeneralCourse;             // "ALL"
        public String     IsDirectCourse;              // "DRT"
        public boolean    IsDirectCourseOnly;          // false
        public boolean    IsTsumitate;                 // true
        public boolean    IsNISA;                      // true
        public boolean    IsTsumiNISA;                 // false
        public boolean    IsiDeCo;                     // false
        public boolean    IsNoload;                    // false
        public boolean    HasPurchaseOrderButton;      // true
        public boolean    HasReserveApplicationButton; // true
        public boolean    HasReserveSimulationButton;  // true
        public boolean    HasMokuromiLink;             // true
        public String     MokuromiUrl;                 // "https://www.smbcnikko.co.jp/doc-pdf/8328_001.pdf"
        public boolean    HasInvestmentReportLink;     // false
        public String     InvestmentReportUrl;         // ""
        public String[]   Categories;
        public String[]   Area;
//        "Categories": [
//          "101"
//        ],
//        "Area": [
//          "日本"
//        ],
        public boolean    IsESG;               // false
        public boolean    IsBullBear;          // false
        public BigDecimal ReturnMonth1Raw;     // 22332.0
        public BigDecimal ReturnMonth3Raw;     // -30105.0,
        public BigDecimal ReturnMonth6Raw;     // 65635.0
        public BigDecimal ReturnYear1Raw;      // 567842.0
        public BigDecimal ReturnYear3Raw;      // 1178181.0
        public BigDecimal ReturnYear5Raw;      // 1413067.0
        public BigDecimal ReturnYear10Raw;     // 3021658.0
        public BigDecimal ReturnSettingRaw;    // 2147351.0
        public BigDecimal SharpeRatioYear1Raw; // 1.79
        public BigDecimal SharpeRatioYear3Raw; // 1.43
        public BigDecimal SharpeRatioYear5Raw; // 1.09
        public BigDecimal NetAssetValueRaw;    // 2482000.0
        public BigDecimal ChangeValueRaw;      // 140.0
        public BigDecimal TotalAssetValueRaw;  // 86242.0
        public BigDecimal DividendProfitRaw;   // 800.00
        public BigDecimal DividendYearsRaw;    // 1510.00
        public BigDecimal NikkoSort;           // null, or 8.0
        public String     FundNameRaw;         // "野村国内株式ｱｸﾃｨﾌﾞｵｰﾌﾟﾝ_野村国内株式アクティブオープン_野村国内株式アクティブオープン"
        public String     NickNameRaw;         // ""
        public String     ReferenceDate;       // "09/14"

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
