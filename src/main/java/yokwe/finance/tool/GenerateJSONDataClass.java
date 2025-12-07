package yokwe.finance.tool;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import yokwe.util.AutoIndentPrintWriter;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

public class GenerateJSONDataClass {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	static class FieldInfo {
		static final Pattern PAT_INT  = Pattern.compile("(\\+|-)?[0-9]+");
		static final Pattern PAT_REAL = Pattern.compile("(\\+|-)?[0-9]+\\.[0-9]+");
		
		enum Type {
			STRING,
			BOOLEAN,
			INTEGER,
			REAL,
			//
			OBJECT,
			ARRAY,
		}
		
		String  name;
		boolean isArray;
		boolean isOptional;
		Type    type;
		
		
		FieldInfo(String name, JsonValue jsonValue) {
			this.name       = name;
			this.isArray    = jsonValue.getValueType() == ValueType.ARRAY;
			this.isOptional = false;
			
			switch(jsonValue.getValueType()) {
			case STRING:
				this.type = Type.STRING;
				break;
			case NUMBER:
			{
				var string = jsonValueToString(jsonValue);
				if (PAT_INT.matcher(string).matches()) {
					this.type = Type.INTEGER;
				} else if (PAT_REAL.matcher(string).matches()) {
					this.type = Type.REAL;
				} else {
					logger.error("Unexpected string");
					logger.error("  string {}!", string);
					throw new UnexpectedException("Unexpected string");
				}
			}
				break;
			case TRUE:
				this.type = Type.BOOLEAN;
				break;
			case FALSE:
				this.type = Type.BOOLEAN;
				break;
			case OBJECT:
				this.type = Type.OBJECT;
				break;
			case ARRAY:
				this.type = Type.ARRAY;
				break;
			default:
				logger.error("Unexpeced jsonValue");
				logger.error("  name  {}", name);
				logger.error("  type   {}", jsonValue.getValueType());
				logger.error("  value  {}", jsonValue.toString());
				throw new UnexpectedException("Unexpected jsonValue");
			}
		}
		
		void setOptional() {
			isOptional = true;
		}
		String javaType() {
			switch(type) {
			case STRING:
				return "String";
			case BOOLEAN:
				return "boolean";
			case INTEGER:
				return "int";
			case REAL:
				return "BigDecimal";
			case OBJECT:
				return name;
			case ARRAY:
				return name;
			default:
				logger.error("Unexpeced type");
				logger.error("  name   {}", name);
				logger.error("  type   {}", type);
				throw new UnexpectedException("Unexpected type");								
			}
		}
	}
	static class ClassInfo {
		String                 name;
		Map<String, FieldInfo> map = new LinkedHashMap<String, FieldInfo>();
		
		ClassInfo(String name) {
			this.name = name;
		}
		
		void add(FieldInfo fieldInfo) {
			if (map.containsKey(fieldInfo.name)) {
				// already contain
			} else {
				map.put(fieldInfo.name, fieldInfo);
			}
		}
	}
	
	static class Context {
		Map<String, ClassInfo> map = new LinkedHashMap<String, ClassInfo>();
		
		Context(Map<String, ClassInfo> map) {
			this.map = map;
		}
		
