package yokwe.finance.data.provider.nikko;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.data.fund.jp.StorageJP;
import yokwe.finance.data.type.TradingFund;
import yokwe.util.CSVUtil;
import yokwe.util.Makefile;
import yokwe.util.ToString;
import yokwe.util.http.HttpUtil;
import yokwe.util.update.UpdateBase;

public class UpdateTradingFundJP extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input(). // StorageJITA.FundInfo
		output(StorageNikko.TradingFundJP).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		downloadFile();
		updateFile();
	}
	
	static final String CHARSET = "UTF-8";

	
	void downloadFile() {
		String url     = "https://www.smbcnikko.co.jp/products/inv/direct_fee/csv/coursedata.csv";
		
		var string = HttpUtil.getInstance().withCharset(CHARSET).downloadString(url);
		StorageNikko.CourceData.save(string);
		logger.info("courdata.csv  {}", string.length());
	}
	
	void updateFile() {
		var noLoad = "ノーロード";
		var fundCodeMap = StorageJP.FundInfo.getList().stream().collect(Collectors.toMap(o -> o.fundCode, Function.identity()));
		
		var pat     = Pattern.compile("(?<percent>[0-9]+\\.[0-9]+)％");
		var patFund = Pattern.compile("銘柄コード：(?<nikkoCode>.{4})　投信協会コード：(?<fundCode>.{8})");

		var dataList = CSVUtil.read(CourseData.class).withHeader(false).withSeparator('|').file(new StringReader(StorageNikko.CourceData.load()));
		logger.info("dataList  {}", dataList.size());
		
		int countA = 0;
		int countB = 0;
		int countC = 0;
		int countD = 0;
		int countE = 0;
		int countF = 0;
		int countG = 0;
		
		var list = new ArrayList<TradingFund>();
		for(var data: dataList) {
			String     isinCode;
			BigDecimal salesFee;
			String     fundName;

			if (fundCodeMap.containsKey(data.fundCode)) {
				var fundInfo = fundCodeMap.get(data.fundCode);
				isinCode = fundInfo.isinCode;
				fundName = fundInfo.name;
				countA++;
			} else {
				// find correct fundCode from fund page
				String url = "https://fund2.smbcnikko.co.jp/smbc_nikko_fund/qsearch.exe?F=detail_kokunai1&KEY1=" + data.fundCode;
				var string = HttpUtil.getInstance().withCharset(CHARSET).downloadString(url);
				Matcher m = patFund.matcher(string);
				if (m.find()) {
					String newFundCode  = m.group("fundCode");
					
					if (fundCodeMap.containsKey(newFundCode)) {
						var fundInfo = fundCodeMap.get(newFundCode);
						isinCode = fundInfo.isinCode;
						fundName = fundInfo.name;
						countB++;
					} else {
						logger.info("bogus new fundCode  {}  {}  {}  {}", newFundCode, data.fundCode, data.nikkoCode, data.name);
						countC++;
						continue;
					}
				} else {
					logger.info("no fund page {}  {}  {}", data.fundCode, data.nikkoCode, data.name);
					countD++;
					continue;
				}
			}
			
			if (data.salesFee.startsWith(noLoad)) {
				salesFee = BigDecimal.ZERO;
				countE++;
			} else {
				Matcher m = pat.matcher(data.salesFee);
				if (m.find()) {
					String string = m.group("percent");
					salesFee = new BigDecimal(string).movePointLeft(2); // change to percent
					countF++;
				} else {
					salesFee = TradingFund.SALES_FEE_UNKNOWN;
					logger.info("bogus salesFee  {}  {}  {}  !{}!", data.fundCode, data.nikkoCode, data.salesFee);
					countG++;
				}
			}
			list.add(new TradingFund(isinCode, salesFee, fundName));
		}
		
		logger.info("countA  {}", countA);
		logger.info("countB  {}", countB);
		logger.info("countC  {}", countC);
		logger.info("countD  {}", countD);
		logger.info("countE  {}", countE);
		logger.info("countF  {}", countF);
		logger.info("countG  {}", countG);
		
		save(list, StorageNikko.TradingFundJP); // use save for make
	}
	
	public static class CourseData {
		public String nikkoCode;
		public String name;
		public String company;
		public String type;
		public String salesFee;
		public String fundCode; // Do not use this fundCode.
		public String flag;
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
}