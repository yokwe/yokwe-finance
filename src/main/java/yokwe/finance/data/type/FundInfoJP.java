package yokwe.finance.data.type;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import yokwe.util.ToString;

public class FundInfoJP implements Comparable<FundInfoJP> {
	public static final LocalDate REDEMPTION_DATE_NONE        = LocalDate.of(2999, 1, 1);
	public static final String    REDEMPTION_DATE_NONE_STRING = "99999999";

	public static final String FUND_TYPE_CEF = "単位型"; // closed end fund
	public static final String FUND_TYPE_OEF = "追加型"; // open end fund

	public static final String SETTLEMENT_DATE_NONE = "-";
	public static final String SETTLEMENT_DATE_EVERYDAY = "毎日決算";


	// "isinCd" : "JP90C000DJ15",
    public String            isinCode;
    // "associFundCd" : "AE313167",
    public String            fundCode;

    // for ETF  stock code or NO_STOCK_CODE
    public String            stockCode;

    // "establishedDate" : "2016-07-29 00:00:00",
    public LocalDate         inceptionDate;
	//  "redemptionDate" : "99999999",
    public LocalDate         redemptionDate;
	//  "setlFqcy" : "002",
    public int               divFreq;

    //    運用管理費用 (信託報酬)
	//    "trustReward" : 0.925,
    public BigDecimal        expenseRatio;
    // 購入時手数料（上限）
    // "buyFee" : 0,
    public BigDecimal        buyFeeMax;

    // 単位型 追加型
    public String            fundType;
    // 投資対象資産
    public String            investingAsset;
    // 投資対象地域
    public String            investingArea;
    // インデックスファンド区分
    public String            indexFundType;

    // 決算日
    public String            settlementDate;

    // LAST
    // "fundNm" : "あおぞら・グローバル・バランス・ファンド（部分為替ヘッジあり）",
    public String            name;


    // redemptionDate
    public boolean noRedemptionDate() {
    	return redemptionDate.equals(REDEMPTION_DATE_NONE);
    }

    // settlementDate
    public boolean noSettlementDate() {
    	return settlementDate.equals(SETTLEMENT_DATE_NONE);
    }
    public boolean everydaySettlementDate() {
    	return settlementDate.equals(SETTLEMENT_DATE_EVERYDAY);
    }
    public List<String> getSettlementDate() {
    	var ret = new ArrayList<String>();

    	if (noSettlementDate()) {
    		return ret;
    	}
    	if (everydaySettlementDate()) {
    		return ret;
    	}

    	var list = Arrays.asList(settlementDate.split(" "));
    	ret.addAll(list);

    	return ret;
    }


	public FundInfoJP(
			String isinCode, String fundCode, String stockCode, LocalDate inceptionDate, LocalDate redemptionDate, int divFreq,
			BigDecimal expenseRatio, BigDecimal buyFeeMax,
			String fundType, String investingArea, String investingAsset, String indexFundType, String settlementDate,
			String name
			) {
			this.isinCode       = isinCode;
			this.fundCode       = fundCode;
			this.stockCode      = stockCode;
			this.inceptionDate  = inceptionDate;
			this.redemptionDate = redemptionDate;
			this.divFreq        = divFreq;

			this.expenseRatio   = expenseRatio;
			this.buyFeeMax      = buyFeeMax;

			this.fundType       = fundType;
			this.investingArea  = investingArea;
			this.investingAsset = investingAsset;
			this.indexFundType  = indexFundType;
			this.settlementDate = settlementDate;

			this.name           = name;
		}

	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}

	public String getKey() {
		return this.isinCode;
	}
	@Override
	public int compareTo(FundInfoJP that) {
		return this.getKey().compareTo(that.getKey());
	}
	@Override
	public int hashCode() {
		return this.getKey().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof StockInfoJP) {
			FundInfoJP that = (FundInfoJP)o;
			return
				this.isinCode.equals(that.isinCode) &&
				this.stockCode.equals(that.stockCode) &&
				this.inceptionDate.equals(that.inceptionDate) &&
				this.redemptionDate.equals(that.redemptionDate) &&
				this.divFreq == that.divFreq &&

				this.expenseRatio.equals(that.expenseRatio) &&
				this.buyFeeMax.equals(that.buyFeeMax) &&

				this.fundType.equals(that.fundType) &&
				this.investingArea.equals(that.investingArea) &&
				this.investingAsset.equals(that.investingAsset) &&
				this.indexFundType.equals(that.indexFundType) &&
				this.settlementDate.equals(that.settlementDate);
				// ignore name
		} else {
			return false;
		}
	}

}