		void add(ClassInfo classInfo) {
			if (map.containsKey(classInfo.name)) {
				// already
				var old = map.get(classInfo.name);
				for(var e: classInfo.map.values()) {
					if (!old.map.containsKey(e.name)) {
						e.setOptional();
					}
					old.add(e);						
				}
			} else {
				map.put(classInfo.name, classInfo);
			}
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		{			
			var jsonPath = "tmp/b.json";
			var javaPath = "tmp/b.java";
			
			genCode(jsonPath, javaPath);
			
		}
		
		logger.info("STOP");
	}
	
	
	static void genCode(String jsonPath, String javaPath) {
		var jsonString = FileUtil.read().file(jsonPath);
		logger.info("jsonString  {}", jsonString.length());
		var reader = new StringReader(jsonString);
		
		// sanity check
		var jsonValue = getJsonValue(reader);
		var valueType = jsonValue.getValueType();

		logger.info("valueType  {}", valueType);
		if (valueType != ValueType.OBJECT) {
			logger.error("Expect object");
			logger.error("  valueType  {}", valueType);
			throw new UnexpectedException("Expect object");
		}
		
		var classInfoMap = new LinkedHashMap<String, ClassInfo>();
		var context = new Context(classInfoMap);
		buildContext(context, "Data", jsonValue.asJsonObject());
		
		for(var e: classInfoMap.values()) {
			logger.info("class  {}", e.name);
			for(var ee: e.map.values()) {
				logger.info("  {}  {}", ee.name, ee.type);
			}
		}
		
		try (var out = new AutoIndentPrintWriter(new PrintWriter(javaPath))) {			
			// generate code using classInfoMap
			out.println("package yokwe.finance.tool;");
			out.println();
			out.println("public class JSON_Data {");
			
			for(var classInfo: classInfoMap.values()) {				
				out.println();
				out.println("public static class %s {", classInfo.name);
				
				for(var fieldInfo: classInfo.map.values()) {
					out.println("%spublic %s%s %s;", fieldInfo.isOptional ? "@Optional " : "", fieldInfo.javaType(), fieldInfo.isArray ? "[]" : "", fieldInfo.name) ;
				}
				out.println();
				
				out.println("public %s() {", classInfo.name);
				out.println("}");
				out.println();
				
				out.println("@Override");
				out.println("public String toString() {");
				out.println("return ToString.withFieldName(this);");
				out.println("}");
				
				out.println("}");
			}
			
			out.println("}");
			
		} catch (FileNotFoundException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	
	static void buildContext(Context context, String className, JsonObject jsonObject) {
		var classInfo = new ClassInfo(className);
		for(var key: jsonObject.keySet()) {
			var value = jsonObject.get(key);
			
			var fieldInfo = new FieldInfo(key, value);
			classInfo.add(fieldInfo);
			
			switch(fieldInfo.type) {
			case ARRAY:
				buildContext(context, key, value.asJsonArray());
				break;
			case OBJECT:
				buildContext(context, key, value.asJsonObject());
				break;
			default:
				break;
			}
		}
		context.add(classInfo);
	}
	
	static void buildContext(Context context, String arrayName, JsonArray jsonArray) {
		var size = jsonArray.size();
		if (size == 0) {
			var classInfo = new ClassInfo(arrayName);
			context.add(classInfo);
			return;
		}
		
		for(int i = 0; i < size; i++) {
			var jsonValue = jsonArray.get(i);
			switch(jsonValue.getValueType()) {
			case OBJECT:
				buildContext(context, arrayName, jsonValue.asJsonObject());
				break;
			default:
				logger.error("Unexpeced jsonValue");
				logger.error("  name   {}", arrayName);
				logger.error("  type   {}", jsonValue.getValueType());
				logger.error("  value  {}", jsonValue.toString());
				throw new UnexpectedException("Unexpected jsonValue");				
			}
		}
	}
	
	
	private static JsonValue getJsonValue(Reader reader) {
		try (JsonReader jsonReader = Json.createReader(reader)) {
			return jsonReader.readValue();
		} catch(JsonException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	private static String jsonValueToString(JsonValue jsonValue) {
		var string = jsonValue.toString();
		if (jsonValue.getValueType() == ValueType.STRING) {			
			// sanity check
			var first = string.charAt(0);
			var last  = string.charAt(string.length() - 1);
			if (first != '"' || last != '"') {
				// unexpected
				logger.error("Unexpected first or last character");
				logger.error("  string {}!", string);
				logger.error("  first  {}", first);
				logger.error("  last   {}", last);
				throw new UnexpectedException("Unexpected first or last character");
			}
			
			// remove first and last character of string
			return string.substring(1, string.length() - 1);
		} else {
			return string;
		}
	}
}
