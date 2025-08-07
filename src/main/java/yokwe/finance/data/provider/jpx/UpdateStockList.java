package yokwe.finance.data.provider.jpx;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import yokwe.finance.data.type.CodeName;
import yokwe.finance.data.type.StockCodeJP;
import yokwe.util.FileUtil;
import yokwe.util.StringUtil;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;
import yokwe.util.makefile.Makefile;
import yokwe.util.update.UpdateBase;

public class UpdateStockList extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input().
		output(StorageJPX.StockList).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	public static List<CodeName> getCodeNameList() {
		var list = new ArrayList<CodeName>();
		
		for(var file: getJSONFileList()) {
			var string = FileUtil.read().file(file);
			var result = JSON.unmarshal(StockList.class, string);
			for(var e: result.section1.data) {
				if (e.LISS.equals("TPM")) continue;
				var code = StockCodeJP.toStockCode5(e.BICD);
				var name = StringUtil.toFullWidth(e.FLLN).replace("　　", "　");
				list.add(new CodeName(code, name));
			}
		}
		
		return list;
	}
	public static List<File> getJSONFileList() {
		var array = StorageJPX.StockListJSON.getDir().listFiles(o -> o.isFile() && o.getName().endsWith(".json"));
		var list  = new ArrayList<File>(Arrays.asList(array));
		Collections.sort(list);
		return list;
	}

	
	@Override
	public void update() {
		// delete files in StorageJPX.StockListJSON
		FileUtil.deleteFile(getJSONFileList());
		
		int pagecount = 99;
		int count = 0;
		for(var page = 1; page <= pagecount; page++) {
			if ((++count % 10) == 1) logger.info("{}  /  {}", page, pagecount);

			var string = downloadFile(page);
			// save
			StorageJPX.StockListJSON.save(String.format("%03d", page), string);
			
			if (page == 1) {
				// set pagecount from page content
				var result = JSON.unmarshal(StockList.class, string);
				pagecount = result.section1.pagecount;
				logger.info("pagecount  {}", pagecount);
			}
		}
		// touch file
		StorageJPX.StockListJSON.touch();
		
		var list = getCodeNameList();
		save(list, StorageJPX.StockList); // use save for make

	}
	private String downloadFile(int page) {
		var urlFormat = "https://quote.jpx.co.jp/jpxhp/jcgi/wrap/qjsonp.aspx?F=ctl/stock_list&page=%d&refindex=%%2BTTCODE&maxdisp=100";
		var referer   = "https://quote.jpx.co.jp/jpxhp/main/index.aspx?f=stock_list&key7=";
		var url       = String.format(urlFormat, page);
		
		return HttpUtil.getInstance().withReferer(referer).downloadString(url);
	}	
}
