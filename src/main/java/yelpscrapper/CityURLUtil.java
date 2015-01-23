package yelpscrapper;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CityURLUtil {

	static List<String> cityList = new ArrayList<String>();
	static Map<String, String> citiesUrl = new HashMap<String,String>();
	public static final String citiesJsonString = "Cities";
	public static final String cityPlaceJsonString = "Place";
	public static final String cityUrlJsonString = "CityURL";
	public static final String indexFile = StateZipUtil.dirName+".."+File.separatorChar+"city_index.json";

	static {
		loadCities();
	}

	public static String getState(String city) {
		String[] citySplit = city.split("-");
		return citySplit[0].trim();
	}

	public static String getCity(String city) {
		String[] citySplit = city.split("-");
		return citySplit[1].trim();
	}

	public static List<String> getMatchedCitiesForState(String state) {
		List<String> cities = new ArrayList<String>();
		for(String city : cityList) {
			String stateFromCity = getState(city);
			if(state.equals(stateFromCity)) {
				cities.add(city);
			}
		}
		return cities;
	}

	public static String getCityURL(String city) {
		return citiesUrl.get(city);
	}

	public static void loadCities(){
		File file = new File(indexFile);
		try {
			if(file.exists()) {
				FileReader reader = new FileReader(indexFile);
				JSONParser jsonParser = new JSONParser();
				JSONObject citiesJson = (JSONObject) jsonParser.parse(reader);
				JSONArray citiesArray = (JSONArray) citiesJson.get(citiesJsonString);
				for (Object cityArrayObject : citiesArray) {
					JSONObject cityJsonObject = (JSONObject) cityArrayObject;
					String cityPlace = (String) cityJsonObject.get(cityPlaceJsonString);
					String cityURL = (String) cityJsonObject.get(cityUrlJsonString);
					cityList.add(cityPlace);
					citiesUrl.put(cityPlace, cityURL);
					System.out.println(cityPlace+"-->"+cityURL);
				}

			}
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

}
