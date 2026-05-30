package yokwe.finance.data.provider.jita;

import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.json.JSON.Ignore;

public final class FundDataSearch {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	// unitOpenDiv
	public enum FundType {
		UNIT("1", "単位型"), OPEN("2", "追加型");

		public static FundType getInstance(String string) {
			for(var e: values()) {
				if (e.code.equals(string)) {
					return e;
				}
			}
			logger.error("Unexpected string");
			logger.error("  string  {}", string);
			throw new UnexpectedException("Unexpected string");
		}

		public final String code;
		public final String name;

		private FundType(String code, String name) {
			this.code = code;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return code;
		}
	}

	// investAssetKindCd
	public enum InvestingAsset {
		STOCK("1", "株式"),
		BOND("2", "債券"),
		REIT("3", "不動産投信"),
		OTHER("4", "その他"),
		COMPOSITE("5", "複合");

		public static InvestingAsset getInstance(String string) {
			for(var e: values()) {
				if (e.code.equals(string)) {
					return e;
				}
			}
			logger.error("Unexpected string");
			logger.error("  string  {}", string);
			throw new UnexpectedException("Unexpected string");
		}

		public final String code;
		public final String name;

		private InvestingAsset(String code, String name) {
			this.code = code;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return code;
		}
	}

	// investArea10kindCd
	public enum InvestingArea {
		GLOBAL("1", "グローバル"),
		JAPAN("2", "日本"),
		NORTH_AMERICA("3", "北米"),
		EUROPE("4", "欧州"),
		ASIA("5", "アジア"),
		OCEANIA("6", "オセアニア"),
		LATIN_AMERICA("7", "中南米"),
		AFRICA("8", "アフリカ"),
		MIDDLE_EAST("9", "中近東"),
		EMERGING("10", "エマージング"),
		;

		public static InvestingArea getInstance(String string) {
			for(var e: values()) {
				if (e.code.equals(string)) {
					return e;
				}
			}
			logger.error("Unexpected string");
			logger.error("  string  {}", string);
			throw new UnexpectedException("Unexpected string");
		}

		public final String code;
		public final String name;

		private InvestingArea(String code, String name) {
			this.code = code;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return code;
		}
	}

	// supplementKindCd
	public enum IndexFundType {
		NOT_INDEX("0", "該当なし"),
		INDEX("1", "インデックス型"),
		SPECIAL("2", "特殊型"),
		;

		public static IndexFundType getInstance(String string) {
			for(var e: values()) {
				if (e.code.equals(string)) {
					return e;
				}
			}
			logger.error("Unexpected string");
			logger.error("  string  {}", string);
			throw new UnexpectedException("Unexpected string");
		}

		public String code;
		public String name;

		private IndexFundType(String code, String name) {
			this.code = code;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return code;
		}
	}


    public static final class ResultInfo {
        // 投資対象地域
        // See InvestingArea
        public String investArea10kindCd4;     // "investArea10kindCd4": "0",
        public String investArea10kindCd5;     // "investArea10kindCd5": "0",
        public String investArea10kindCd2;     // "investArea10kindCd2": "0",
        public String investArea10kindCd3;     // "investArea10kindCd3": "0",
        public String investArea10kindCd1;     // "investArea10kindCd1": "1",

        public String lastUpdater;             // "lastUpdater": "FDSA000015",
        public String stdCostPointFrmDayBfrRt; // "stdCostPointFrmDayBfrRt": "0.19",
        public String rankStandardPriceRa3y;   // "rankStandardPriceRa3y": "185/691",
        public String rankSharpRa3y;           // "rankSharpRa3y": "103/691",
        // 基準価額 評価基準日
        public String standardDate;            // "standardDate": "2026-05-29 00:00:00",
        public String rankRiskRa3y;            // "rankRiskRa3y": "378/691",
        public String separateseparateDiv;     // "separateseparateDiv": "0",
        public String sharpRa5y;               // "sharpRa5y": null,
        // 決算日
        public String setlDate;                // "setlDate": "04/20",
        public String standardPriceRa1y;       // "standardPriceRa1y": "42.67",
        public String myInstFundCd;            // "myInstFundCd": "0000110141",
        public String redemptionDate;          // "redemptionDate": "99999999",
        public String isinCd;                  // "isinCd": "JP90C000NB94",
        public String rankStandardPriceRa20y;  // "rankStandardPriceRa20y": "-/-",
        public String riskRa6m;                // "riskRa6m": "0.2178",

