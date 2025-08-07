package yokwe.finance.data.provider.nasdaq;

import yokwe.util.CSVUtil;
import yokwe.util.ToString;

public class OtherListed implements Comparable<OtherListed> {
	// ACT Symbol|Security Name|Exchange|CQS Symbol|ETF|Round Lot Size|Test Issue|NASDAQ Symbol
	// A|Agilent Technologies, Inc. Common Stock|N|A|N|100|N|A
	// ZYME|Zymeworks Inc. Common Shares|N|ZYME|N|100|N|ZYME
	// File Creation Time: 1122202221:31||||||

	// See below URL for detail
	//   http://www.nasdaqtrader.com/trader.aspx?id=symboldirdefs
	
	// ACT Symbol
	//   Identifier for each security used in ACT and CTCI connectivity protocol.
	//   Typical identifiers have 1-5 character root symbol and then 1-3 characters for suffixes. Allow up to 14 characters.
	
	// Exchange	
	//   The listing stock exchange or market of a security.
	//   Allowed values are:
	//   A = NYSE MKT
	//   N = New York Stock Exchange (NYSE)
	//   P = NYSE ARCA
	//   Z = BATS Global Markets (BATS)
	//   V = Investors' Exchange, LLC (IEXG)

	public static final String EXCHANGE_NYSE_MKT  = "A";
	public static final String EXCHANGE_NYSE      = "N";
	public static final String EXCHANGE_NYSE_ARCA = "P";
	public static final String EXCHANGE_BATS      = "Z";
	public static final String EXCHANGE_IEXG      = "V";

	// CQS Symbol	
	//   Identifier of the security used to disseminate data via the SIAC Consolidated Quotation System (CQS) and Consolidated Tape System (CTS) data feeds.
	//   Typical identifiers have 1-5 character root symbol and then 1-3 characters for suffixes. Allow up to 14 characters.
	
	// ETF
	//   Identifies whether the security is an exchange traded fund (ETF). Possible values:
	//   Y = Yes, security is an ETF
	//   N = No, security is not an ETF
	
	// Round Lot Size
	//   Indicates the number of shares that make up a round lot for the given security. Allow up to 6 digits.
	
	// Test Issue
	//   Indicates whether the security is a test security.
	//   Y = Yes, it is a test issue.
	//   N = No, it is not a test issue
	
	// NASDAQ Symbol
	//   Identifier of the security used to in various NASDAQ connectivity protocols and NASDAQ market data feeds.
	//   Typical identifiers have 1-5 character root symbol and then 1-3 characters for suffixes. Allow up to 14 characters.
	//   See below link for explanation
	//     https://www.nasdaqtrader.com/trader.aspx?id=CQSsymbolconvention
	
	public static final String SUFFIX_WARRANT     = "+";
	public static final String SUFFIX_RIGHTS      = "^";
	public static final String SUFFIX_UNITS       = "=";
	public static final String SUFFIX_WHEN_ISSUED = "#";
	public static final String SUFFIX_CALLED      = "*";

	
	// ACT Symbol|Security Name|Exchange|CQS Symbol|ETF|Round Lot Size|Test Issue|NASDAQ Symbol
	@CSVUtil.ColumnName("ACT Symbol")      public String actSymbol;
	@CSVUtil.ColumnName("Security Name")   public String name;
	@CSVUtil.ColumnName("Exchange")        public String exchange;
	@CSVUtil.ColumnName("CQS Symbol")      public String cqsSymbol;
	@CSVUtil.ColumnName("ETF")             public String etf;
	@CSVUtil.ColumnName("Round Lot Size")  public String roundLotSize;
	@CSVUtil.ColumnName("Test Issue")      public String testIssue;
	@CSVUtil.ColumnName("NASDAQ Symbol")   public String symbol;
		
	public boolean isETF() {
		return etf.equals("Y");
	}
	public boolean isTestIssue() {
		return testIssue.equals("Y");
	}
	
	public boolean isWarrant() {
		return symbol.contains(SUFFIX_WARRANT);
	}
	public boolean isRights() {
		return symbol.contains(SUFFIX_RIGHTS);
	}
	public boolean isUnits() {
		return symbol.contains(SUFFIX_UNITS);
	}
	public boolean isWhenIssed() {
		return symbol.contains(SUFFIX_WHEN_ISSUED);
	}
	public boolean isCalled() {
		return symbol.contains(SUFFIX_CALLED);
	}
	
	public boolean isStock() {
		return (isWarrant() || isRights() || isUnits()) ? false : true;
	}

	public boolean isBATS() {
		return exchange.equals(EXCHANGE_BATS);
	}
	public boolean isNYSE() {
		return exchange.equals(EXCHANGE_BATS) || exchange.equals(EXCHANGE_BATS) || exchange.equals(EXCHANGE_BATS);
	}
	
	// Remove suffix of issued and called
	public String normalizedSymbol() {
		return symbol.replace(SUFFIX_WHEN_ISSUED, "").replace(SUFFIX_CALLED, "");
	}

	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	@Override
	public int compareTo(OtherListed that) {
		return this.actSymbol.compareTo(that.actSymbol);
	}

}
