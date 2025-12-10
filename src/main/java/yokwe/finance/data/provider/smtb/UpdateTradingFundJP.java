package yokwe.finance.data.provider.smtb;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.data.fund.jp.StorageJP;
import yokwe.finance.data.provider.smtb.WebPage.PageInfo;
import yokwe.finance.data.type.FundInfoJP;
import yokwe.finance.data.type.TradingFund;
import yokwe.util.FileUtil;
import yokwe.util.Makefile;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.update.UpdateBase;

public class UpdateTradingFundJP extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
//		input(StorageJITA.FundInfo).
		output(StorageSMTB.TradingFundJP).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		downloadFile();
		var list = new ArrayList<TradingFund>();
		buildList(list);
		save(list, StorageSMTB.TradingFundJP); // use save for make
	}
	
	void downloadFile() {
		// delete all file in WebPage
		StorageSMTB.WebPage.deleteFiles(o -> true);
		
		int pageNoMax;
		{
			int pageNo = 0;
			logger.info("pageNo  {}", pageNo);
			downloadPage(pageNo);
			var page = StorageSMTB.WebPage.load(getName(pageNo));
			var pageInfo = PageInfo.getInstance(page);
			logger.info("pageInfo  {}", pageInfo);
			
			pageNoMax = pageInfo.hitcount / pageInfo.maxdisp;
			logger.info("pageNoMax  {}", pageNoMax);
		}
		
		for(int pageNo = 1; pageNo <= pageNoMax; pageNo++) {
			logger.info("pageNo  {}", pageNo);
			downloadPage(pageNo);
		}
	}
	
	String getName(int pageNo) {
		return String.format("%04d", pageNo);
	}

	void downloadPage(int pageNo) {
		var url  = getURL(pageNo);
		var value = HttpUtil.getInstance().downloadString(url);
		
		value = value.replace("\r", "\n");
		value = value.replace("\n\n", "\n");
		
		StorageSMTB.WebPage.save(getName(pageNo), value);
	}

	private static final int MAXDISP = 100;
	private static String getURL(int pageNo) { // pageNo starts from zero
		// https://fund.smtb.jp/smtbhp/qsearch.exe?F=openlist&type=result&MAXDISP=100&TAB=t&BEFORE=0&GO_BEFORE=
		// https://fund.smtb.jp/smtbhp/qsearch.exe?F=openlist&type=result&MAXDISP=100&TAB=t&BEFORE=100&GO_BEFORE=
		int before = MAXDISP * pageNo;
		return String.format("https://fund.smtb.jp/smtbhp/qsearch.exe?F=openlist&type=result&MAXDISP=%d&TAB=t&BEFORE=%d&GO_BEFORE=", MAXDISP, before);
	}
	
	void buildList(ArrayList<TradingFund> list) {
		int countA = 0;
		int countB = 0;
		int countC = 0;
		int countD = 0;
		var fundInfoList = StorageJP.FundInfo.getList();
		var fundCodeMap  = fundInfoList.stream().collect(Collectors.toMap(o -> o.fundCode, Function.identity()));
		
		for(var file: StorageSMTB.WebPage.getDir().listFiles((d, n) -> n.endsWith(".html"))) {
			var page = FileUtil.read().file(file);
			
			var fundList = WebPage.FundInfo.getInstance(page);			
			for(var e: fundList) {
				var fundCode = e.fundCode;
				var fundName = normalizeString(e.fundName);
				if (e.initialFee.isEmpty()) {
//					logger.info("skip  {}  {}", fundCode, fundName);
					countA++;
					continue;
				}
				var salesFee = new BigDecimal(e.initialFee);
				
//				logger.info("fund  {}  {}", fundCode, fundName);
				FundInfoJP fundInfo = null;
				if (fundCodeMap.containsKey(fundCode)) {
					fundInfo = fundCodeMap.get(fundCode);
					countB++;
				} else {
					for(var ee: fundInfoList) {
						if (normalizeString(ee.name).equals(fundName)) {
							fundInfo = ee;
						}
					}
					if (fundInfo != null) {
						countC++;
					} else {
						logger.error("Unexpected fund");
						logger.error("  fund  {}", e);
						logger.error("        {}!", normalizeString(e.fundName));
						countD++;
						throw new UnexpectedException("Unexpected fund");						
					}
				}
				list.add(new TradingFund(fundInfo.isinCode, salesFee, fundInfo.name));
			}
		}
		logger.info("countA  {}", countA);
		logger.info("countB  {}", countB);
		logger.info("countC  {}", countC);
		logger.info("countD  {}", countD);
	}
		
	private static String normalizeString(String string) {
		int length = string.length();
		var ret = new StringBuilder(length);
		for(int i = 0; i < length; i++) {
			char c = string.charAt(i);
			var nomalized = NORMALIZE_CHAR_MAP.get(c);
			if (nomalized == null) {
				ret.append(c);
			} else {
				ret.append(nomalized);
			}
		}
		return ret.toString();
	}
	private static final Map<Character, String> NORMALIZE_CHAR_MAP;
	static {
		NORMALIZE_CHAR_MAP = new HashMap<>();
		NORMALIZE_CHAR_MAP.put(Character.valueOf('-'),   "ー");
		NORMALIZE_CHAR_MAP.put(Character.valueOf(' '),   "");
		NORMALIZE_CHAR_MAP.put(Character.valueOf('　'),  "");
		NORMALIZE_CHAR_MAP.put(Character.valueOf('－'),  "ー");
		// 新光ＵＳ－ＲＥＩＴオープン
		// 新光ＵＳーＲＥＩＴオープン
		{
			char h = 'A';
			char f = 'Ａ';
			for(int i = 0; i < 26; i++) {
				NORMALIZE_CHAR_MAP.put(Character.valueOf(h++), Character.toString(f++));
			}
		}
		{
			char h = '0';
			char f = '０';
			for(int i = 0; i < 10; i++) {
				NORMALIZE_CHAR_MAP.put(Character.valueOf(h++), Character.toString(f++));
			}
		}
	}
}
