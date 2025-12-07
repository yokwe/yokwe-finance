package yokwe.finance.tool;

import yokwe.util.Storage;

public class T001 {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) {
		logger.info("START");
		
		logger.info("storage.file  {}", Storage.storage.getFile().getPath());
		
		logger.info("STOP");
	}
}
