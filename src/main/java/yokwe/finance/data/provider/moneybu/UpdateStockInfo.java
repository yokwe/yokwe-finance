package yokwe.finance.data.provider.moneybu;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.jpx.StorageJPX;
import yokwe.finance.data.type.StockCodeJP;
import yokwe.util.Makefile;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;
import yokwe.util.json.JSON.Ignore;
import yokwe.util.json.JSON.Optional;
import yokwe.util.update.UpdateBase;

public class UpdateStockInfo extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static Makefile MAKEFILE = Makefile.builder().
		input(StorageMoneybu.StockList, StorageJPX.StockCodeName).
		output(StorageMoneybu.StockInfo).
		build();


	public static void main(String[] args) {
		callUpdate();
	}

	@Override
	public void update() {
		var list = StorageMoneybu.StockList.getList();
		download(list);
		update(list);
	}

	private void download(List<StockList> stockList) {
		logger.info("download  {}", stockList.size());

		var http = HttpUtil.getInstance();
		var uuid = UUID.randomUUID().toString();

		var list = stockList.stream().filter(o -> needsUpdate(StorageMoneybu.StockInfoJSON.getFile(o.code))).collect(Collectors.toList());
		//
		Collections.shuffle(list);
		int count = 0;
		for(var e: list) {
			if ((count++ % 40) == 0) {
				logger.info("{}", count - 1);
			} else {
//				logger.info("{}", e.code);
			}

			var string = http.withPost(getBody(uuid, e.code), CONTENT_TYPE).downloadString(URL);
			StorageMoneybu.StockInfoJSON.save(e.code, string);
		}

	}
	private static final String CONTENT_TYPE = "application/json;charset=UTF-8";
	private static final String URL = "https://jpx.cloud.qri.jp/tosyo-moneybu/api/detail/info";
	private static String getBody(String uuid, String stockCode) {
		// {"uuid":"b6dc8a98-0c64-467f-8458-0167614b4520","stockCode":"1655"}
		return String.format(
			"""
			{"uuid":"%s","stockCode":"%s"}"""
			, uuid, stockCode);
	}


	private void update(List<StockList> stockList) {
		//
		var stockInfoList = new ArrayList<StockInfo>(stockList.size());

		var stockMap = StorageJPX.StockCodeName.getList().stream().collect(Collectors.toMap(o->o.stockCode, Function.identity()));
		var today    = LocalDate.now();

		logger.info("update  {}", stockList.size());
		int count = 0;
		for(var e: stockList) {
			if ((count++ % 40) == 0) {
				logger.info("{}", count - 1);
			} else {
//				logger.info("{}", count - 1);
			}

			var string = StorageMoneybu.StockInfoJSON.load(e.code);
			var raw = JSON.unmarshal(Raw.class, string);

			var stockCode = StockCodeJP.toStockCode5(e.code);
			if (stockMap.containsKey(stockCode)) {
				var stock = stockMap.get(stockCode);

				var duration = durationInMonth(toLocalDate(raw.data.listingDate), today);

				StockInfo stockInfo = new StockInfo();

				stockInfo.stockCode = stock.stockCode;
				stockInfo.isinCode  = stock.isinCode;
				stockInfo.name      = stock.name;
				stockInfo.duration  = duration;
				stockInfo.divYield  = raw.data.dividendYield == null ? StockInfo.UNKNOWN_DIV_YIELD : raw.data.dividendYield.scaleByPowerOfTen(-2);

				if (raw.data.dividendHist != null && raw.data.dividendHist.length != 0) {
					// try find non zero dividend
					for(var ee: raw.data.dividendHist) {
						stockInfo.lastDivDate  = toLocalDate(ee.date);
						stockInfo.lastDivValue = ee.dividend;

						if (stockInfo.lastDivValue.signum() != 0) {
							break;
						}
					}
				} else {
					stockInfo.lastDivDate  = StockInfo.UNKNOWN_DIV_DATE;
					stockInfo.lastDivValue = 12 < duration ? BigDecimal.ZERO : StockInfo.UNKNOWN_DIV_VALUE;
				}

				stockInfoList.add(stockInfo);
			} else {
				// delisted in jpx but not delisted in moneybu
				logger.warn("Unexpected code  {}  {}", e.code, e.name);
			}
		}

		logger.info("stockInfoList  {}", stockInfoList.size());
		StorageMoneybu.StockInfo.save(stockInfoList);
	}


	private LocalDate toLocalDate(String string) {
		// 2026/06/03
		// 0123456789
		if (string.length() == 10 && string.charAt(4) == '/' && string.charAt(7) == '/') {
			var yyyy = string.substring(0, 4);
			var mm   = string.substring(5, 7);
			var dd   = string.substring(8);
			return LocalDate.of(Integer.parseInt(yyyy), Integer.parseInt(mm), Integer.parseInt(dd));
		} else {
			logger.error("Unexpected string");
			logger.error("  string  {}!", string);
			throw new UnexpectedException("Unexpected string");
		}
	}
	private int durationInMonth(LocalDate startDate, LocalDate endDate) {
		// startDate and endDate is inclusive
		if (startDate.isAfter(endDate)) {
			return 0;
		} else {
			LocalDate endDatePlusOne = endDate.plusDays(1);
			Period    period         = startDate.until(endDatePlusOne);
			return period.getYears() * 12 + period.getMonths();
		}
	}




    public static class Raw {
    	public static class Icons {
    		// value must be 0 or 1
    	      public int genbutsu;
    	      public int foreign;
    	      public int otcswap;
    	      public int linked;
    	      public int etn;
    	      public int lev;
    	      public int inv;
    	      public int jdr;
    	      public int longterm;
    	      public int financialfuture;
    	      public int activefund;
    	      public int newNISA;

       		@Override
       		public String toString() {
       		    return ToString.withFieldName(this);
       		}
    	}
    	public static class Dividend {
    		public String     date;
    		public BigDecimal dividend;

       		@Override
       		public String toString() {
       		    return ToString.withFieldName(this);
       		}
    	}
    	public static class Data {
     		public String     stockCode;
     		public String     stockName;
     		public BigDecimal minInvest;
     		public BigDecimal price;
     		public String     nav;
     		public boolean    hasNav;
     		@Optional
     		public BigDecimal deviation;     // 乖離率
     		public String     date;          // 2026/06/15
     		public String     priceDate;     // 2026/06/15
     		public String     nriDate;       // 2026/06/15
     		@Optional
     		public String     iNavDate;      // null
     		public String     tvDate;        // 2026/06/15

     		public Icons      icons;
     		public BigDecimal managementFee; // 信託報酬 unit is percent
     		public BigDecimal netAssets;     // 15298171576340
     		public String     netAssetsDate; // 2026/06/15

     		public BigDecimal dividend;      // 70.6
     		public String     dividendDate;  // （年1回）
     		public BigDecimal dividendYield; // 1.64  分配金利回り unit is percent

     		@Optional
     		public BigDecimal shintakuRyuhogaku;  // null or 0.1
     		public BigDecimal managementFeeTotal; // 0.09
     		public int        exType;             // 0

     		public int        categoryCode;       // 0
     		public String     categoryName;       // 国内株ETF

     		@Optional
     		public BigDecimal productCode;        // 1
     		public String     productType;        // TOPIX
     		public String     targetIndex;        // ＴＯＰＩＸ（配当込み）

     		@Ignore
     		public String     managementCompany;

     		public BigDecimal rightUnit;  // 3606678090
     		public String     sharesDate; // 2026/05/29
     		public int        unit;       // 売買単位

     		public BigDecimal volume;       // 259960
     		public BigDecimal tradingValue; // 1117324870

     		public BigDecimal quarterTradingVolume; // 217718.13559322033
     		public BigDecimal quarterTradingValue;  // 885191602.2033899

     		public int        favoriteCount;  // 511
     		public int        isFavorite;     // 0
     		public int        inav;           // 1

     		public String     feature;
     		public String     underlierOutline;

     		@Ignore
     		public String     disclaimer;

     		public int        liquidity; // 0
     		public int        reserve;   // 0

     		@Optional
     		public BigDecimal otherExpense; // null
     		@Optional
     		public String     notice; // null

     		@Ignore
     		public String     pcfFundDate;
     		@Ignore
     		public String     pcfDataDate;
     		@Ignore
     		public String     pcfWeight;

     		public int        marketMake;    // 0 => NO  1 => YES

     		public BigDecimal spread;     // 6.6
     		public String     spreadDate; // 2026/05/29

     		public BigDecimal depth;      // 65330200　売り気配・買い気配の数量
     		public String     depthDate;  // 2026/05/29

     		public Dividend[] dividendHist;

     		public String     listingDate; // 001/07/13
     		@Ignore
     		public String     yokogaoLink;

     		@Override
     		public String toString() {
     		    return ToString.withFieldName(this);
     		}
    	}

    	public String status; // 0 => OK
    	public Data   data;

 		@Override
 		public String toString() {
 		    return ToString.withFieldName(this);
 		}
    }

}
