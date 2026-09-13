package yokwe.finance.report.stock.us;

import java.math.BigDecimal;

import yokwe.util.ToString;
import yokwe.util.libreoffice.Sheet;

@Sheet.SheetName("stock-stats")
@Sheet.HeaderRow(0)
@Sheet.DataRow(1)
public final class ReportForm extends Sheet implements Comparable<ReportForm> {
	@Sheet.ColumnName("stockCode") public String stockCode = "";

	@Sheet.ColumnName("type")     public String type     = "";
	@Sheet.ColumnName("sector")   public String sector   = "";
	@Sheet.ColumnName("industry") public String industry = "";
	@Sheet.ColumnName("name")     public String name     = "";

	// current price and volume
	@Sheet.ColumnName("pricec") public BigDecimal pricec = null;
	@Sheet.ColumnName("price")  public BigDecimal price  = null;
	// last price
	@Sheet.ColumnName("last")   public BigDecimal last   = null;

	// dividend
	@Sheet.ColumnName("divc")          public BigDecimal divc          = null;
	@Sheet.ColumnName("lastDiv")       public BigDecimal lastDiv       = null;
	@Sheet.ColumnName("forwardYield")  public BigDecimal forwardYield  = null;
	@Sheet.ColumnName("annualDiv")     public BigDecimal annualDiv     = null;
	@Sheet.ColumnName("trailingYield") public BigDecimal trailingYield = null;

	// rate of return
	@Sheet.ColumnName("rorPrice")        public BigDecimal rorPrice        = null;
	@Sheet.ColumnName("rorReinvested")   public BigDecimal rorReinvested   = null;
	@Sheet.ColumnName("rorNoReinvested") public BigDecimal rorNoReinvested = null;

	// stats - sd hv rsi
	//  30 < pricec
	@Sheet.ColumnName("sd")    public BigDecimal sd = null;
	@Sheet.ColumnName("hv")    public BigDecimal hv = null;
	// 15 <= pricec
	@Sheet.ColumnName("rsi")   public BigDecimal rsi = null;

	// min max
	@Sheet.ColumnName("min")   public BigDecimal min   = null;
	@Sheet.ColumnName("max")   public BigDecimal max   = null;
	@Sheet.ColumnName("minY3") public BigDecimal minY3 = null;
	@Sheet.ColumnName("maxY3") public BigDecimal maxY3 = null;

	// volume
	@Sheet.ColumnName("vol")   public BigDecimal vol   = null;
	// 5 <= pricec
	@Sheet.ColumnName("vol5")  public BigDecimal vol5  = null;
	// 20 <= pricec
	@Sheet.ColumnName("vol21") public BigDecimal vol21 = null;

	@Sheet.ColumnName("nisa")  public String     nisa     = "";

	@Sheet.ColumnName("楽天")  public String     rakuten  = "";
	@Sheet.ColumnName("日興")  public String     nikko    = "";

	@Override
	public int compareTo(ReportForm that) {
		return this.stockCode.compareTo(that.stockCode);
	}

	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}
