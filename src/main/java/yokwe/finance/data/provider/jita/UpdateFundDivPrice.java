package yokwe.finance.data.provider.jita;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import yokwe.finance.data.type.DailyValue;
import yokwe.finance.data.type.FundInfoJP;
import yokwe.finance.data.type.FundPriceJP;
import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;
import yokwe.util.Makefile;
import yokwe.util.Storage;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.update.UpdateBase;

public class UpdateFundDivPrice extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static Makefile MAKEFILE = Makefile.builder().
		input(StorageJITA.FundInfo).
		output(StorageJITA.FundDiv, StorageJITA.FundPrice).
		build();

	public static void main(String[] args) {
		callUpdate();
	}

	@Override
	public void update() {
		Storage.initialize();

		var fundInfoList = StorageJITA.FundInfo.getList();

		// delist unknown
		delistUnknownFile(fundInfoList);

		for(int take = 1; take < 9; take++) {
			logger.info("start take {}", take);
			int countMod = updateFile(fundInfoList);
			if (countMod == 0) {
				// touch file
				StorageJITA.FundDiv.touch();
				StorageJITA.FundPrice.touch();
				break;
			}
			sleep(SLEEP_BETWEEN_UPDATE);
		}
	}
	private static final Duration SLEEP_BETWEEN_UPDATE   = Duration.ofMillis(2000);
	private static final Duration SLEEP_BETWEEN_DOWNLOAD = Duration.ofMillis(100);


	private void sleep(Duration duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			//
		}
	}

	private void delistUnknownFile(List<FundInfoJP> fundInfoList) {
		var validNameList = fundInfoList.stream().map(o -> o.isinCode).toList();

		StorageJITA.FundDivPrice.delistUnknownFile(validNameList);
		StorageJITA.FundDiv.delistUnknownFile(validNameList);
		StorageJITA.FundPrice.delistUnknownFile(validNameList);

		// remove incomplete file
		for(var fundInfo: fundInfoList) {
			var file = StorageJITA.FundDivPrice.getFile(fundInfo.isinCode);
			removeInvalidFile(file);
		}
	}

	private void removeInvalidFile(File file) {
		if (file.exists()) {
			var string = FileUtil.read().file(file);
			if (!string.startsWith(CSV_HEADER)) {
				file.delete();
			}
		}
	}
	private static final String CSV_HEADER = "年月日,基準価額(円),純資産総額（百万円）,分配金,決算期";


	private static final Charset CHARSET = Charset.forName("SHIFT_JIS");

	private static String URL_FORMAT = "https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000/csv-file-download?isinCd=%s&associFundCd=%s";
	private String getURL(FundInfoJP fundInfo) {
		return String.format(URL_FORMAT, fundInfo.isinCode, fundInfo.fundCode);
	}

	protected int updateFile(List<FundInfoJP> fundInfoList) {
		logger.info("fundInfoList  {}", fundInfoList.size());

		// build list
		var list = new ArrayList<FundInfoJP>();
		{
			for(var fundInfo: fundInfoList) {
				var file = StorageJITA.FundDivPrice.getFile(fundInfo.isinCode);
				if (file.exists()) {
					if (needsUpdate(file)) {
						list.add(fundInfo);
					}
				} else {
					list.add(fundInfo);
				}
			}
		}
		logger.info("list          {}", list.size());

		Collections.shuffle(list);

		var http = HttpUtil.getInstance();

		int count = 0;
		int countMod = 0;
		for(var fundInfo: list) {
			var isinCode = fundInfo.isinCode;

			if ((++count % 100) == 1) {
				logger.info("{}  /  {}  {}", count, list.size(), isinCode);
			} else {
//				logger.info("{}  /  {}  {}", count, list.size(), isinCode);
			}

			var referer = String.format("https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000?isinCd=%s", isinCode);
			http.withReferer(referer);

			var file = StorageJITA.FundDivPrice.getFile(isinCode);

			var rawData = http.downloadRaw(getURL(fundInfo));
			sleep(SLEEP_BETWEEN_DOWNLOAD);
			var string = new String(rawData, CHARSET);
			if (string.startsWith(CSV_HEADER)) {
				FileUtil.write().file(file, string);
			} else {
				// unexpected result
				logger.warn("{}  /  {}  {}  UNEXPECTED RESULT", count, list.size(), isinCode);
				return -1;
			}

			List<FundDivPrice> divPriceList;
			{
				divPriceList = CSVUtil.read(FundDivPrice.class).file(file);
				if (divPriceList == null) {
					logger.error("Unexpected null");
					logger.error("  file  {}", file.getPath());
					throw new UnexpectedException("Unexpected null");
				}
			}

			// build divList and priceList
			var divList   = new ArrayList<DailyValue>();
			var priceList = new ArrayList<FundPriceJP>();

			for(var divPrice: divPriceList) {
				// sanity check
				if (divPrice.price.isEmpty() || divPrice.nav.isEmpty()) {
					logger.warn("Skip unexpected divPrice  {}  {}", isinCode, divPrice);
					continue;
				}
				LocalDate  date   = toLocalDate(divPrice.date);
				BigDecimal price  = new BigDecimal(divPrice.price);
				BigDecimal nav    = new BigDecimal(divPrice.nav).scaleByPowerOfTen(6); // 純資産総額（百万円）
				String     div    = divPrice.div.trim();

				if (!div.isEmpty()) {
					divList.add(new DailyValue(date, new BigDecimal(div)));
				}

				priceList.add(new FundPriceJP(date, nav, price));
			}

			// save divList and priceList
			StorageJITA.FundDiv.save(isinCode, divList);
			StorageJITA.FundPrice.save(isinCode, priceList);

			countMod++;
		}

		return countMod;
	}

	private LocalDate toLocalDate(String dateString) {
		// 2000年01月01日
		// 01234 567 890
		if (dateString.length() == 11 && dateString.charAt(4) == '年' && dateString.charAt(7) == '月' && dateString.charAt(10) == '日') {
			int yyyy = Integer.parseInt(dateString.substring(0, 4));
			int mm   = Integer.parseInt(dateString.substring(5, 7));
			int dd   = Integer.parseInt(dateString.substring(8, 10));
			return LocalDate.of(yyyy, mm, dd);
		} else {
			logger.error("Unexpected date");
			logger.error("  dateString {}  !{}!", dateString.length(), dateString);
			throw new UnexpectedException("Unexpected date");
		}
	}
}
