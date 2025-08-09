package yokwe.finance.tool;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import yokwe.util.ClassUtil;
import yokwe.util.StackWalkerUtil;
import yokwe.util.Storage;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;

public class Makefile implements Comparable<Makefile> {
	static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static class Builder {
		private Class<?>   clazz;
		private List<File> inputList  = new ArrayList<>();
		private List<File> outputList = new ArrayList<>();
		
		private Builder(Class<?> clazz) {
			this.clazz = clazz;
		}
		
		public Builder input(Storage.LoadSave... newValues) {
			for(var newValue: newValues) {
				if (newValue instanceof Storage.LoadSaveFileGeneric) {
					var loadSave = (Storage.LoadSaveFileGeneric<?>)newValue;
					inputList.add(loadSave.getFile());
				} else if (newValue instanceof Storage.LoadSaveDirectoryGeneric) {
					var loadSave = (Storage.LoadSaveDirectoryGeneric<?>)newValue;
					inputList.add(loadSave.getTouchFile());
				} else {
					logger.error("Unexpected newValues");
					logger.error("  newValues  {}", Arrays.stream(newValues).toList());
					throw new UnexpectedException("Unexpected newValues");
				}
			}
			return this;
		}
		public Builder output(Storage.LoadSave... newValues) {
			for(var newValue: newValues) {
				if (newValue instanceof Storage.LoadSaveFileGeneric) {
					var loadSave = (Storage.LoadSaveFileGeneric<?>)newValue;
					outputList.add(loadSave.getFile());
				} else if (newValue instanceof Storage.LoadSaveDirectoryGeneric) {
					var loadSave = (Storage.LoadSaveDirectoryGeneric<?>)newValue;
					outputList.add(loadSave.getTouchFile());
				} else {
					logger.error("Unexpected newValues");
					logger.error("  newValues  {}", Arrays.stream(newValues).toList());
					throw new UnexpectedException("Unexpected newValues");
				}
			}
			return this;
		}
		public Makefile build() {
			return new Makefile(clazz, inputList, outputList);
		}
	}
	
	public static Builder builder() {
		var callerClass = StackWalkerUtil.getCallerStackFrame(StackWalkerUtil.OFFSET_CALLER).getDeclaringClass();
		return new Builder(callerClass);
	}
	
	public final Class<?>   clazz;
	public final List<File> inputList;
	public final List<File> outputList;
	public final String     antTarget;
	public final String     makeGroup;
	
	private Makefile(Class<?> clazz, List<File> inputs, List<File> outputs) {
		this.clazz      = clazz;
		this.inputList  = inputs;
		this.outputList = outputs;
		this.antTarget  = toAntTarget(clazz);
		this.makeGroup  = toMakeGroup(clazz);
	}
	private String toAntTarget(Class<?> clazz) {
		var names = clazz.getTypeName().toLowerCase().split("\\.");
		var name3 = names[names.length - 3];
		var name2 = names[names.length - 2];
		var name1 = names[names.length - 1];
		
		var buffer = new StringBuilder((name2.equals("jp") || name2.equals("us")) ? (name3 + "-" + name2) : name2);
		var string = name1;
		
		for(;;) {
			boolean modified = false;
			for(var token: tokens) {
				if (!string.startsWith(token)) continue;
				buffer.append("-").append(token);
				string = string.substring(token.length());
				modified = true;
				break;
			}
			if (string.isEmpty()) break;
			if (modified) continue;
			
			logger.error("Unexpected string");
			logger.error("  clazz   {}", clazz.getTypeName());
			logger.error("  string  {}", string);
			throw new UnexpectedException("Unexpected string");
		}
		
		return buffer.toString();
	}
	private static String[] tokens = {
		"update", "stock", "info", "fund", "div", "price", "nisa", "etf", "etn", "infra",
		"kessan", "reit", "code", "name", "detail", "json", "list", "ohlcv", "value", "jreit",
		"trading", "jp", "us", "company", "all", "fx", "rate", "2", "intra", "day", "report",
	};
	private static String toMakeGroup(Class<?> clazz) {
		return clazz.getPackageName().replace("yokwe.finance.", "").replace(".", "-");
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	@Override
	public int compareTo(Makefile that) {
		return this.clazz.getTypeName().compareTo(that.clazz.getTypeName());
	}
	
	public static List<Makefile> scanModule(Module module) {
		try {
			var list = new ArrayList<Makefile>();
			
			for(var clazz: ClassUtil.findClassInModule(module)) {
				for(var field: clazz.getDeclaredFields()) {
					field.setAccessible(true);
					if (Modifier.isStatic(field.getModifiers()) && field.getType().equals(Makefile.class)) {
						var makefile = (Makefile)field.get(null);
						list.add(makefile);
					}
				}
			}
			
			Collections.sort(list);
			return list;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	public static List<Makefile> scanModule(Module... modules) {
		var list = new ArrayList<Makefile>();
		
		for(var module: modules) {
			list.addAll(scanModule(module));
		}
		
		Collections.sort(list);
		return list;
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		var module = ClassUtil.findModule("yokwe.finance");
		var list = Makefile.scanModule(module);
		for(var e: list) {
			logger.info("XX  {}", e.clazz.getTypeName());
			logger.info("    {}", e.antTarget);
		}
		
		logger.info("STOP");
	}
}