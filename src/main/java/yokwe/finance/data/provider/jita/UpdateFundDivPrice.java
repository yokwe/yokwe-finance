package yokwe.finance.data.provider.jita;

import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.hc.core5.http.HttpStatus;

import yokwe.finance.data.type.DailyValue;
import yokwe.finance.data.type.FundInfoJP;
import yokwe.util.CSVUtil;
import yokwe.util.Makefile;
import yokwe.util.Storage;
import yokwe.util.ThreadUtil;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.update.UpdateBase;

public class UpdateFundDivPrice extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static Makefile MAKEFILE = Makefile.builder().
		input(StorageJITA.FundInfoJITA).
		output(StorageJITA.FundDiv, StorageJITA.FundPrice).
		build();

	public static void main(String[] args) {
		callUpdate();
	}

	@Override
	public void update() {
		Storage.initialize();
		// updateFile() takes time and fund data is update daily.
		// set gracePeriod to 20 hours
		gracePeriod = Duration.ofHours(20);

		var fundInfoList = StorageJITA.FundInfoJITA.getList();

		for(int take = 0; take < 10; take++) {
			logger.info("retry  {}", take);
			var updateCount = updateFile(fundInfoList);
			if (updateCount == 0) {
				// touch file
				StorageJITA.FundDiv.touch();
				StorageJITA.FundPrice.touch();
				break;
			}
			ThreadUtil.sleep(SLEEP_BETWEEN_UPDATE);
		}
	}

	private int updateFile(List<FundInfoJP> fundInfoList) {
		var today = LocalDate.now();

		var http = HttpUtil.getInstance();

		var list = new ArrayList<FundInfoJP>(fundInfoList);
		logger.info("list  {}", list.size());
		list.removeIf(o -> !needsUpdate(StorageJITA.FundDiv.getFile(o.isinCode)));
		logger.info("list  {}", list.size());
		list.removeIf(o -> today.isBefore(o.inceptionDate));
		logger.info("list  {}", list.size());

		Collections.shuffle(list);

		int count = 0;
		int countMod = 0;
		for(var fundInfo: list) {
			var isinCode = fundInfo.isinCode;

			if ((++count % 100) == 1) {
				logger.info("{}  /  {}  {}", count, list.size(), isinCode);
			} else {
//				logger.info("{}  /  {}  {}", count, fundInfoList.size(), isinCode);
			}

			var referer = String.format("https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000?isinCd=%s", isinCode);
			http.withReferer(referer);

			// get rawData because http request returns bogus character set.
			var result = http.withRawData(true).downloadOrNull(getURL(fundInfo));
			if (result != null && result.code == HttpStatus.SC_OK && result.rawData.length != 0) {
				var string = new String(result.rawData, CHARSET);
				var reader = new StringReader(string);

				// save for debug
				StorageJITA.FundDivPrice.save(isinCode, string);

				var priceDivList = CSVUtil.read(CSV.class).file(reader);

				var priceList = new ArrayList<FundPrice>(list.size());
				var divList   = new ArrayList<DailyValue>();

				for(var e: priceDivList) {
					var date  = toLocalDate(e.date);
					var price = new BigDecimal(e.price);
					var nav   = new BigDecimal(e.nav).movePointRight(6); // convert million to number

					priceList.add(new FundPrice(date, price, nav));

					if (!e.div.isEmpty()) {
						var value = new BigDecimal(e.div);
						divList.add(new DailyValue(date, value));
					}
				}

				StorageJITA.FundPrice.save(isinCode, priceList);
				StorageJITA.FundDiv.save(isinCode, divList);
				countMod++;
			} else {
				logger.warn("download failed  {}  {}", fundInfo.isinCode, fundInfo.name);
			}
			ThreadUtil.sleep(SLEEP_BETWEEN_DOWNLOAD);
		}

		logger.info("countMod  {}", countMod);
		return countMod;
	}
	private static final Charset CHARSET = Charset.forName("SHIFT_JIS");
	private static String URL_FORMAT = "https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000/csv-file-download?isinCd=%s&associFundCd=%s";
	private String getURL(FundInfoJP fundInfo) {
		return String.format(URL_FORMAT, fundInfo.isinCode, fundInfo.fundCode);
	}
	private static final Duration SLEEP_BETWEEN_UPDATE   = Duration.ofMillis(2000);
	private static final Duration SLEEP_BETWEEN_DOWNLOAD = Duration.ofMillis(100);


	public static class CSV implements Comparable<CSV> {
		// 年月日	        基準価額(円)	純資産総額（百万円）	分配金	決算期
		// 2022年04月25日	19239	        1178200	                0       4
		// 2022年04月26日	19167	        1174580
		@CSVUtil.ColumnName("年月日")
		public String date;
		@CSVUtil.ColumnName("基準価額(円)")
		public String price;
		@CSVUtil.ColumnName("純資産総額（百万円）")
		public String nav;
		@CSVUtil.ColumnName("分配金")
		public String div;
		@CSVUtil.ColumnName("決算期")
		public String period;

		@Override
		public int compareTo(CSV that) {
			return this.date.compareTo(that.date);
		}
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	private static LocalDate toLocalDate(String string) {
		if (DATE_PAT.matcher(string).matches()) {
			return LocalDate.parse(string, DATE_FORMAT);
		}

		logger.error("Unexpeced string");
		logger.error("  string  {}!", string);
		throw new UnexpectedException("Unexpeced string");
	}
	private static final Pattern           DATE_PAT    = Pattern.compile("2[0-9]{3}年[0-9]{1,2}月[0-9]{1,2}日");
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy年M月d日");

}

