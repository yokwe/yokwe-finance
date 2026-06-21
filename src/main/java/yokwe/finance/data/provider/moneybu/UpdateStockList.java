package yokwe.finance.data.provider.moneybu;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import yokwe.util.Makefile;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;
import yokwe.util.json.JSON.Ignore;
import yokwe.util.json.JSON.Optional;
import yokwe.util.update.UpdateBase;

public class UpdateStockList extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static Makefile MAKEFILE = Makefile.builder().
		output(StorageMoneybu.StockList).
		build();


	public static void main(String[] args) {
		callUpdate();
	}

	@Override
	public void update() {
		downloadList();
		updateList();
	}

	private void downloadList() {
		var http = HttpUtil.getInstance();
		var uuid = UUID.randomUUID().toString();

		var string0 = http.withPost(getBody(0, uuid), CONTENT_TYPE).downloadString(URL);
		var string1 = http.withPost(getBody(1, uuid), CONTENT_TYPE).downloadString(URL);

		logger.info("string0  {}", string0.length());
		logger.info("string1  {}", string1.length());

		StorageMoneybu.LIST_0_JSON.save(string0);
		StorageMoneybu.LIST_1_JSON.save(string1);
	}
	private static final String CONTENT_TYPE = "application/json;charset=UTF-8";
	private static final String URL = "https://jpx.cloud.qri.jp/tosyo-moneybu/api/result/list";
	private static String getBody(int exType, String uuid) {
		return String.format(
			"""
			{"exType":%d,"category":[],"allFlag":1,"uuid":"%s","filters":[]}"""
			, exType, uuid);
	}

	private void updateList() {
		var string0 = StorageMoneybu.LIST_0_JSON.load();
		var string1 = StorageMoneybu.LIST_1_JSON.load();

		var raw0 = JSON.unmarshal(Raw.class, string0);
		logger.info("raw0  {}", raw0.data.length);
		var raw1 = JSON.unmarshal(Raw.class, string1);
		logger.info("raw1  {}", raw1.data.length);


		var stockList = new ArrayList<StockList>();
		for(var e: raw0.data) {
			stockList.add(toStockList(e));
		}
		for(var e: raw1.data) {
			stockList.add(toStockList(e));
		}

		logger.info("stockList  {}", stockList.size());
		StorageMoneybu.StockList.save(stockList);
	}
	private StockList toStockList(Raw.Data data) {
		StockList ret = new StockList();

		ret.code          = data.stockCode;
		ret.name          = data.stockName;

		ret.exType        = data.exType == 0 ? "ETF" : "ETN";
		ret.managementFee = data.managementFee.scaleByPowerOfTen(-2);
		ret.dividendNum   = data.dividendYield == null ? -1 : data.dividendNum;
		ret.category      = data.categoryName;
		ret.productType   = data.productType == null ? "" : data.productType;
		ret.targetIndex   = data.targetIndex;
		ret.tradeUnit     = data.unit;
		ret.marketMake    = data.marketMake;

		ret.date          = toLocalDate(data.date);
		ret.price         = data.price;
		ret.dividendYield = data.dividendYield == null ? MINUS_ONE : data.dividendYield.scaleByPowerOfTen(-2);
		ret.netAssets     = data.netAssets == null ? MINUS_ONE : data.netAssets;
		ret.y1return      = data.y1return  == null ? MINUS_ONE : data.y1return;
		ret.deviation     = data.deviation == null ? MINUS_ONE : data.deviation.scaleByPowerOfTen(-2);

		return ret;
	}
	static final BigDecimal MINUS_ONE = BigDecimal.ONE.negate();
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


//    "stockCode": "1305",
//    "stockName": "iFreeETF TOPIX（年1回決算型）",
//    "minInvest": 42870,
//    "price": 4287,
//    "date": "2026/06/03",
//    "exType": 0,
//    "icons": {
//    },
//    "managementFee": 0.066,
//    "dividendNum": 1,
//    "dividendYield": 1.64,
//    "categoryName": "国内株ETF",
//    "productType": "TOPIX",
//    "targetIndex": "ＴＯＰＩＸ（配当込み）",
//    "netAssets": 14512167025398,
//    "favoriteCount": 509,
//    "isFavorite": 0,
//    "managementCompany": {
//    },
//    "y1return": 0.442705704189803,
//    "deviation": 0.08,
//    "unit": 10,
//    "marketMake": 1

    public static class Raw {
    	public static class Data {
     		public String     stockCode;
     		public String     stockName;
     		public BigDecimal minInvest;
     		public BigDecimal price;
     		public String     date;
     		public int        exType;        // 0 => ETF  1 => ETN
     		@Ignore
     		public String     icons;
     		public BigDecimal managementFee; // 信託報酬 unit is percent

     		@Optional
     		public int        dividendNum;
     		@Optional
     		public BigDecimal dividendYield; // 分配金利回り unit is percent

     		public String     categoryName;      // 国内株ETF

     		@Optional
     		public String     productType;   // TOPIX

     		public String     targetIndex;   // ＴＯＰＩＸ（配当込み）

     		@Optional
     		public BigDecimal netAssets;     // 14,512,167,025,398  145,121.7億円

     		@Ignore
     		public int        favoriteCount;
     		@Ignore
     		public int        isFavorite;
     		@Ignore
     		public String     managementCompany;

     		@Optional
     		public BigDecimal y1return;

     		@Optional
     		public BigDecimal deviation;     // 乖離率

     		public int        unit;          // 売買単位
     		public int        marketMake;    // 0 => NO  1 => YES

     		@Override
     		public String toString() {
     		    return ToString.withFieldName(this);
     		}
    	}

    	public String status; // 0 => OK
    	public Data[] data;

 		@Override
 		public String toString() {
 		    return ToString.withFieldName(this);
 		}
    }



}
