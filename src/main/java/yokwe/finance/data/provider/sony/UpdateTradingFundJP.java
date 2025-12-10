package yokwe.finance.data.provider.sony;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.jita.StorageJITA;
import yokwe.finance.data.type.TradingFund;
import yokwe.util.FileUtil;
import yokwe.util.Makefile;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;
import yokwe.util.update.UpdateBase;

public class UpdateTradingFundJP extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input(StorageJITA.FundInfo).
		output(StorageSony.TradingFundJP).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		downloadFile();
		
		var list = new ArrayList<TradingFund>();
		buildList(list);
		save(list, StorageSony.TradingFundJP); // use save for make
	}
	
	private String getURL() {
		String queryString;
		{
			var currentTimeMillis = System.currentTimeMillis();
	        // valid values
	        // 02YYYYMMDD0----00000
	        // 02202512050269000000
	        
	        // 02202512060269500000
	        // 02202512060269600000
	        // 02202512060269700000
	        
	        // 02202512070269800000
	        // 02202512070269900000
	        // 02202512070270000000
	        
	        // 02202512080270100000
	        // 02202512080270200000
	        // 02202512080270300000
	        // 02202512080270400000
	        // 
	        // 02202512090270500000
	        // 02202512090270600000
	        // 02202512090270700000
	        // 02202512090270800000
	        
	        // 02202512090270834223
	        // 02202512090270834494
	        // 02202512090270834645
	        
	        // 
	        var com_id_session = "02202512080270100000";
			
			var map = new LinkedHashMap<String, String>();
			// _com_id_product=1&
			// _com_id_company=C160035&
			// _com_id_screen=10107101&
			// _biz_id_abiccode=&
			// id_companyapp=003&
			// _com_id_session=02202512050269110315&
			// d_keyword=&
			// id_fundlist=&
			// fg_stop=&
			// id_companylclass=&
			// callback=callback_ca_fndlst&
			map.put("_com_id_product", "1");
			map.put("_com_id_company", "C160035");
			map.put("_com_id_screen", "10107101");
			map.put("_biz_id_abiccode", "");
			map.put("id_companyapp", "003");
			map.put("_com_id_session", com_id_session);
			map.put("id_keyword", "");
			map.put("id_fundlist", "");
			map.put("fg_stop", "");
			map.put("id_companylclass", "");
			map.put("callback", "callback_ca_fndlst");
			map.put("_" + Long.toString(currentTimeMillis), "");
			queryString = map.entrySet().stream().map(o -> o.getKey() + "=" + o.getValue()).collect(Collectors.joining("&"));
		}
		
		var url = "https://www.wam.abic.co.jp/ap03/services/cafndlst/getCAFndLst";
		return url + "?" + queryString;
	}
	private void downloadFile() {
		var url = getURL();
		logger.info("url {}", url);
		var string = HttpUtil.getInstance().downloadString(url);
		string = string.replace("callback_ca_fndlst(", "").replace("})", "}");
//		logger.info("string  {}", string);
		var file = StorageSony.FundListJSON.getFile();
		logger.info("file  {}  {}", string.length(), file.toPath());
		FileUtil.write().file(file, string);
	}
	
	private void buildList(List<TradingFund> list) {
		var file = StorageSony.FundListJSON.getFile();
		var string = FileUtil.read().file(file);
		
		var data = JSON.unmarshal(FundList.Data.class, string);
		if (data == null) {
			logger.error("JSON unmarshal failed");
			logger.error("  string {}", string);
			throw new UnexpectedException("JSON unmarshal failed");
		}
		
		var map = StorageJITA.FundInfo.getList().stream().collect(Collectors.toMap(o -> o.fundCode, Function.identity()));
		
		logger.info("ns_favourregisterfund  {}", data.ns_favourregisterfund.length);
		
		int countA = 0;
		int countB = 0;
		int countC = 0;
		int countD = 0;
		for(var e: data.ns_favourregisterfund) {
			if (e.st_currencyabbr.equals("米ドル")) {
				countA++;
				continue;
			}
			
			// FIXME how to convert abic fund code to ISIN
			var fundCode = e.id_abicfund.substring(1);
			if (map.containsKey(fundCode)) {
				var fundInfo = map.get(fundCode);
				var tradingFund = new TradingFund(fundInfo.isinCode, BigDecimal.ZERO, fundInfo.name);
				list.add(tradingFund);
				countB++;
			} else {
				var name = e.st_fundshortname1;
				// remove ＜愛称：
				if (name.contains("＜愛称：")) {
					name = name.substring(0, name.indexOf("＜愛称："));
				}
				// remove kanji space
				if (name.contains("　")) {
					name = name.replace("　", "");
				}
				boolean found = false;
				for(var fundInfo: map.values()) {
					// remove kanji space
					var fundName = fundInfo.name.replace("　", "");
					if (fundName.equals(name)) {
						var tradingFund = new TradingFund(fundInfo.isinCode, BigDecimal.ZERO, fundInfo.name);
						list.add(tradingFund);
						found = true;
					}
				}
				if (found) {
					countC++;
				} else {
					logger.info("XX  {}  {}  {}  {}", e.id_abicfund, e.st_itcompany, e.st_currencyabbr, e.st_fundshortname1);
					countD++;
				}
			}
		}
		logger.info("countA  {}", countA);
		logger.info("countB  {}", countB);
		logger.info("countC  {}", countC);
		logger.info("countD  {}", countD);
	}

}
