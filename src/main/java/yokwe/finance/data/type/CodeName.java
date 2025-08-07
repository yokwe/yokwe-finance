package yokwe.finance.data.type;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class CodeName implements Comparable<CodeName> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public final String code;
	public final String name;
	
	public CodeName(String code, String name) {
		this.code = code;
		this.name = name;
	}
	
	public CodeName changeName(String newValue) {
		return new CodeName(code, newValue);
	}
	
	@Override
	public String toString() {
		return String.format("{%s  %s}", code, name);
	}
	
	@Override
	public int compareTo(CodeName that) {
		return this.code.compareTo(that.code);
	}
	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof CodeName) {
			var that = (CodeName)o;
			// ignore name part
			return this.code.equals(that.code);
		}
		return false;
	}
	
	public static Set<String> keySet(Collection<CodeName> collection) {
		return collection.stream().map(o -> o.code).collect(Collectors.toSet());
	}
	public static boolean equals(Collection<CodeName> collectionA, Collection<CodeName> collectionB) {
		var keySetA = keySet(collectionA);
		var keySetB = keySet(collectionB);
		return keySetA.equals(keySetB);
	}
	
	public static void showDifference(String nameA, Collection<CodeName> collectionA, String nameB, Collection<CodeName> collectionB) {
		logger.info("Show difference between {} and {}", nameA, nameB);
		logger.info("====");
		var keySetA = keySet(collectionA);
		var keySetB = keySet(collectionB);
		
		var keyListOnlyA = keySetA.stream().filter(o -> !keySetB.contains(o)).collect(Collectors.toList());
		var keyListOnlyB = keySetB.stream().filter(o -> !keySetA.contains(o)).collect(Collectors.toList());
		
		if (keyListOnlyA.isEmpty() && keyListOnlyB.isEmpty()) {
			logger.info("  No difference");
		}
		
		if (!keyListOnlyA.isEmpty()) {
			var map = collectionA.stream().collect(Collectors.toMap(o -> o.code, o -> o.name));
			Collections.sort(keyListOnlyA);
			logger.info("  Unique to {}  {}", nameA, keyListOnlyA.size());
			for(var code: keyListOnlyA) {
				logger.info("    {}  {}  {}", nameA, code, map.get(code));
			}
		}
		if (!keyListOnlyB.isEmpty()) {
			var map = collectionB.stream().collect(Collectors.toMap(o -> o.code, o -> o.name));
			Collections.sort(keyListOnlyB);
			logger.info("  Unique to {}  {}", nameB, keyListOnlyB.size());
			for(var code: keyListOnlyB) {
				logger.info("    {}  {}  {}", nameB, code, map.get(code));
			}
		}
		logger.info("====");
	}
}
