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
	@Sheet.ColumnName("isinコード")      public String isinCode;
	@Sheet.ColumnName("ファンドコード")  public String fundCode;
	@Sheet.ColumnName("銘柄コード")      public String stockCode;
	
	@Sheet.ColumnName("設定日")  public LocalDate inception;
	@Sheet.ColumnName("償還日")  public LocalDate redemption;
	
	@Sheet.ColumnName("年月") public BigDecimal age; // yy.mm
	
	@Sheet.ColumnName("投資対象")    public String investingAsset;
	@Sheet.ColumnName("投資地域")    public String investingArea;
	@Sheet.ColumnName("ファンド型")  public String indexFundType;
	
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT3)
	@Sheet.ColumnName("管理費")      public BigDecimal expenseRatio;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT3)
	@Sheet.ColumnName("購入費最大")  public BigDecimal buyFeeMax;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_NUMBER_MILLION)
	@Sheet.ColumnName("資産総額")    public BigDecimal nav;
	@Sheet.ColumnName("配当回数")    public int        divc;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_NUMBER0)
	@Sheet.ColumnName("RSI14")       public BigDecimal rsi14;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_NUMBER0)
	@Sheet.ColumnName("RSI7")        public BigDecimal rsi7;
	
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("sd1年")  public BigDecimal sd1Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("sd3年")  public BigDecimal sd3Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("sd5年")  public BigDecimal sd5Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("sd10年") public BigDecimal sd10Y;

	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("収益1年")  public BigDecimal ror1Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("収益3年")  public BigDecimal ror3Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("収益5年")  public BigDecimal ror5Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("収益10年") public BigDecimal ror10Y;
	
	@Sheet.NumberFormat(SpreadSheet.FORMAT_NUMBER0)
	@Sheet.ColumnName("配当1年")  public BigDecimal div1Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_NUMBER0)
	@Sheet.ColumnName("配当3年")  public BigDecimal div3Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_NUMBER0)
	@Sheet.ColumnName("配当5年")  public BigDecimal div5Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_NUMBER0)
	@Sheet.ColumnName("配当10年") public BigDecimal div10Y;
	
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("利回り1年")  public BigDecimal yield1Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("利回り3年")  public BigDecimal yield3Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("利回り5年")  public BigDecimal yield5Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("利回り10年") public BigDecimal yield10Y;
	
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT0)
	@Sheet.ColumnName("配当品質1年")  public BigDecimal divScore1Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT0)
	@Sheet.ColumnName("配当品質3年")  public BigDecimal divScore3Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT0)
	@Sheet.ColumnName("配当品質5年")  public BigDecimal divScore5Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT0)
	@Sheet.ColumnName("配当品質10年") public BigDecimal divScore10Y;
	
	//
	@Sheet.ColumnName("名前")     public String name;
	@Sheet.ColumnName("|")        public String bar = "|";
	
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("日興")       public BigDecimal nikko;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("楽天")       public BigDecimal rakuten;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("ソニー")     public BigDecimal sony;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("PRESTIA")    public BigDecimal prestia;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("SMTB")       public BigDecimal smtb;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("クリック")   public BigDecimal click;
	
	@Sheet.ColumnName("NISA")       public BigDecimal nisa;
	
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
