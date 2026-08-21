package yokwe.finance.report.fund.jp;

import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.util.ToString;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

@Sheet.SheetName("fund-stats")
@Sheet.HeaderRow(0)
@Sheet.DataRow(1)
public class ReportForm extends Sheet implements Comparable<ReportForm> {
	@Sheet.ColumnName("isinコード")      public String isinCode   = null;
	@Sheet.ColumnName("ファンドコード")  public String fundCode   = null;
	@Sheet.ColumnName("銘柄コード")      public String stockCode  = null;

	@Sheet.ColumnName("設定日")  public LocalDate  inception   = null;
	@Sheet.ColumnName("償還日")  public LocalDate  redemption  = null;
	@Sheet.ColumnName("年月")    public String     age         = null; // yy.mm

	@Sheet.ColumnName("投資対象")    public String investingAsset = "";
	@Sheet.ColumnName("投資地域")    public String investingArea  = "";
	@Sheet.ColumnName("二重課税")    public String taxAdjuettment = "";
	@Sheet.ColumnName("ファンド型")  public String indexFundType  = "";

	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT3)
	@Sheet.ColumnName("管理費")      public BigDecimal expenseRatio = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT3)
	@Sheet.ColumnName("購入費最大")  public BigDecimal buyFeeMax    = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_NUMBER_MILLION)
	@Sheet.ColumnName("資産総額")    public BigDecimal nav          = BigDecimal.ZERO;
	@Sheet.ColumnName("配当回数")    public int  divc               = 0;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_NUMBER0)
	@Sheet.ColumnName("RSI14")       public BigDecimal rsi14        = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_NUMBER0)
	@Sheet.ColumnName("RSI7")        public BigDecimal rsi7         = BigDecimal.ZERO;

	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2_BLANK)
	@Sheet.ColumnName("sd1年")  public BigDecimal sd1Y  = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2_BLANK)
	@Sheet.ColumnName("sd3年")  public BigDecimal sd3Y  = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2_BLANK)
	@Sheet.ColumnName("sd5年")  public BigDecimal sd5Y  = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2_BLANK)
	@Sheet.ColumnName("sd10年") public BigDecimal sd10Y = BigDecimal.ZERO;

	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2_BLANK)
	@Sheet.ColumnName("収益1年")  public BigDecimal ror1Y  = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2_BLANK)
	@Sheet.ColumnName("収益3年")  public BigDecimal ror3Y  = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2_BLANK)
	@Sheet.ColumnName("収益5年")  public BigDecimal ror5Y  = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2_BLANK)
	@Sheet.ColumnName("収益10年") public BigDecimal ror10Y = BigDecimal.ZERO;

	@Sheet.NumberFormat(SpreadSheet.FORMAT_NUMBER0)
	@Sheet.ColumnName("配当1年")  public BigDecimal div1Y = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_NUMBER0)
	@Sheet.ColumnName("配当3年")  public BigDecimal div3Y  = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_NUMBER0)
	@Sheet.ColumnName("配当5年")  public BigDecimal div5Y  = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_NUMBER0)
	@Sheet.ColumnName("配当10年") public BigDecimal div10Y = BigDecimal.ZERO;

	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2_BLANK)
	@Sheet.ColumnName("利回り1年")  public BigDecimal yield1Y  = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2_BLANK)
	@Sheet.ColumnName("利回り3年")  public BigDecimal yield3Y  = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2_BLANK)
	@Sheet.ColumnName("利回り5年")  public BigDecimal yield5Y  = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2_BLANK)
	@Sheet.ColumnName("利回り10年") public BigDecimal yield10Y = BigDecimal.ZERO;

	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT0_BLANK)
	@Sheet.ColumnName("配当品質1年")  public BigDecimal divScore1Y  = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT0_BLANK)
	@Sheet.ColumnName("配当品質3年")  public BigDecimal divScore3Y  = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT0_BLANK)
	@Sheet.ColumnName("配当品質5年")  public BigDecimal divScore5Y  = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT0_BLANK)
	@Sheet.ColumnName("配当品質10年") public BigDecimal divScore10Y = BigDecimal.ZERO;

	//
	@Sheet.ColumnName("名前")     public String name = "";
	@Sheet.ColumnName("|")        public String bar  = "|";

	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2_BLANK)
	@Sheet.ColumnName("日興")       public BigDecimal nikko = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2_BLANK)
	@Sheet.ColumnName("楽天")       public BigDecimal rakuten = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2_BLANK)
	@Sheet.ColumnName("ソニー")     public BigDecimal sony = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2_BLANK)
	@Sheet.ColumnName("PRESTIA")    public BigDecimal prestia = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2_BLANK)
	@Sheet.ColumnName("SMTB")       public BigDecimal smtb = BigDecimal.ZERO;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2_BLANK)
	@Sheet.ColumnName("クリック")   public BigDecimal click = BigDecimal.ZERO;

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
