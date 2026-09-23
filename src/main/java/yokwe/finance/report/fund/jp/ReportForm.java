package yokwe.finance.report.fund.jp;

import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.util.ToString;
import yokwe.util.libreoffice.Sheet;

@Sheet.SheetName("fund-stats")
@Sheet.HeaderRow(0)
@Sheet.DataRow(1)
public class ReportForm extends Sheet implements Comparable<ReportForm> {
	@Sheet.ColumnName("isinコード")      public String isinCode   = "";
	@Sheet.ColumnName("ファンドコード")  public String fundCode   = "";
	@Sheet.ColumnName("銘柄コード")      public String stockCode  = "";

	@Sheet.ColumnName("設定日")  public LocalDate  inception   = null;
	@Sheet.ColumnName("償還日")  public LocalDate  redemption  = null;
	@Sheet.ColumnName("年月")    public BigDecimal age         = null; // yy.mm

	@Sheet.ColumnName("投資対象")    public String investingAsset = "";
	@Sheet.ColumnName("投資地域")    public String investingArea  = "";
	@Sheet.ColumnName("二重課税")    public String taxAdjuettment = "";
	@Sheet.ColumnName("ファンド型")  public String indexFundType  = "";

	@Sheet.ColumnName("管理費")      public BigDecimal expenseRatio = null;
	@Sheet.ColumnName("購入費最大")  public BigDecimal buyFeeMax    = null;
	@Sheet.ColumnName("資産総額")    public BigDecimal nav          = null;
	@Sheet.ColumnName("配当回数")    public BigDecimal divc         = null;
	@Sheet.ColumnName("RSI14")       public BigDecimal rsi14        = null;
	@Sheet.ColumnName("RSI7")        public BigDecimal rsi7         = null;

	@Sheet.ColumnName("sd1年")  public BigDecimal sd1Y  = null;
	@Sheet.ColumnName("sd3年")  public BigDecimal sd3Y  = null;
	@Sheet.ColumnName("sd5年")  public BigDecimal sd5Y  = null;
	@Sheet.ColumnName("sd10年") public BigDecimal sd10Y = null;

	@Sheet.ColumnName("収益1年")  public BigDecimal ror1Y  = null;
	@Sheet.ColumnName("収益3年")  public BigDecimal ror3Y  = null;
	@Sheet.ColumnName("収益5年")  public BigDecimal ror5Y  = null;
	@Sheet.ColumnName("収益10年") public BigDecimal ror10Y = null;

	@Sheet.ColumnName("配当1年")  public BigDecimal div1Y = null;
	@Sheet.ColumnName("配当3年")  public BigDecimal div3Y  = null;
	@Sheet.ColumnName("配当5年")  public BigDecimal div5Y  = null;
	@Sheet.ColumnName("配当10年") public BigDecimal div10Y = null;

	@Sheet.ColumnName("利回り1年")  public BigDecimal yield1Y  = null;
	@Sheet.ColumnName("利回り3年")  public BigDecimal yield3Y  = null;
	@Sheet.ColumnName("利回り5年")  public BigDecimal yield5Y  = null;
	@Sheet.ColumnName("利回り10年") public BigDecimal yield10Y = null;

	@Sheet.ColumnName("配当品質1年")  public BigDecimal divScore1Y  = null;
	@Sheet.ColumnName("配当品質3年")  public BigDecimal divScore3Y  = null;
	@Sheet.ColumnName("配当品質5年")  public BigDecimal divScore5Y  = null;
	@Sheet.ColumnName("配当品質10年") public BigDecimal divScore10Y = null;

	//
	@Sheet.ColumnName("名前")     public String name = "";
	@Sheet.ColumnName("|")        public String bar  = "|";

	@Sheet.ColumnName("日興")       public BigDecimal nikko   = null;
	@Sheet.ColumnName("楽天")       public BigDecimal rakuten = null;
	@Sheet.ColumnName("ソニー")     public BigDecimal sony    = null;
	@Sheet.ColumnName("PRESTIA")    public BigDecimal prestia = null;
	@Sheet.ColumnName("SMTB")       public BigDecimal smtb    = null;
	@Sheet.ColumnName("クリック")   public BigDecimal click   = null;

	@Sheet.ColumnName("NISA")       public String nisa = "";

    @Override
    public String toString() {
        return ToString.withFieldName(this);
    }
    @Override
    public int compareTo(ReportForm that) {
    	return this.isinCode.compareTo(that.isinCode);
    }
    @Override
    public int hashCode() {
    	return this.isinCode.hashCode();
    }
}
