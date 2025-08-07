package yokwe.finance.data.provider.mizuho;

public class Quote implements Comparable<Quote> {
	public String DATE;
	public String USD;
	public String GBP;
	public String EUR;
	public String CAD;
	public String CHF;
	public String SEK;
	public String DKK;
	public String NOK;
	public String AUD;
	public String NZD;
	public String ZAR;
	public String BHD;
	public String IDR;
	public String CNY;
	public String HKD;
	public String INR;
	public String MYR;
	public String PHP;
	public String SGD;
	public String KRW;
	public String THB;
	public String KWD;
	public String SAR;
	public String AED;
	public String MXN;
	public String PGK;
	public String HUF;
	public String CZK;
	public String PLN;
	public String TRY;
	public String XXX;
	public String TWD;
	public String CNY2;
	public String KRW2;
	public String IDR2;
	public String MYR2;
	public String XPF;
	public String BRL;
	public String VND;
	public String EGP;
	public String RUB;
	
	@Override
	public int compareTo(Quote that) {
		return this.DATE.compareTo(that.DATE);
	}
}