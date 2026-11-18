package yokwe.finance.data.provider.jita;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import yokwe.finance.data.analysis.StorageAnalysis;
import yokwe.finance.data.provider.jpx.StockCodeName;
import yokwe.finance.data.type.FundInfoJP;
import yokwe.util.FileUtil;
import yokwe.util.Makefile;
import yokwe.util.ThreadUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;
import yokwe.util.update.UpdateBase;

public class UpdateFundInfo extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static Makefile MAKEFILE = Makefile.builder().
		input(StorageAnalysis.ForeignTaxAdjusted).
		output(StorageJITA.FundInfoJITA).
		build();

	public static void main(String[] args) {
		logger.info("START");

		callUpdate();

		logger.info("STOP");
	}

	@Override
	public void update() {
		this.gracePeriod = Duration.ofHours(20);

		downloadFile();
		updateFile();
	}

	private void downloadFile() {
		if (!needsUpdate(StorageJITA.FundInfoPage.getFile(getName(0)))) {
			return;
		}

		// delete old json file
		FileUtil.deleteFile(StorageJITA.FundInfoPage.getDir(), o -> o.getName().endsWith(".json"));

		int pageNo    = 0;
		int startNo   = 0;
		for(;;) {
			var string = HttpUtil.getInstance().withPost(getBody(startNo), CONTENT_TYPE).downloadString(URL);
			StorageJITA.FundInfoPage.save(getName(pageNo), string);

			// sanity check
			var data = JSON.unmarshal(FundDataSearch.class, string);
			if (data == null) {
				logger.error("JSON unmarshal failed");
				logger.error("  string {}", string);
				throw new UnexpectedException("JSON unmarshal failed");
			}

			var allPageNo = Integer.valueOf(data.searchResultInfo.allPageNo);
			var pageSize  = Integer.valueOf(data.searchResultInfo.pageSize);

			if ((pageNo % 10) == 0) {
				logger.info("{}  /  {}", pageNo, allPageNo);
			} else {
//				logger.info("{}  /  {}", pageNo, allPageNo);
			}

			// update for next iteration
			pageNo  += 1;
			startNo += pageSize;
			if (allPageNo <= pageNo) {
				break;
			}

			// speed bump
			ThreadUtil.sleep(Duration.ofMillis(500));
		}
	}
	private static final String URL          = "https://toushin-lib.fwg.ne.jp/FdsWeb/FDST999900/fundDataSearch";
	private static final String CONTENT_TYPE = "application/json";
	private String getName(int pageNo) {
		return String.format("%03d", pageNo);
	}
	private String getBody(int startNo) {
		return String.format(
			"""
			{"s_kensakuKbn":"1","s_supplementKindCd":"1","f_etfKBun":"1","s_standardPriceCond1":"0","s_standardPriceCond2":"0","s_riskCond1":"0","s_riskCond2":"0","s_sharpCond1":"0","s_sharpCond2":"0","s_buyFee":"1","s_trustReward":"1","s_monthlyCancelCreateVal":"1","s_nisaGrowthCd":"1","s_investAssetKindCd":[],"s_investArea3kindCd":[],"s_instCd":[],"s_fdsInstCd":[],"s_dcFundCD":[],"t_investArea10kindCd":[],"t_investAssetKindCd":[],"t_instCd":[],"t_fdsInstCd":[],"s_investArea10kindCd":[],"s_setlFqcy":[],"s_dividend1y":[],"s_totalNetAssets":[],"s_nowToRedemptionDate":[],"s_establishedDateToNow":[],"s_isinCd":[],"startNo":%d,"draw":2,"searchBtnClickFlg":true}"""
			, startNo);
	}


	private void updateFile() {
		updateJSONFile();


	}

	private void updateJSONFile() {
		var fileArray = StorageJITA.FundInfoPage.getDir().listFiles(o -> o.getName().endsWith(".json"));
		Arrays.sort(fileArray);
		logger.info("updateFile  {}", fileArray.length);

		var list = new ArrayList<FundInfoJP>(fileArray.length);

		int count = 0;
		for(var file: fileArray) {
			if ((count++ % 100) == 0) {
				logger.info("{}  /  {}", count, fileArray.length);
			} else {
//				logger.info("{}  /  {}", count, fileArray.length);
			}
			var string = FileUtil.read().file(file);
			var data = JSON.unmarshal(FundDataSearch.class, string);
			if (data == null) {
				logger.error("JSON unmarshal failed");
				logger.error("  string {}", string);
				throw new UnexpectedException("JSON unmarshal failed");
			}

			for(var e: data.searchResultInfo.resultInfoMapList) {
				list.add(toFundInfo(e));
			}
		}

		logger.info("save  {}  {}", list.size(), StorageJITA.FundInfoJITA.getFile());
		StorageJITA.FundInfoJITA.save(list);
	}

	private FundInfoJP toFundInfo(FundDataSearch.ResultInfo resultInfo) {
		String    isinCode       = resultInfo.isinCd;
		String    fundCode       = resultInfo.associFundCd;
		String    stockCode      = StockCodeName.getStockCode(isinCode, "");

		LocalDate listingDate;
		{
			// 2016-07-29 00:00:00
			listingDate = LocalDate.parse(resultInfo.establishedDate, ESTABLISHED_DATE_FORMAT);
		}

		LocalDate redemptionDate;
		{
			if (resultInfo.redemptionDate.equals(FundInfoJP.REDEMPTION_DATE_NONE_STRING)) {
				redemptionDate = FundInfoJP.REDEMPTION_DATE_NONE;
			} else {
				redemptionDate = LocalDate.parse(resultInfo.redemptionDate, REDEMPTION_DATE_FORMAT);
			}
		}

		int        divFreq = resultInfo.setlFqcy.equals("-") ? 0 : Integer.parseInt(resultInfo.setlFqcy);
		String     name    = resultInfo.fundNm;

		BigDecimal expenseRatio = new BigDecimal(resultInfo.trustReward).scaleByPowerOfTen(-2); // percent to value
		BigDecimal buyFreeMax   = (resultInfo.buyFee != null) ? new BigDecimal(resultInfo.buyFee).scaleByPowerOfTen(-2) : BigDecimal.ZERO;

		String     fundType = FundDataSearch.FundType.getInstance(resultInfo.unitOpenDiv).getName();

		String investingArea;
		{
			List<String> areaList = new ArrayList<>();
			if (resultInfo.investArea10kindCd1.equals("1")) {
				areaList.add(FundDataSearch.InvestingArea.getInstance("1").getName());
			}
			if (resultInfo.investArea10kindCd2.equals("1")) {
				areaList.add(FundDataSearch.InvestingArea.getInstance("2").getName());
			}
			if (resultInfo.investArea10kindCd3.equals("1")) {
				areaList.add(FundDataSearch.InvestingArea.getInstance("3").getName());
			}
			if (resultInfo.investArea10kindCd4.equals("1")) {
				areaList.add(FundDataSearch.InvestingArea.getInstance("4").getName());
			}
			if (resultInfo.investArea10kindCd5.equals("1")) {
				areaList.add(FundDataSearch.InvestingArea.getInstance("5").getName());
			}
			if (resultInfo.investArea10kindCd6.equals("1")) {
				areaList.add(FundDataSearch.InvestingArea.getInstance("6").getName());
			}
			if (resultInfo.investArea10kindCd7.equals("1")) {
				areaList.add(FundDataSearch.InvestingArea.getInstance("7").getName());
			}
			if (resultInfo.investArea10kindCd8.equals("1")) {
				areaList.add(FundDataSearch.InvestingArea.getInstance("8").getName());
			}
			if (resultInfo.investArea10kindCd9.equals("1")) {
				areaList.add(FundDataSearch.InvestingArea.getInstance("9").getName());
			}
			if (resultInfo.investArea10kindCd10.equals("1")) {
				areaList.add(FundDataSearch.InvestingArea.getInstance("10").getName());
			}

			investingArea = String.join(" ", areaList);
		}

		String investingAsset = FundDataSearch.InvestingAsset.getInstance(resultInfo.investAssetKindCd).getName();
		String indexFundType  = FundDataSearch.IndexFundType.getInstance(resultInfo.supplementKindCd).getName();
		String settlementDate;

		{
			var setlDate = resultInfo.setlDate.replace(",", "");

			if (setlDate.equals("-")) {
				logger.info("XX  {}  {}  {}  {}", setlDate, listingDate, isinCode, name);
				settlementDate = "";
			} else if (setlDate.equals("毎日決算")) {
				logger.info("XX  {}  {}  {}", setlDate, isinCode, name);
				settlementDate = "毎日決算";
			} else {
				var monthList = new ArrayList<String>();
				var daySet    = new TreeSet<String>();
				for(var e: setlDate.split(" ")) {
					var monthDay = e.split("\\/");

					if (monthDay.length == 2) {
						var month = monthDay[0];
						var day   = monthDay[1];

						monthList.add(String.valueOf(Integer.valueOf(month)));
						daySet.add(String.valueOf(Integer.valueOf(day)));
					} else {
						logger.error("Unexpected setlDate");
						logger.error("XX  {}  {}  {}", setlDate, isinCode, name);
						throw new UnexpectedException("Unexpected setlDate");
					}
				}

				if (daySet.size() != 1) {
					logger.info("XX  {}  {}  {}", setlDate, isinCode, name);
					settlementDate = setlDate;
				} else {
					if (monthList.size() == 12) { // every month
						settlementDate = "*/" + daySet.first();
					} else {
						settlementDate = String.join(",", monthList).toString() + "/" + daySet.first();
					}
				}
			}
		}


		BigDecimal totalNetAssets;
		{
			var string = resultInfo.totalNetAssets;
			if (string == null) {
				logger.info("ZZ  {}  {}", isinCode, name);
				totalNetAssets = BigDecimal.ZERO;
			} else {
				totalNetAssets = new BigDecimal(resultInfo.totalNetAssets).movePointRight(6).stripTrailingZeros();
			}
		}

		boolean foreginTaxAdjusted = foreignTaxAdjustedSet.contains(stockCode);

		FundInfoJP fund = new FundInfoJP(
			isinCode, fundCode, stockCode, listingDate, redemptionDate, divFreq,
			expenseRatio, buyFreeMax,
			fundType, investingArea, investingAsset, indexFundType, settlementDate,
			totalNetAssets, foreginTaxAdjusted,
			name
			);
		return fund;
	}
	private static DateTimeFormatter ESTABLISHED_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-M-d 00:00:00");
	private static DateTimeFormatter REDEMPTION_DATE_FORMAT  = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final Set<String> foreignTaxAdjustedSet = StorageAnalysis.ForeignTaxAdjusted.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());

}
