package yokwe.finance.tool;

import static yokwe.finance.tool.Utils.toAntTarget;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import yokwe.util.ClassUtil;
import yokwe.util.ColorPalette;
import yokwe.util.FileUtil;
import yokwe.util.Makefile;
import yokwe.util.Storage;
import yokwe.util.graphviz.Dot;

public class GenerateDot {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) {
		logger.info("START");
		
		var moduleName = "yokwe.finance";
		var file       = new File("tmp/dot/a.dot");
		
		var module = ClassUtil.findModule(moduleName);
		var string = generate(module);
		
		logger.info("save  {}  {}", string.length(), file.getAbsoluteFile());
		FileUtil.write().file(file, string);
		
		logger.info("STOP");
	}
	
	public static String generate(Module... modules) {
		Arrays.stream(modules).toList();
		return generate(Arrays.stream(modules).toList());
	}
	public static String generate(List<Module> moduleList) {
		var rootPath = Storage.storage.getFile().getAbsolutePath() + "/";
		logger.info("rootPath  {}", rootPath);
		
		Map<String, List<Makefile>> makefileMap;
		{
			var makefileList = new ArrayList<Makefile>();
			for(var module: moduleList) {
				logger.info("moduleName  {}", module.getDescriptor().toNameAndVersion());
				makefileList.addAll(Makefile.scanModule(module));
			}
			logger.info("makeList  {}", makefileList.size());
			
			makefileMap = makefileList.stream().collect(Collectors.groupingBy(o -> o.clazz.getPackageName(), TreeMap::new, Collectors.toCollection(ArrayList::new)));
			logger.info("makeMap   {}", makefileMap.size());
		}
		
		var paletteMap = new TreeMap<String, String>();
		//                           group   rgb
		{
			var orignalPalette = ColorPalette.SET3_N12;
			var groupList = new ArrayList<String>(makefileMap.keySet());
			var palette = orignalPalette.interpolate(groupList.size());
			for(int i = 0; i < groupList.size(); i++) {
				var group = groupList.get(i);
				var rgb   = palette.toString(i);
				paletteMap.put(group, rgb);
			}
		}
				
		var g = new Dot.Digraph("G");
		g.attr("ranksep", "1").attr("nodesep", "0.5");
		g.attr("rankdir", "LR");
//		g.attr("splines", "spline");
//		g.attr("splines", "polyline");
		g.attr("fontname", "Migu 1M");
		
		g.nodeAttr().attr("fontname", "Migu 1M");
		g.nodeAttr().attr("style", "filled");
		
		
		int countAntTask = 0;
		int countFile    = 0;
		int countEdge    = 0;
		// output ant task
		for(var group: makefileMap.keySet()) {
			for(var makefile: makefileMap.get(group)) {
				countAntTask++;
				var antTarget = toAntTarget(makefile);
				var name      = makefile.clazz.getSimpleName();
				var color     = paletteMap.get(group);
				g.node(antTarget).attr("label", group + "\\n" + name).attr("shape",  "box").attr("fillcolor", color).attr("peripheries", makefile.inputList.size() == 0 ? "2" : "1");
			}
		}
		
		// output file
		for(var group: makefileMap.keySet()) {
			for(var makefile: makefileMap.get(group)) {
				var color = paletteMap.get(group);
				
				for(var e: makefile.outputList) {
					countFile++;
					var path     = e.getPath().replace(rootPath, "");
					var slashPos = path.lastIndexOf('/');
					var dir      = path.substring(0, slashPos);
					var name     = path.substring(slashPos + 1);
					
					g.node(path).attr("label", dir + "\\n" + name).attr("shape", "oval").attr("fillcolor", color);
				}
			}
		}
		
		// connect file and task
		for(var group: makefileMap.keySet()) {
			for(var makefile: makefileMap.get(group)) {
				var antTarget = toAntTarget(makefile);
				for(var e: makefile.inputList) {
					countEdge++;
					var path = e.getPath().replace(rootPath, "");
					g.edge(path, antTarget);
				}
				for(var e: makefile.outputList) {
					countEdge++;
					var path = e.getPath().replace(rootPath, "");
					g.edge(antTarget, path).attr("style", "bold"); // make bold for output file
				}
			}
		}
		
		logger.info("countAntTask  {}", countAntTask);
		logger.info("countFile     {}", countFile);
		logger.info("countEdge     {}", countEdge);
		
		var string = g.toString();
		return string;
	}
}
