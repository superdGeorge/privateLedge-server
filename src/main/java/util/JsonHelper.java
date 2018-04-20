package util;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JsonHelper {

	public static String toJson(Map<String, Object> map) {
		Gson gson = new Gson();
	    Type mapType = new TypeToken<HashMap<String, Integer>>() {}.getType();
	    
	    return gson.toJson(map, mapType);
	}
	
	public static <T> T fromJson(T target, BufferedReader jsonReader) {
		Type type = new TypeToken<T>() {}.getType();
		Gson gson = new Gson();
		return gson.fromJson(jsonReader, type);
	}
	
	public static <T> T fromJson(BufferedReader jsonReader,Class<T> clazz) {
		Gson gson = new Gson();
		return gson.fromJson(jsonReader, clazz);
	}
	
	public static Map<String, Object> decode(BufferedReader bodyReader){
		Map<String, Object> map = new HashMap<>();
		return fromJson(map, bodyReader);
	}
}
