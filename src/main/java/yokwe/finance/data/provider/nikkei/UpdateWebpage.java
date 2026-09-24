package yokwe.finance.data.provider.nikkei;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import yokwe.finance.data.provider.jita.StorageJITA;
import yokwe.finance.data.type.FundInfoJP;
import yokwe.util.Makefile;
import yokwe.util.ThreadUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.update.UpdateBase;

public class UpdateWebpage  extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static Makefile MAKEFILE = Makefile.builder().
//		input(StorageJITA.FundInfoJITA).
		output(StorageNikkei.Webpage).
		build();

	public static void main(String[] args) {
		callUpdate();
	}

	private static final Duration SLEEP_BETWEEN_RETRY          = Duration.ofSeconds(1);
	private static final Duration SLEEP_BETWEEN_DOWNLOAD       = Duration.ofSeconds(1);
	private static final Duration SLEEP_AFTER_DOWNLOAD_FAILURE = Duration.ofSeconds(10);

	private static final int MAX_RETRY = 5;

	@Override
	public void update() {
		var list = getList();
		logger.info("list  {}", list.size());

		for(int retry = 1; retry <= MAX_RETRY; retry++) {
			logger.info("  retry  {}", retry);
			var taskList = getTaskList(list);
			logger.info("  task   {}", taskList.size());
			if (taskList.isEmpty()) {
				break;
			}

			downloadFile(taskList);

			ThreadUtil.sleep(SLEEP_BETWEEN_RETRY);
		}

		StorageNikkei.Webpage.touch();
	}

	private List<FundInfoJP> getList() {
		var ret = StorageJITA.FundInfoJITA.getList();
		logger.info("list  {}", ret.size());

		// skip if inceptionDate is after today
		ret.removeIf(o -> o.inceptionDate.isAfter(LocalDate.now()));
		logger.info("list  {}", ret.size());

		ret.removeIf(o -> o.fundType.equals(FundInfoJP.FUND_TYPE_CEF));
		logger.info("list  {}", ret.size());

		ret.removeIf(o -> o.name.contains("マネー・リザーブ・ファンド"));
		ret.removeIf(o -> o.name.contains("マネーファンド"));
		logger.info("list  {}", ret.size());

		ret.removeIf(o -> o.name.contains("公社債投信"));
		ret.removeIf(o -> o.name.contains("公社債投資信託"));
		ret.removeIf(o -> o.name.contains("公社債証券投資信託"));
		logger.info("list  {}", ret.size());

		ret.removeIf(o -> o.name.contains("財形給付金ファンド"));
		logger.info("list  {}", ret.size());

		return ret;
	}
	private List<FundInfoJP> getTaskList(List<FundInfoJP> list) {
		var ret = new ArrayList<FundInfoJP>();

		int countA = 0;
		int countB = 0;

		for(var e: list) {
			// skip if file exists
			if (StorageNikkei.Webpage.getFile(e.fundCode).canRead()) {
				countA++;
				continue;
			} else {
				countB++;
				ret.add(e);
//				logger.info("XX  {}  {}", e.fundCode, e.name);
			}
		}

		logger.info("countA  {}", countA);
		logger.info("countB  {}", countB);

		return ret;
	}
	void downloadFile(List<FundInfoJP> list) {
		var urlFormat = "https://www.nikkei.com/nkd/fund/dividend/?fcode=%s";

		Collections.shuffle(list); // shuffle

		int count = 0;
		for(var fundInfo: list) {
			if ((count++ % 50) == 0) {
				logger.info("dowonloadFile  {}  /  {}", count, list.size());
			}

			try {
				var fundCode = fundInfo.fundCode;
				var url      = String.format(urlFormat, fundCode);
				var string   = HttpUtil.getInstance().downloadString(url);
				StorageNikkei.Webpage.save(fundCode, string);
				ThreadUtil.sleep(SLEEP_BETWEEN_DOWNLOAD);
			} catch (UnexpectedException e) {
				logger.warn("failed to download  {}  {}", fundInfo.fundCode, fundInfo.name);
				ThreadUtil.sleep(SLEEP_AFTER_DOWNLOAD_FAILURE);
			}
		}
	}
}