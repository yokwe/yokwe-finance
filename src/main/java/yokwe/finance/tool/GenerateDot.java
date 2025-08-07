package yokwe.finance.tool;

import java.io.File;

import yokwe.util.ClassUtil;
import yokwe.util.FileUtil;

public class GenerateDot {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) {
		logger.info("START");
		
		var moduleName = "yokwe.finance";
		var file       = new File("tmp/dot/a.dot");
		
		var module = ClassUtil.findModule(moduleName);
		var string = yokwe.util.makefile.GenerateDot.generate(module);
		
		logger.info("save  {}  {}", string.length(), file.getAbsoluteFile());
		FileUtil.write().file(file, string);
		
		logger.info("STOP");
	}
}
