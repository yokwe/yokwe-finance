package yokwe.finance.report.stock.jp;

import yokwe.util.ToString;
import yokwe.util.libreoffice.Sheet;

@Sheet.SheetName("stock-stats")
@Sheet.HeaderRow(0)
@Sheet.DataRow(1)
public final class ReportForm extends Sheet implements Comparable<ReportForm> {
	@Sheet.ColumnName("stockCode") public String stockCode = null;
	
	@Sheet.ColumnName("type")     public String type     = null;
	@Sheet.ColumnName("sector")   public String sector   = null;
	@Sheet.ColumnName("industry") public String industry = null;
	@Sheet.ColumnName("name")     public String name     = null;
	
	@Sheet.ColumnName("marketCap") public long   marketCap = -1;
	
	// current price and volume
	@Sheet.ColumnName("pricec") public int    pricec  = -1;
	@Sheet.ColumnName("price")  public double price   = -1;
	@Sheet.ColumnName("invest") public int    invest  = -1;
	// last price
	@Sheet.ColumnName("last")   public double last    = -1;
	
	// dividend
	@Sheet.ColumnName("divc")          public int    divc          = -1;
	@Sheet.ColumnName("lastDiv")       public double lastDiv       = -1;
	@Sheet.ColumnName("forwardYield")  public double forwardYield  = -1;
	@Sheet.ColumnName("annualDiv")     public double annualDiv     = -1;
	@Sheet.ColumnName("trailingYield") public double trailingYield = -1;
	
	// rate of return
	@Sheet.ColumnName("rorNoReinvested") public double rorNoReinvested = -1;
	
	// stats - sd hv rsi
	//  30 < pricec
	@Sheet.ColumnName("sd")  public double sd = -1;
	@Sheet.ColumnName("hv")  public double hv = -1;
	// 15 <= pricec
	@Sheet.ColumnName("rsi14") public double rsi14 = -1;
	@Sheet.ColumnName("rsi7")  public double rsi7  = -1;
	
	// min max
	@Sheet.ColumnName("min")   public double min   = -1;
	@Sheet.ColumnName("max")   public double max   = -1;
	@Sheet.ColumnName("minY3") public double minY3 = -1;
	@Sheet.ColumnName("maxY3") public double maxY3 = -1;
	
	// volume
	@Sheet.ColumnName("vol")   public double vol   = -1;
	// 5 <= pricec
	@Sheet.ColumnName("vol5")  public double vol5  = -1;
	// 20 <= pricec
	@Sheet.ColumnName("vol21") public double vol21 = -1;
	
	@Sheet.ColumnName("nisa") public String nisa = null;
	
	@Override
	public int compareTo(ReportForm that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}
