package yokwe.finance.data.provider.nasdaq;

import yokwe.util.CSVUtil;
import yokwe.util.ToString;

public class NasdaqListed implements Comparable<NasdaqListed> {
	// Symbol|Security Name|Market Category|Test Issue|Financial Status|Round Lot Size|ETF|NextShares
	// AACG|ATA Creativity Global - American Depositary Shares, each representing two common shares|G|N|N|100|N|N
	// ZYXI|Zynex, Inc. - Common Stock|Q|N|N|100|N|N
	// File Creation Time: 1122202221:30|||||||
		
	// See below URL for detail
	//   http://www.nasdaqtrader.com/trader.aspx?id=symboldirdefs
	
	// Market Category	The category assigned to the issue by NASDAQ based on Listing Requirements. Values:
	//  Q = NASDAQ Global Select MarketSM
	// 	G = NASDAQ Global MarketSM
	// 	S = NASDAQ Capital Market
	
	// Test Issue	Indicates whether or not the security is a test security. Values:
	//   Y = yes, it is a test issue.
	//   N = no, it is not a test issue.
	
	// Financial Status	Indicates when an issuer has failed to submit its regulatory filings on a timely basis,
	// has failed to meet NASDAQ's continuing listing standards, and/or has filed for bankruptcy. Values include:
	//   D = Deficient: Issuer Failed to Meet NASDAQ Continued Listing Requirements
	//   E = Delinquent: Issuer Missed Regulatory Filing Deadline
	//   Q = Bankrupt: Issuer Has Filed for Bankruptcy
	//   N = Normal (Default): Issuer Is NOT Deficient, Delinquent, or Bankrupt.
	//   G = Deficient and Bankrupt
	//   H = Deficient and Delinquent
	//   J = Delinquent and Bankrupt
	//   K = Deficient, Delinquent, and Bankrupt
	
	// Round Lot	Indicates the number of shares that make up a round lot for the given security.
		
	// NASDAQ Integrated Platform Suffix
	// See page below
	//   Ticker Symbol Convention
	//   https://www.nasdaqtrader.com/trader.aspx?id=CQSsymbolconvention
	
	// Symbol|Security Name|Market Category|Test Issue|Financial Status|Round Lot Size|ETF|NextShares
	@CSVUtil.ColumnName("Symbol")           public String symbol;
	@CSVUtil.ColumnName("Security Name")    public String name;
	@CSVUtil.ColumnName("Market Category")  public String marketCategory;
	@CSVUtil.ColumnName("Test Issue")       public String testIssue;
	@CSVUtil.ColumnName("Financial Status") public String financialStatus;
	@CSVUtil.ColumnName("Round Lot Size")   public String roundLotSize;
	@CSVUtil.ColumnName("ETF")              public String etf;
	@CSVUtil.ColumnName("NextShares")       public String nextShare;
	
	
	public boolean isTestIssue() {
		return testIssue.equals("Y");
	}
	public boolean isFinancialNormal() {
		return financialStatus.equals("N");
	}
	public boolean isETF() {
		return etf.equals("Y");
	}
	
	
	public boolean isWarrant() {
		return (symbol.length() == 5 && symbol.charAt(4) == 'W');
	}
	public boolean isRights() {
		return (symbol.length() == 5 && symbol.charAt(4) == 'R');
	}
	public boolean isUnits() {
		return (symbol.length() == 5 && symbol.charAt(4) == 'U');
	}

	public boolean isStock() {
		return (isWarrant() || isRights() || isUnits()) ? false : true;
	}

	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	@Override
	public int compareTo(NasdaqListed that) {
		return this.symbol.compareTo(that.symbol);
	}

}
