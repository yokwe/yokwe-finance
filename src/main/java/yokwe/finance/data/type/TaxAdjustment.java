package yokwe.finance.data.type;

import yokwe.util.ToString;

public class TaxAdjustment implements Comparable<TaxAdjustment> {
	// 投資信託等の二重課税調整制度の対象となる可能性の高いETF・REIT
	// https://www.jpx.co.jp/equities/related/tax/tvdivq00000170tw-att/20260903_ETF_REIT.pdf

	//1,野村アセットマネジメント,13090,NEXT FUNDS ChinaAMC・中国株式・上証50連動型上場投信,1,7/8

	public String seq;
	public String management;
	public String stockCode;
	public String name;
	public String divCount;
	public String divDateList;

	@Override
	public int compareTo(TaxAdjustment that) {
		return this.stockCode.compareTo(that.stockCode);
	}

	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}
