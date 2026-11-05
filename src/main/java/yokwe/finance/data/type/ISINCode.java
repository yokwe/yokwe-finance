package yokwe.finance.data.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

import yokwe.finance.data.provider.jita.StorageJITA;

public class ISINCode {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	// See https://en.wikipedia.org/wiki/International_Securities_Identification_Number

	private static final Pattern PAT_ISIN_CODE = Pattern.compile("[A-Z]{2}[A-Z0-9]{10}");

	public static boolean isValidCode(String string) {
		// return true if string has correct check digit

		if (string == null) {
			return false;
		}

		var m = PAT_ISIN_CODE.matcher(string);
		if (m.matches()) {
			// verify check digit
			var list = new ArrayList<Integer>();
			for(int i = 0; i < 11; i++) {
				var c = string.charAt(i);

				var n = (c <= '9') ? (c - '0') : (c - 55);
				if (n < 10) {
					list.add(n);
				} else {
					list.add(n / 10);
					list.add(n % 10);
				}
			}
			Collections.reverse(list);
			int sum = 0;
			for(int i = 0; i < list.size(); i++) {
				int n = list.get(i);
				if ((i % 2) == 0) {
					n = n + n;
				}
				if (n < 10) {
					sum += n;
				} else {
					sum += (n % 10) + 1;
				}
			}
			var expect = (10 - (sum % 10)) % 10;
			var actual = string.charAt(11) - '0';

//			logger.info("expect  {}  actual  {}", expect, actual);

			return expect == actual;
		} else {
			return false;
		}
	}

	public static void main(String[] args) {
		logger.info("START");

		{
			for(var e: StorageJITA.FundInfoJITA.getList()) {
				if (!isValidCode(e.isinCode)) {
					logger.info("XX  {}  {}", e.isinCode, e.name);
				}
			}
		}

		logger.info("STOP");
	}


}
