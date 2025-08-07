package yokwe.finance.data.provider.jita;

import yokwe.util.ToString;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

@Sheet.SheetName("対象商品一覧")
@Sheet.HeaderRow(1)
@Sheet.DataRow(2)
public class UnlistedFundForInvestor extends Sheet implements Comparable<UnlistedFundForInvestor> {
	@Sheet.ColumnName("リスト更新日")
	@Sheet.NumberFormat(SpreadSheet.FORMAT_INTEGER)
	public String updateDate; // date as YYYYMMDD
	@Sheet.ColumnName("追加・変更の別")
	public String changeType; // 追加 or 変更
	@Sheet.ColumnName("投信協会ファンドコード")
	public String fundCode; // 4 digits stockCode
	@Sheet.ColumnName("ファンド名称")
	public String fundName;
	@Sheet.ColumnName("運用会社名")
	public String assetManagementCompanyName;
	@Sheet.ColumnName("設定日")
	@Sheet.NumberFormat(SpreadSheet.FORMAT_INTEGER)
	public String inceptionDate; // date as YYYYMMDD
	@Sheet.ColumnName("償還日")
	public String redemptionDate = ""; // date as YYYYMMDD
	@Sheet.ColumnName("成長投資枠取扱可能日")
	@Sheet.NumberFormat(SpreadSheet.FORMAT_INTEGER)
	public String growthStartDate; // date as YYYYMMDD
	@Sheet.ColumnName("決算回数")
	public String numberOfSettlements; // 年１回 or 年2回 or 四半期 or 隔月
	@Sheet.ColumnName("つみたて投資枠の対象・非対象")
	public String tsumitate; // 対象 or 非対象

	@Override
	public int compareTo(UnlistedFundForInvestor that) {
		return this.fundCode.compareTo(that.fundCode);
	}

	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	public boolean isTsumitate() {
		return tsumitate.equals("対象");
	}
}