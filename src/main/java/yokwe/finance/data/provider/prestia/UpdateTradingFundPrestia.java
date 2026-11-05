package yokwe.finance.data.provider.prestia;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.jita.StorageJITA;
import yokwe.finance.data.type.Currency;
import yokwe.finance.data.type.DailyValue;
import yokwe.util.Makefile;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;
import yokwe.util.update.UpdateBase;

public class UpdateTradingFundPrestia extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static Makefile MAKEFILE = Makefile.builder().
//		input(StorageJITA.FundInfo).
		output(StoragePrestia.TradingFundPrestia, StoragePrestia.FundDiv, StoragePrestia.FundPrice).
		build();

	public static void main(String[] args) {
		callUpdate();
	}

	@Override
	public void update() {
		gracePeriod = Duration.ofHours(20);

		downloadFile();
		updateFile();
	}

	void downloadFile() {
		{
			var file = StoragePrestia.FundListJSON.getFile();
			if (needsUpdate(file)) {
				var string = HttpUtil.getInstance().downloadString(URL_FUND_LIST).replace("\r\n", "\n");
				logger.info("fundList  {}", string.length());
				StoragePrestia.FundListJSON.save(string);
			}
		}

		var fundList = JSON.unmarshal(Raw.FundList.class, StoragePrestia.FundListJSON.load());

		for(var e: fundList.fundlist.data) {
			var prstCode = e.PRSTCode;
			var fundName = e.FundName;

			{
				var file = StoragePrestia.FundDetailJSON.getFile(prstCode);
				if (needsUpdate(file)) {
					var url    = String.format(URL_FUND_DETAIL, prstCode);
					var string = HttpUtil.getInstance().downloadString(url).replace("\r\n", "\n");
					logger.info("detail  {}  {}  {}", string.length(), prstCode, fundName);
					StoragePrestia.FundDetailJSON.save(prstCode, string);
				}
			}

			{
				var file = StoragePrestia.FundDataJSON.getFile(prstCode);
				if (needsUpdate(file)) {
					var time   = String.valueOf(System.currentTimeMillis());
					var url    = String.format(URL_FUND_DATA, prstCode, time);
					var string = HttpUtil.getInstance().downloadString(url).replace("\r\n", "\n");
					logger.info("data    {}  {}  {}", string.length(), prstCode, fundName);
					StoragePrestia.FundDataJSON.save(prstCode, string);
				}
			}
		}
	}
	private static final String URL_FUND_LIST   = "https://qw168.qhit.net/smbctbfund/cgi/wrap/qjsonp.aspx?F=ctl/fund_list";
	private static final String URL_FUND_DETAIL = "https://qw168.qhit.net/smbctbfund/cgi/wrap/qjsonp.aspx?F=ctl/fund_detail&KEY1=%s";
	private static final String URL_FUND_DATA   = "https://qw168.qhit.net/smbctbfund/cgi/navchart/funddata.aspx?mode=D&termtype=S&fundcode=%s&_=%s";

	void updateFile() {
		var fundList = JSON.unmarshal(Raw.FundList.class, StoragePrestia.FundListJSON.load());
		logger.info("fundlist.data  {}",fundList.fundlist.data.length);

		var tradingFundList = new ArrayList<TradingFundPrestia>();

		var unlistedMap = StorageJITA.FundInfoJITA.getList().stream().collect(Collectors.toMap(o -> o.fundCode, Function.identity()));

		for(var e: fundList.fundlist.data) {
			var prstCode = e.PRSTCode;
			var fundName = e.FundName;

			{
				var code = e.FundCode;
				if (unlistedMap.containsKey(code)) {
//					logger.info("XX  {}  {}", fundCode, fundName);
				} else {
					if (code.startsWith("G")) {
						//
					} else {
						logger.info("YY  {}  {}  {}", prstCode, code, fundName);
					}
				}
			}

			{
				var fundDetail = JSON.unmarshal(Raw.FundDetail.class, StoragePrestia.FundDetailJSON.load(prstCode));
				var data = fundDetail.fundlist.data[0];

				var tradingFund = new TradingFundPrestia();
				tradingFund.prstCode     = prstCode;
				tradingFund.fundCode     = data.FundCode;
				tradingFund.launchDate   = data.LaunchDate;
				tradingFund.name         = fundName;
				tradingFund.currency     = Currency.getInstance(e.CurrencyCode);
				tradingFund.investArea   = data.InvestArea;
				tradingFund.investTarget = data.InvestTarget;

				tradingFund.buyable      = e.Buyable;
				tradingFund.buyableOnly  = e.BuyableOnly;

				tradingFund.dividendFrequency = data.DividendFrequency;
			    tradingFund.dividendDate      = data.DividendDate;
			    tradingFund.dividendLastDate  = data.DividendLastDate;
			    tradingFund.dividendAmount    = data.DividendAmount;

				tradingFund.totalNetAsset = new BigDecimal(data.TotalNetAsset.replace(",", "")).movePointRight(6);

				{
					var string = data.PurchaseFee;
					if (string.equals("なし")) {
						tradingFund.purchaseFee = BigDecimal.ZERO;
					} else {
						var value = BigDecimal.ZERO;

						var m = Pattern.compile("[0-9]+\\.[0-9]+%").matcher(string);
						while(m.find()) {
							var newValue = new BigDecimal(m.group().replace("%", "")).movePointLeft(2);
//							logger.info("newValue  {}", newValue);
							if (0 < newValue.compareTo(value)) {
								value = newValue;
							}
						}
						tradingFund.purchaseFee = value.stripTrailingZeros();
//						logger.info("purchaseFee  {}", tradingFund.purchaseFee);
					}
				}

				{
					var string = data.TrustFee.replace("％", "%");
					if (string.equals("なし")) {
						tradingFund.trustFee = BigDecimal.ZERO;
					} else {
						var value = BigDecimal.ZERO;

						var m = Pattern.compile("[0-9]+\\.[0-9]+%").matcher(string);
						while(m.find()) {
							var newValue = new BigDecimal(m.group().replace("%", "")).movePointLeft(2);
//							logger.info("newValue  {}", newValue);
							if (0 < newValue.compareTo(value)) {
								value = newValue;
							}
						}

						if (value.compareTo(BigDecimal.ZERO) == 0) {
							logger.error("Unexpected string");
							logger.error("  string  {}!", string);
							throw new UnexpectedException("Unexpected string");
						} else {
							tradingFund.trustFee = value.stripTrailingZeros();
//							logger.info("trustFee  {}", tradingFund.trustFee);
						}
					}
				}
				{
					var string = data.TrustReserve;
					if (string.equals("なし")) {
						tradingFund.trustReserve = BigDecimal.ZERO;
					} else {
						var m = Pattern.compile("[0-9]+\\.[0-9]+%").matcher(string);
						if (m.find()) {
							var value = new BigDecimal(m.group().replace("%", "")).movePointLeft(2);
							tradingFund.trustReserve = value.stripTrailingZeros();
//							logger.info("trustReserve  {}", tradingFund.trustReserve);
						} else {
							logger.error("Unexpected string");
							logger.error("  string  {}!", string);
							throw new UnexpectedException("Unexpected string");
						}
					}
				}
				{
					var string = data.SuccessFee;
					if (string.equals("なし")) {
						tradingFund.successFee = "なし";
					} else {
						tradingFund.successFee = "あり";
					}
				}

				tradingFundList.add(tradingFund);
			}
			{
				var string   = StoragePrestia.FundDataJSON.load(prstCode);
				var fundData = JSON.unmarshal(Raw.FundData.class, string);

				var divList   = new ArrayList<DailyValue>();
				var priceList = new ArrayList<DailyValue>();

				for(var data: fundData.funddata) {
					//  0  "2026/10/15",  // date
					//  1  "4411",        // price
					//  2  "25640.9",
					//  3  "191.4",       // net asset value
					//  4  "55",          // dividend
					//  5  "1",
					//  6  "17412.00",
					//  7  "",
					//  8 "",
					//  9  "-14",
					//  10 "-0.32",
					//  11"0.93"

					var date = toLocalDate(data[0]);

					var price = new DailyValue(date, new BigDecimal(data[1]));
					priceList.add(price);

					if (!data[4].isEmpty()) {
						var div = new DailyValue(date, new BigDecimal(data[4]));
						divList.add(div);
					}
				}

				StoragePrestia.FundDiv.save(prstCode, divList);
				StoragePrestia.FundPrice.save(prstCode, priceList);
			}
		}

		StoragePrestia.FundDiv.touch();
		StoragePrestia.FundPrice.touch();

		save(tradingFundList, StoragePrestia.TradingFundPrestia); // use save for make
	}

	public static class Raw {
		public static class FundList {
			public static class CompanyList {
				public static class Data {
			        public String InvestCompany;
			        public int    line_no;

					@Override
					public String toString() {
						return ToString.withFieldName(this);
					}
				}

				public int    currentpage;
				public int    hitcount;
				public int    pagecount;
				public int    recordcount;
				public int    status;
				public String type;

				public Data[] data;


				@Override
				public String toString() {
					return ToString.withFieldName(this);
				}
			}

			public static class DomesticDate {
				public static class Data {
			        public String MonthlyUpdateDate;
			        public int    line_no;

					@Override
					public String toString() {
						return ToString.withFieldName(this);
					}
				}

				public int    currentpage;
				public int    hitcount;
				public int    pagecount;
				public int    recordcount;
				public int    status;
				public String type;

				public Data[] data;

				@Override
				public String toString() {
					return ToString.withFieldName(this);
				}
			}

			public static class ForeignDate {
				public static class Data {
			        public String MonthlyUpdateDate;
			        public int    line_no;

					@Override
					public String toString() {
						return ToString.withFieldName(this);
					}
				}

				public int    currentpage;
				public int    hitcount;
				public int    pagecount;
				public int    recordcount;
				public int    status;
				public String type;

				public Data[] data;

				@Override
				public String toString() {
					return ToString.withFieldName(this);
				}
			}

			public static class FundList_ {
				public static class Data {
			        public String SharperatioYear1;  // "SharperatioYear1": "-",
			        public String SharperatioYear3;  // "SharperatioYear3": "-",
			        public String AnnualReturnYear3; // "AnnualReturnYear3": "-",
			        public String AnnualReturnYear5; // "AnnualReturnYear5": "-",
			        public String RiskYear1;         // "RiskYear1": "-",
			        public String RiskYear3;         // "RiskYear3": "-",
			        public String ReturnYear1;       // "ReturnYear1": "-",
			        public String ReturnMonth6;      // "ReturnMonth6": "1.45",
			        public String PRSTCode;          // "PRSTCode": "31270051",
			        public String FundName;          // "FundName": "UBSユニバーサル・トラスト（ケイマン）Ⅲ&nbsp;－Manダイナミック・インカム・ファンド　円クラス",
			        public String PRSTRisk;          // "PRSTRisk": "3",
			        public String DividendRegularly; // "DividendRegularly": "2",
			        public String MinInvestAmount;   // "MinInvestAmount": "500,000",
			        public String DividendFrequency; // "DividendFrequency": "予定なし",
			        public String InvestArea;        // "InvestArea": "グローバル",
			        public String InvestTarget;      // "InvestTarget": "債券",
			        public String BuyUrl;            // "BuyUrl": "https://www.smbctb.co.jp/re/smbctbfund/ib.html?linkid=MFBDOTH&fund_c=31270051",
			        public String InvestCompany;     // "InvestCompany": "UBSマネジメント（ケイマン）リミテッド",
			        public int    line_no;           // "line_no": 1,
			        public String FundCode;          // "FundCode": "G2212",
			        public String FundType;          // "FundType": "9",
			        public String[] PRSTTheme;       // "PRSTTheme": [ "FGN" ],
			        public String Currency;          // "Currency": "日本円",
			        public String CurrencyUnit;      // "CurrencyUnit": "円",
			        public String CurrencyCode;      // "CurrencyCode": "JPY",
			        public String ReferenceDate;     // "ReferenceDate": "10/30",
			        public String NetAssetValue;     // "NetAssetValue": "1,056",
			        public String ChangeValue;       // "ChangeValue": "3",
			        public int    Buyable;           // "Buyable": 1,
			        public int    BuyableOnly;       // "BuyableOnly": 1

					@Override
					public String toString() {
						return ToString.withFieldName(this);
					}
				}

				public int    currentpage;
				public int    hitcount;
				public int    pagecount;
				public int    recordcount;
				public int    status;
				public String type;

				public Data[] data;

				@Override
				public String toString() {
					return ToString.withFieldName(this);
				}
			}

			public String       cputime;
			public int          status;
			public String       ver;

			public CompanyList  companylist;
			public DomesticDate domesticdate;
			public ForeignDate  foreigndate;
			public FundList_    fundlist;

			@Override
			public String toString() {
				return ToString.withFieldName(this);
			}
		}

		public static class FundDetail {
			public static class FundList_ {
				public static class Data {
			        public String SharperatioYear1;    // "SharperatioYear1": "-0.02",
			        public String SharperatioYear3;    // "SharperatioYear3": "0.19",
			        public String SharperatioYear5;    // "SharperatioYear5": "0.20",
			        public String AnnualReturnYear1;   // "AnnualReturnYear1": "0.06",
			        public String AnnualReturnYear3;   // "AnnualReturnYear3": "6.00",
			        public String AnnualReturnYear5;   // "AnnualReturnYear5": "5.82",
			        public String AnnualReturnSetting; // "AnnualReturnSetting": "5.15",
			        public String ReturnMonth1;        // "ReturnMonth1": "1.97",
			        public String MonthlyUpdateDate;   // "MonthlyUpdateDate": "2026/10/30",
			        public String RiskYear1;           // "RiskYear1": "2.27",
			        public String RiskYear3;           // "RiskYear3": "2.42",
			        public String RiskYear5;           // "RiskYear5": "2.34",
			        public String ReturnYear1;         // "ReturnYear1": "0.06",
			        public String ReturnMonth3;        // "ReturnMonth3": "-3.12",
			        public String ReturnYear3;         // "ReturnYear3": "19.10",
			        public String ReturnYear5;         // "ReturnYear5": "32.67",
			        public String ReturnMonth6;        // "ReturnMonth6": "-3.76",
			        public String ReturnSetting;       // "ReturnSetting": "93.65",
			        public String OtherFee;            // "OtherFee": "以下その他の費用・手数料について信託財産からご負担いただきます。<br/>①監査法人等に支払われるファンドの監査費用<br/>②有価証券の売買時に発生する売買委託手数料<br/>③資産を外国で保管する場合の費用　等<br/>※上記の費用等については、運用状況等により変動するため、事前に料率、上限額等を示すことができません。",
			        public String PRSTCode;            // "PRSTCode": "11020021",
			        public String SwitchingTarget;     // "SwitchingTarget": "不可",
			        public String SwitchingAttention;  // "SwitchingAttention": "―",
			        public String SwitchingCost;       // "SwitchingCost": "なし",
			        public String Feature;             // "Feature": "コーポレート・ボンド・インカムマザーファンドへの投資を通じて、主として米ドル建投資適格社債等に投資することにより、信託財産の成長と安定的な収益の確保を目指して運用を行います。<br/>１．高格付社債（米ドル建て、投資適格社債）へ投資します。<br/>２．投資対象とする債券の格付けは、Ａ格相当以上を中心とし、業種配分等にも配慮します。<br/>３．外貨建資産については、原則として対円での為替ヘッジを行いません。<br/><br/>※資金動向、市況動向等によっては、上記のような運用ができない場合があります。",
			        public String FundName;            // "FundName": "コーポレート・ボンド・インカム（為替ノーヘッジ型／１年決算型）　愛称：泰平航路１年決算型",
			        public String PRSTRisk;            // "PRSTRisk": "3",
			        public String ManageReportUrl;     // "ManageReportUrl": "https://www.smbctb.co.jp/investment/mutualfunds/products/annualreport/pdf/R_11020021.pdf",
			        public String TrustFee;            // "TrustFee": "1.089%（税込）",
			        public String TrustReserve;        // "TrustReserve": "0.15%",
			        public String DividendTreat;       // "DividendTreat": "再投資・受取選択可",
			        public String DividendFrequency;   // "DividendFrequency": "年1回",
			        public String InitiallyUnit;       // "InitiallyUnit": "50万円以上1円単位",
			        public String SuccessFee;          // "SuccessFee": "なし",
			        public String InvestArea;          // "InvestArea": "米国",
			        public String InvestTarget;        // "InvestTarget": "債券",
			        public String SwitchingUnit;       // "SwitchingUnit": "1口以上1口単位",
			        public String RedemptionFee;       // "RedemptionFee": "なし",
			        public String MonthlyReportUrl;    // "MonthlyReportUrl": "https://www.smbctb.co.jp/investment/mutualfunds/products/monthlyreport/pdf/F3622m.pdf",
			        public String LimitRedeemFee;      // "LimitRedeemFee": "なし",
			        public String DividendDate;        // "DividendDate": "毎年11月5日",
			        public String CurrencyHedge;       // "CurrencyHedge": "原則ヘッジなし",
			        public String ExchangeFee;         // "ExchangeFee": "なし",
			        public String SpecialRedeemFee;    // "SpecialRedeemFee": "なし",
			        public String PurchaseFee;         // "PurchaseFee": "1億円未満…1.10%（税込）<br/>1億円以上…0.55%（税込）",
			        public String ProspectusUrl;       // "ProspectusUrl": "https://www.smbctb.co.jp/investment/mutualfunds/products/eprosreport/pdf/P_11020021.pdf",
			        public String LaunchDate;          // "LaunchDate": "2013/08/28",
			        public String SalesBrochureUrl;    // "SalesBrochureUrl": "https://www.smbctb.co.jp/investment/mutualfunds/leaflet/pdf/11020021.pdf",
			        public String BuyUrl;              // "BuyUrl": "https://www.smbctb.co.jp/re/smbctbfund/ib.html?linkid=MFBDOTH&fund_c=11020021",
			        public String AdditionalUnit;      // "AdditionalUnit": "1万円以上1円単位",
			        public String InvestCompany;       // "InvestCompany": "三井住友DSアセットマネジメント",
			        public int    line_no;             // "line_no": 1,
			        public String FundCode;            // "FundCode": "79315138",
			        public String FundType;            // "FundType": "31",
			        public String DividendLastDate;    // "DividendLastDate": "2025/11/05",
			        public String[] PRSTTheme;          // "PRSTTheme": [],
			        public String Currency;            // "Currency": "日本円",
			        public String CurrencyUnit;        // "CurrencyUnit": "円",
			        public int    OfferClosed;         // "OfferClosed": 0,
			        public String ReferenceDate;       // "ReferenceDate": "2026/10/30",
			        public String NetAssetValue;       // "NetAssetValue": "19,365",
			        public String ChangeValue;         // "ChangeValue": "-27",
			        public String TotalNetAsset;       // "TotalNetAsset": "12,085",
			        public int    Buyable;             // "Buyable": 1,
			        public String[] SalesChannel;      // "SalesChannel": [ "INT", "OTC", "TEL"],
			        public String DividendAmount;      // "DividendAmount": "0"
				}

				public int    currentpage;
				public int    hitcount;
				public int    pagecount;
				public int    recordcount;
				public int    status;
				public String type;

				public Data[] data;

				@Override
				public String toString() {
					return ToString.withFieldName(this);
				}
			}

			public String       cputime;
			public int          status;
			public String       ver;

			public FundList_    fundlist;

			@Override
			public String toString() {
				return ToString.withFieldName(this);
			}
		}

		public static class FundData {
			@JSON.Name("PRSTファンド名")
			public String fundname; // "PRSTファンド名": "コーポレート・ボンド・インカム（為替ノーヘッジ型／１年決算型）　愛称：泰平航路１年決算型",
			public int    status;   //  "status": 0

			public String[][] funddata;

			@Override
			public String toString() {
				return ToString.withFieldName(this);
			}
		}
	}

	//
	// LocalDate
	//
	public static LocalDate toLocalDate(String string) {
		return LocalDate.parse(string, DATE_FORMAT);
	}
	private static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/M/d");

}