        @Ignore
        public String settlementInfo;

        public String fundStNm;                  // "fundStNm": "あおぞら・徹底分散グローバル・サステナビリティ株式Ｆ《満天観測》",
        public String instCd;                    // "instCd": "100AE",
        public String investArea10kindCd8;       // "investArea10kindCd8": "0",
        public String investArea10kindCd9;       // "investArea10kindCd9": "0",
        public String investArea10kindCd6;       // "investArea10kindCd6": "0",
        public String riskRa5y;                  // "riskRa5y": null,
        public String sharpRa6m;                 // "sharpRa6m": "0.98",
        public String investArea10kindCd7;       // "investArea10kindCd7": "0",
        public String kanaName;                  // "kanaName": null,
        public String establishedDate;           // "establishedDate": "2022-05-31 00:00:00",
        public String associFundCd;              // "associFundCd": "AE311225",
        public String rankSharpRa1y;             // "rankSharpRa1y": "211/691",
        public String rankStandardPriceRa1y;     // "rankStandardPriceRa1y": "254/691",
        public String investArea10kindCd10;      // "investArea10kindCd10": "0",
        public String reportDiv;                 // "reportDiv": "1",
        public String rankRiskRa1y;              // "rankRiskRa1y": "394/691",
        public String sharpRa3y;                 // "sharpRa3y": "1.57",
        public String openDiv;                   // "openDiv": "1",
        // 運用管理費用（信託報酬） 信託銀行
        public String custodyTrustReward;        // "custodyTrustReward": "0.025",
        public String riskRa20y;                 // "riskRa20y": null,
        public String standardPriceRa3y;         // "standardPriceRa3y": "89.76",
        public String rankStandardPriceRa10y;    // "rankStandardPriceRa10y": "-/-",
        public String retentionMoneyCd;          // "retentionMoneyCd": "1",
        public String standardPriceRa5y;         // "standardPriceRa5y": null,
        public String dividendLast;              // "dividendLast": "0.0",
        public String cancelLationFeeCd;         // "cancelLationFeeCd": "1",
        public String reportUrl;                 // "reportUrl": "https://www.aozora-im.co.jp/cms-data/pdf/fund/detail/sikk6skaa0gpyc5s9ukuku90083nwl.pdf",
        // 運用管理費用 (信託報酬)
        public String trustReward;               // "trustReward": "0.525",
        public String renzokuCancelCreateValFlg; // "renzokuCancelCreateValFlg": "1",
        public String stdCostPointFrmDayBfr;     // "stdCostPointFrmDayBfr": "38.0",
        public String totalNetAssets;            // "totalNetAssets": "11507.0",
        public String rankSharpRa20y;            // "rankSharpRa20y": "-/-",
        public String investArea3kindCd;         // "investArea3kindCd": "3",
        public String riskRa3y;                  // "riskRa3y": "0.1421",
        public String dividendFrom;              // "dividendFrom": null,
        public String returnRaY;                 // "returnRaY": null,
        public String dcFundFlg;                 // "dcFundFlg": "9",

        @Ignore
        public String dividendInfo;

        public String evalDiscrepancyTextDisplayFlag; // "evalDiscrepancyTextDisplayFlag": "0",
        public String rankRiskRa20y;                  // "rankRiskRa20y": "-/-",
        public String standardPriceRa6m;              // "standardPriceRa6m": "10.49",
        // インデックスファンド区分
        // See IndexFundType
        public String supplementKindCd;               // "supplementKindCd": "0",
        public String fundNkNm;                       // "fundNkNm": "満天観測",
        public String repordNo;                       // "repordNo": "51895222",
        public String sharpRa10y;                     // "sharpRa10y": null,

        @Ignore
        public String institutionInfo;

