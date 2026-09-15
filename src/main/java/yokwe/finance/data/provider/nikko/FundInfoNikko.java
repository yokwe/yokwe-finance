package yokwe.finance.data.provider.nikko;

import yokwe.util.ToString;

public class FundInfoNikko implements Comparable<FundInfoNikko> {
    public String     isinCode;
    public String     fundCode;

    public String     quickFundRisk;
    public String     morningstarRating;

    public boolean    generalCourse;
    public boolean    directCourse;

    public boolean    noload;

    public String     fundName;


	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}

	@Override
	public int compareTo(FundInfoNikko that) {
		return this.isinCode.compareTo(that.isinCode);
	}
}
