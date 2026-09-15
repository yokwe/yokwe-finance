package yokwe.finance.data.provider.nikko;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.jita.StorageJITA;
import yokwe.util.Makefile;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;
import yokwe.util.update.UpdateBase;

public class UpdateFundList extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static Makefile MAKEFILE = Makefile.builder().
		input(StorageJITA.FundInfoJITA).
		output(StorageNikko.FundInfoNikko).
		build();

	public static void main(String[] args) {
		callUpdate();
	}

	@Override
	public void update() {
//		downloadFile();
		updateFile();
	}

	private static final String CHARSET = "SHIFT_JIS";
	private static final String URL     = "https://fund2.smbcnikko.co.jp/smbc_nikko_hp/fund/jcgi/wrapcf/qjsonp.aspx?F=ctl/fnd_list";

	void downloadFile() {
		var string = HttpUtil.getInstance().withCharset(CHARSET).downloadString(URL);
		logger.info("string  {}", string.length());
		StorageNikko.FundListString.save(string);

		var patJSON = Pattern.compile("fnd_list\\((?<jsonString>.+)\\)");
		var m = patJSON.matcher(string);
		if (m.matches() && m.groupCount() == 1) {
			var jsonString = m.group("jsonString");
			logger.info("jsonString  {}", jsonString.length());
			StorageNikko.FundListJSON.save(jsonString);
		} else {
			logger.info("Unexpected string");
			throw new UnexpectedException("Unexpected string");
		}
	}

	void updateFile() {
		var string = StorageNikko.FundListJSON.load();
		var json = JSON.unmarshal(FundList.class, string);

		var jitaMap = StorageJITA.FundInfoJITA.load().stream().collect(Collectors.toMap(o -> o.fundCode, Function.identity()));
		logger.info("fundCodeMap {}", jitaMap.size());

		logger.info("data {}", json.section1.data.length);
		int countA = 0;
		int countB = 0;
		int countC = 0;

		var fundInfoList = new ArrayList<FundInfoNikko>();

		for(var e: json.section1.data) {
			var fundCode = e.FundCode;
			if (jitaMap.containsKey(fundCode)) {
				var jita = jitaMap.get(fundCode);

				var fundInfo = new FundInfoNikko();

			    fundInfo.fundCode = fundCode;

				fundInfo.isinCode = jita.isinCode;
			    fundInfo.fundName = jita.name;

			    fundInfo.quickFundRisk     = e.QuickFundRisk;
			    fundInfo.morningstarRating = e.MorningstarRating;
			    fundInfo.noload            = e.IsNoload;

			    if (e.HasPurchaseOrderButton) {
					countA++;

			    	fundInfo.directCourse  = !e.IsDirectCourse.isEmpty();
			    	fundInfo.generalCourse = !e.IsGeneralCourse.isEmpty();

			    	// sanity check
			    	if (e.IsDirectCourseOnly) {
			    		if (!fundInfo.directCourse || fundInfo.generalCourse) {
			    			logger.info("XX  {}  {}  {}  {}  {}", e.FundCode, e.IsDirectCourse, e.IsGeneralCourse, e.IsDirectCourseOnly, e.FundName);
			    		}
			    	} else {
			    		if (!fundInfo.generalCourse) {
			    			logger.info("YY  {}  {}  {}  {}  {}", e.FundCode, e.IsDirectCourse, e.IsGeneralCourse, e.IsDirectCourseOnly, e.FundName);
			    		}
			    	}
			    	if (!fundInfo.directCourse && !fundInfo.generalCourse) {
		    			logger.info("ZZ  {}  {}  {}  {}  {}", e.FundCode, e.IsDirectCourse, e.IsGeneralCourse, e.IsDirectCourseOnly, e.FundName);
			    	}
			    } else {
					countB++;

				    fundInfo.generalCourse = false;
				    fundInfo.directCourse  = false;

//				    logger.info("AA  {}  {}", fundInfo.fundCode, fundInfo.fundName);
			    }

				fundInfoList.add(fundInfo);
			} else {
				countC++;
			}
		}

		logger.info("countA  {}", countA);
		logger.info("countB  {}", countB);
		logger.info("countC  {}", countC);

		logger.info("save  {}  {}", fundInfoList.size(), StorageNikko.FundInfoNikko.getFile());
		StorageNikko.FundInfoNikko.save(fundInfoList);
	}

}
