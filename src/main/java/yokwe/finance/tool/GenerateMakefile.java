package yokwe.finance.tool;

import static yokwe.finance.tool.Utils.toAntTarget;
import static yokwe.finance.tool.Utils.toMakeGroup;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.TreeSet;
import java.util.stream.Collectors;

import yokwe.util.ClassUtil;
import yokwe.util.FileUtil;

public class GenerateMakefile {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) {
		logger.info("START");
		
		var moduleName = "yokwe.finance";
		var file       = new File("tmp/update-all.make");
		
		logger.info("moduleName  {}", moduleName);
		var module = ClassUtil.findModule(moduleName);
		var string = generate(module);
		
		logger.info("save  {}  {}", string.length(), file.getAbsoluteFile());
		FileUtil.write().file(file, string);
		
		logger.info("STOP");
	}
	
	public static String generate(Module module) {
		var makefileList = Makefile.scanModule(module);

		var groupNameSet   = makefileList.stream().map(o -> toMakeGroup(o)).collect(Collectors.toCollection(TreeSet::new));
		var groupUpdateSet = groupNameSet.stream().map(o -> "udate-" + o).collect(Collectors.toCollection(TreeSet::new));
		
		var sw = new StringWriter();		
		try (var out = new PrintWriter(sw)) {
			out.println("#");
			out.println("# module " + module.getDescriptor().toNameAndVersion());
			out.println("#");
			out.println();
			out.println(".PHONY: update-all " + String.join(" ", groupUpdateSet));
			out.println();
			out.println("#");
			out.println("# update-all");
			out.println("#");
			out.println("update-all: \\");
			out.println("\t" + String.join(" \\\n\t", groupUpdateSet));
			out.println();
			out.println();
			
			for(var group: groupNameSet) {
				var makeList = makefileList.stream().filter(o -> toMakeGroup(o).equals(group)).toList();
				var outFileSet = new TreeSet<String>();
				for(var e: makeList) {
					e.outputList.stream().forEach(o -> outFileSet.add(o.getAbsolutePath()));
				}
				
				out.println("#");
				out.println("# " + group);
				out.println("#");
				out.println("update-" + group + ": \\");
				out.println("\t" + String.join(" \\\n\t", outFileSet));
				out.println();
				
				for(var e: makeList) {
					var iList = e.inputList.stream().map(o -> o.getAbsolutePath()).toList();
					var oList = e.outputList.stream().map(o -> o.getAbsolutePath()).toList();
					
					if (iList.isEmpty()) {
						out.println(String.join(" ", oList) + ":");
					} else {
						out.println(String.join(" ", oList) + ": \\");
						out.println("\t" + String.join(" \\\n\t", iList));
					}
					out.println("\tant " + toAntTarget(e));
				}
				
				out.println();
				out.println();
			}
		}
		
		return sw.toString();
	}
}
