package yokwe.finance.data.provider.bats;

import yokwe.util.CSVUtil;
import yokwe.util.ToString;

public class ListedSecurityReport implements Comparable<ListedSecurityReport> {
	// Daily Listed Securities Report
	// Web Page
	//   https://www.cboe.com/us/equities/market_statistics/listed_securities/
	// File Location
	//   ftp://ftp.batstrading.com/bzx-equities/listed-securities/bzx_equities_listed_security_rpt_20230519.txt
	//   https://www.cboe.com/us/equities/market_statistics/listed_securities/2023/05/bzx_equities_listed_security_rpt_20230519.txt-dl
	// Data format
	//   https://cdn.cboe.com/resources/membership/BATS_BZX_Exchange_US_Listings_Corporate_Actions_Specification.pdf
	// CSM Symbology
	//   https://cdn.cboe.com/resources/membership/US_Symbology_Reference.pdf
	
	// From Cboe BZX Exchange US Listings Corporate Actions Specification
	//   https://cdn.cboe.com/resources/membership/BATS_BZX_Exchange_US_Listings_Corporate_Actions_Specification.pdf
	//
	// Daily Listed Securities Report Fields
	//
	// Symbol
	//   Symbol of Listed Security (upper case) in CMS Symbology.
	//   See below page for CMS Symbology
	//   https://cdn.cboe.com/resources/membership/US_Symbology_Reference.pdf
	// CUSIP
	//   The nine-character CUSIP assigned to the symbol on the day the report is published.
	// Issue Name
	//   A text field representing the name of the issue.
	// Issue Type
	//   The type of issue: common, preferred, etc. Allowed values:
	//   Commodity Futures Trust Shares
	//   Commodity Index Trust Shares
	//   Commodity-Based Trust Shares
	//   Commodity-Linked Securities
	//   Convertible Debt
	//   Currency Trust Shares
	//   Currency Warrants
	//   Derivative Securities Traded under UTP
	//   Equity Gold Shares
	//   Equity Index-Linked Securities
	//   Exchange-Traded Fund Shares
	//   Fixed Income Index-Linked Securities
	//   Futures-Linked Securities
	//   Index Fund Shares
	//   Index Warrants
	//   Index-Linked Exchangeable Notes
	//   Managed Fund Shares
	//   Managed Portfolio Shares
	//   Managed Trust Securities
	//   Multifactor Index-Linked Securities
	//   Other Securities
	//   Partnership Units
	//   Portfolio Depository Receipts
	//   Preferred Stock
	//   Primary Equity
	//   Right
	//   Secondary Class of Common
	//   Selected Equity-linked Debt Securities (SEEDS)
	//   Tracking Fund Shares
	//   Trust Certificates
	//   Trust Issued Receipts
	//   Trust Units
	//   Units
	//   Warrant
	// Currency
	//   Currency of the issue (ISO code)
	// Outstanding Shares
	//   Integer representing the number of shares outstanding for the issue.
	//   Will be “N/A” for all ETPs.
	// Test Symbol
	//   Indicates whether or not the issue is a test symbol. Allowed values:
	//   Y = Yes
	//   N = No
	// Market Category
	//   The Market Category of a security. Allowed values:
	//   Tier 1
	//   Tier 2
	// First Date Traded
	//   The date the Issue first traded as a BZX Listed Issue
	// IPO Flag
	//   Indicates if the issue conducted an Initial Public Offering on Cboe. Allowed values:
	//   Y = Yes
	//   N = No
	// Expiration Date
	//   Date (in YYYY-MM-DD format) that a security (warrant, when issued, etc.) expires.
	// Separation Date
	//   Date (in YYYY-MM-DD format) that a unit or warrant is separating from the associated common stock.
	// When Issued Flag
	//   Indicates if the issue is in a "when-issued" status. Allowed values:
	//   Y = Yes
	//   N = No
	// When Distributed Flag
	//   Indicates if the issue is in a "when-distributed" status. Allowed values:
	//   Y = Yes
	//   N = No
	// Round Lot Quantity
	//   Integer indicating the number of shares that define a round lot.
	// Notes
	//   Free-form text field for notes pertaining to the issue.
	// Financial Status
	//   Values:
	//   0 = Normal
	//   1 = Bankrupt
	//   2 = Below Continuing Listing Standards
	//   3 = Bankrupt & Below Continuing Listing Standards
	//   4 = Late Filing
	//   5 = Bankrupt & Late Filing
	//   6 = Below Continuing Listing Standards & Late Filing
	//   7 = Bankrupt, Below Continuing Listing Standards & Late Filing 8 = Creations Suspended (for Exchange Traded Product)
	//   9 = Redemptions Suspended (for Exchange Traded Product)
	//   A = Liquidation (for Exchange Traded Product)