        public String riskRa10y;                    // "riskRa10y": null,
        public String fundCategory;                 // "fundCategory": "7",
        public String monthlyCancelCreateVal12;     // "monthlyCancelCreateVal12": "-256.0",
        // 運用管理費用（信託報酬）運用会社
        public String entrustTrustReward;           // "entrustTrustReward": "0.25",
        public String nisaGrowthFlg;                // "nisaGrowthFlg": "1",
        public String dividendTo;                   // "dividendTo": null,
        public String salesFee;                     // "salesFee": null,
        public String sharpRa1y;                    // "sharpRa1y": "2.29",
        // 投資対象資産
        // See InvestingAsset
        public String investAssetKindCd;            // "investAssetKindCd": "1",
        public String rankSharpRa10y;               // "rankSharpRa10y": "-/-",
        public String dividendLastSelDate;          // "dividendLastSelDate": "2026-04-20 00:00:00",
        public String riskRa1y;                     // "riskRa1y": "0.1599",
        public String rankStandardPriceRa6m;        // "rankStandardPriceRa6m": "291/691",
        // 運用年数
        public String establishedDateToNow;         // "establishedDateToNow": "0048",
        public String salesInstDiv;                 // "salesInstDiv": null,
        // 購入時手数料（上限）
        public String buyFee;                       // "buyFee": "3.0",
        public String nisaFlg;                      // "nisaFlg": "2",
        // 償還までの期間
        public String nowToRedemptionDate;          // "nowToRedemptionDate": "9999",
        public String rankRiskRa10y;                // "rankRiskRa10y": "-/-",
        public String rankRiskRa5y;                 // "rankRiskRa5y": "-/-",
        public String evalDiscrepancyStandardMonth; // "evalDiscrepancyStandardMonth": "202604",
        public String rankSharpRa5y;                // "rankSharpRa5y": "-/-",
        public String rankStandardPriceRa5y;        // "rankStandardPriceRa5y": "-/-",
        public String sharpRa20y;                   // "sharpRa20y": null,
        public String entrustCmpNm;                 // "entrustCmpNm": "あおぞら投信",
        public String lastUpdYmd;                   // "lastUpdYmd": "2026-05-30 06:50:28",
        public String fundNm;                       // "fundNm": "あおぞら・徹底分散グローバル・サステナビリティ株式ファンド",
        // 単位型 追加型
        public String unitOpenDiv;                  // "unitOpenDiv": "2",
        public String standardPriceRa10y;           // "standardPriceRa10y": null,
        public String standardPriceRa20y;           // "standardPriceRa20y": null,
        public String rankRiskRa6m;                 // "rankRiskRa6m": "351/691",
        public String monthlyCancelCreateVal9;      // "monthlyCancelCreateVal9": "-222.0",
        public String monthlyCancelCreateVal6;      // "monthlyCancelCreateVal6": "-113.0",
        public String dividend1m;                   // "dividend1m": null,
        public String setlFqcy;                     // "setlFqcy": "001",
        // 運用管理費用（信託報酬）販売会社
        public String bondTrustReward;              // "bondTrustReward": "0.25",
        public String monthlyCancelCreateVal3;      // "monthlyCancelCreateVal3": "202.0",
        // 基準価額
        public String standardPrice;                // "standardPrice": "20432.0",
        public String dividend1y;                   // "dividend1y": "0.0",
        public String monthlyCancelCreateVal;       // "monthlyCancelCreateVal": "-19.0",
        public String rankSharpRa6m;                // "rankSharpRa6m": "275/691",
        public String evalDiscrepancyFlag;          // "evalDiscrepancyFlag": "0",
        @Ignore
        public String instName;                     // "instName": null,
    }


    public static final class SearchResultInfo {
        public String draw;            // public String draw; // "draw": "2",
        public String recordsTotal;    // "recordsTotal": "5841",
        public String recordsFiltered; // "recordsFiltered": "5841",
        public String pageSize;        // "pageSize": "20",
        public String startNo;         // "startNo": "0",
        public String allPageNo;       // "allPageNo": "293",
        public String showRecordText;  // "showRecordText": "1～20",
        public String standardDate;    // "standardDate": "2026-05-29 00:00:00",
        public String sortKey1;        // "sortKey1": "fundStNm",
        public String sortOrder1;      // "sortOrder1": "Asc",
        @Ignore
        public String sortKey2;        // "sortKey2": null,
        @Ignore
        public String sortOrder2;      // "sortOrder2": null,

        public ResultInfo[] resultInfoMapList;
    }


    @Ignore
    public String           statusCode;
    public SearchResultInfo searchResultInfo;

    @Override
    public String toString() {
		return ToString.withFieldName(this);
    }
}
