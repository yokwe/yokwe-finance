package yokwe.finance.data.provider.nasdaq.api;

import yokwe.util.ToString;
import yokwe.util.json.JSON;

public class CompanyProfile {
	// https://api.nasdaq.com/api/company/AACI/company-profile
	
	public static String getURL(String symbol) {
		return String.format("https://api.nasdaq.com/api/company/%s/company-profile", API.encodeSymbolForURL(symbol));
	}
	
	public static CompanyProfile getInstance(String symbol) {
		String url  = getURL(symbol);
		return API.getInstance(CompanyProfile.class, url);
//		return API.getInstance(Info.class, url, getPath(symbol));
	}

	public static class Data {
		@JSON.Name("ModuleTitle")        @JSON.Ignore public LabelValue moduleTitle;
		@JSON.Name("CompanyName")                     public LabelValue companyName;
		@JSON.Name("Symbol")                          public LabelValue symbol;
		@JSON.Name("Address")            @JSON.Ignore public LabelValue address;
        @JSON.Name("Phone")              @JSON.Ignore public LabelValue phone;
        @JSON.Name("Industry")                        public LabelValue industry;
        @JSON.Name("Sector")                          public LabelValue sector;
        @JSON.Name("Region")                          public LabelValue region;
        @JSON.Name("CompanyDescription") @JSON.Ignore public LabelValue companyDescription;
        @JSON.Name("CompanyUrl")         @JSON.Ignore public LabelValue companyURL;
        @JSON.Name("KeyExecutives")      @JSON.Ignore public LabelValue keyExecutive;
	}
	
	public Data   data;
	public String message;
	public Status status;
	
	public CompanyProfile() {
		data    = null;
		message = null;
		status  = null;
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}