	// First three lines of file
	// ----
	// Environment=PROD|Report Date=2023-05-19|Record Count=619
	// Symbol|Issue Name|Issue Type|Currency|Outstanding Shares|Test Symbol|Market Category|First Date Traded|IPO Flag|Expiration Date|Separation Date|When Issued Flag|When Distributed Flag|Round Lot Quantity|Notes|Financial Status
	// AAAU|Goldman Sachs Physical Gold ETF Shares|Commodity-Based Trust Shares|USD|n/a|N|Tier 1|2022-02-03|N|||N|N|100||0
	// ----
	
	// Last three lines of file
	// ----
	// ZIVB|-1x Short VIX Mid-Term Futures Strategy ETF|Exchange-Traded Fund Shares|USD|n/a|N|Tier 1|2023-04-19|Y|||N|N|100||0
	// ZTEST|Cboe Test|Primary Equity|USD|0|Y|Tier 1|2018-08-22|N|||N|N|100||0
	// ZTST|MPS Test Symbol|Managed Portfolio Shares|USD|n/a|Y|Tier 1|2020-01-15|Y|||N|N|100||0
	// ----
	
	// Symbol|CUSIP|Issue Name|Issue Type|Currency|Outstanding Shares|Test Symbol|Market Category|First Date Traded|IPO Flag|Expiration Date|Separation Date|When Issued Flag|When Distributed Flag|Round Lot Quantity|Notes|Financial Status
	@CSVUtil.ColumnName("Symbol")                 public String symbol;
	@CSVUtil.ColumnName("CUSIP")                  public String cusip;
	@CSVUtil.ColumnName("Issue Name")             public String issueName;
	@CSVUtil.ColumnName("Issue Type")             public String issueType;
	@CSVUtil.ColumnName("Currency")               public String currency;
	@CSVUtil.ColumnName("Outstanding Shares")     public String outstandingShares;
	@CSVUtil.ColumnName("Test Symbol")            public String testSymbol;
	@CSVUtil.ColumnName("Market Category")        public String marketCategory;
	@CSVUtil.ColumnName("First Date Traded")      public String firstDateTraded;
	@CSVUtil.ColumnName("IPO Flag")               public String ipoFlag;
	@CSVUtil.ColumnName("Expiration Date")        public String expirationDate;
	@CSVUtil.ColumnName("Separation Date")        public String separationDate;
	@CSVUtil.ColumnName("When Issued Flag")       public String whenIssuedFlag;
	@CSVUtil.ColumnName("When Distributed Flag")  public String whenDistributedFlag;
	@CSVUtil.ColumnName("Round Lot Quantity")     public String roundLotQuantity;
	@CSVUtil.ColumnName("Notes")                  public String notes;
	@CSVUtil.ColumnName("Financial Status")       public String financialStatus;
	
	
	public boolean isTestSymbol() {
		return testSymbol.equals("Y");
	}
	public boolean isFinancialNormal() {
		return financialStatus.equals("0");
	}

		
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	@Override
	public int compareTo(ListedSecurityReport that) {
		return this.symbol.compareTo(that.symbol);
	}
}
