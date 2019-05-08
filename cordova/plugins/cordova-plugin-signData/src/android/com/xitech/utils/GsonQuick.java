package com.xitech.utils;


import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * Gson 工具类
 */
public final class GsonQuick {
	/**
	 * Json 解析器
	 */
	private static final JsonParser PARSER = new JsonParser();

	private static Gson gson = null;

	/**
	 * 设置一个通用的Gson
	 * 
	 * @param gs
	 */
	public static void setGson(Gson gs) {
		gson = gs;
	}

	public static Gson getGson() {
		if (gson == null) {
			GsonBuilder builder = new GsonBuilder();
			builder.serializeNulls();
			builder.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);
			gson = builder.disableHtmlEscaping().create();
		}
		return gson;
	}

	/**
	 * 获取Json对象里面的键, 并将其值以String返回.<br/>
	 * 当值为json对象或者json数组时,返回其json格式的字符串
	 * 
	 * @param json
	 * @param key
	 * @return
	 */
	public static String getString(String json, String key) {
		JsonElement element = PARSER.parse(json);
		if (element.isJsonObject()) {
			JsonObject obj = element.getAsJsonObject();
			JsonElement value = obj.get(key);
			if (value != null) {
				if (value instanceof JsonPrimitive) {
					return value.getAsString();
				} else if (value instanceof JsonNull) {
					return null;
				} else {
					return value.toString();
				}
			}
		}
		return null;
	}

	/**
	 * 将json解析为一个JsonObject
	 * 
	 * @param json
	 * @param myInformationBeanDataClass
	 * @return
	 *//*
	public static JsonObject toJsonObject(String json, Class<MyInformationBeanData> myInformationBeanDataClass) {
		JsonElement element = PARSER.parse(json);
		return element.getAsJsonObject();
	}*/

	/**
	 * 将json解析为一个JsonArray
	 * 
	 * @param json
	 * @return
	 */
	public static JsonArray toJsonArray(String json) {
		JsonElement element = PARSER.parse(json);
		return element.getAsJsonArray();
	}
	/**
	 * 将json解析为一个JsonArray
	 *
	 * @param json
	 * @return
	 */
	public static HashMap<String,Object> toJsonMap(String json) {
		return  getGson().fromJson(json, new TypeToken<HashMap<String,String>>(){}.getType());
	}

	/**
	 * 将json对象序列化为一个对象实例
	 * 
	 * @param json
	 * @param clss
	 *            要序列化为的对象
	 * @return
	 */
	public static <T> T toObject(String json, Class<T> clss) {
		Gson gson = getGson();
		return gson.fromJson(json, clss);
	}

	/**
	 * 将json数组序列化为一个对象的List实例
	 * 
	 * @param json
	 * @param clss
	 *            对象类型
	 * @return
	 */
	public static <T> List<T> toList(String json, Class<T> clss) {
		JsonElement element = PARSER.parse(json);
		if (element.isJsonArray()) {
			Gson gson = getGson();
			JsonArray array = element.getAsJsonArray();
			int size = array.size();
			ArrayList<T> result = new ArrayList<T>(size);
			for (int i = 0; i < size; i++) {
				JsonElement item = array.get(i);
				T t = gson.fromJson(item, clss);
				result.add(t);
			}
			return result;
		}
		return null;
	}

	/***
	 * 将对象转换成相对应的字符串
	 * 
	 * @return json字符串
	 */
	public static String toJsonFromBean(Object obj) {
		try {
			Gson gson = getGson();
			return gson.toJson(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/***
	 * 将list集合转换成相对应的字符串
	 * 
	 * @param lists
	 *            需要转换的list集合
	 * @return
	 */
	public static <T> String toJsonFromList(List<T> lists) {
		try {
			Gson gson = getGson();
			TypeToken<List<T>> type = new TypeToken<List<T>>() {
			};
			return gson.toJson(lists, type.getType());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将map集合转换成相对应的字符串
	 * 
	 * @param map
	 * @return
	 */
	public static <T> String toJsonFromMap(Map<String, T> map) {
		try {
			Gson gson = getGson();
			TypeToken<Map<String, T>> type = new TypeToken<Map<String, T>>() {
			};
			return gson.toJson(map, type.getType());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将list<map<>>集合转换成相对应的字符串
	 * 
	 * @param listMaps
	 * @return
	 */
	public static <T> String toJsonFromListMap(List<Map<String, T>> listMaps) {
		try {
			Gson gson = getGson();
			TypeToken<List<Map<String, T>>> type = new TypeToken<List<Map<String, T>>>() {
			};
			return gson.toJson(listMaps, type.getType());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * 功能:将请求参数转换成数组形式
	 * 
	 * @param parms
	 * @return
	 * @author: huchao
	 * @date:2015-5-27下午5:19:44
	 */
	public static String paramsToJson(Map<String, String> parms) {
		List<Map<String, String>> jsonArray = new ArrayList<Map<String,String>>();
		jsonArray.add(parms);
		return toJsonFromList(jsonArray);
	}
}
