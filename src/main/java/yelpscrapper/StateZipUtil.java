package yelpscrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class StateZipUtil {
	public static final String dirName = "/media/santhosh/Data/workspace/datalab/data/from ubuntu/zips/";
	public static final String indexFile = dirName+"index.json";
	public static final String STATE_ZIPS = "CityZips";
	public static final String STATE = "State";
	public static final String ZIPS = "Zips";
	private static HashMap<String, Doublet<Integer, Integer>> stateToZips =
			new HashMap<String, Doublet<Integer,Integer>>();
	
	static{
		loadZips();
		System.out.println("States:"+ stateToZips.keySet().size());
	}
	
	public static Doublet<Integer, Integer> getZipsForState(String state) {
		return stateToZips.get(state);
	}
	
	public static void loadZips() {
		File file = new File(indexFile);
		if(!file.exists()) {
			readZipsFromURL(file);
		}
		
		try {
			FileReader reader = new FileReader(file);
			JSONParser jsonParser = new JSONParser();
			JSONObject stateZips = (JSONObject) jsonParser.parse(reader);
			JSONArray stateZipsArray = (JSONArray)stateZips.get(STATE_ZIPS);
			for (Object stateZipJson : stateZipsArray) {
				JSONObject stateZip = (JSONObject)stateZipJson;
				String state = (String)stateZip.get(STATE);
				String zips = (String)stateZip.get(ZIPS);
				String[] zipRangeString = zips.split("-");
				//System.out.println(zips+" "+zipRangeString[0]+"  "+zipRangeString[1]);
				stateToZips.put(state,
						new Doublet<Integer, Integer>(
								Integer.parseInt(zipRangeString[0]),
								Integer.parseInt(zipRangeString[1])));
			}
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	
	private static void readZipsFromURL(File file){
		String url = "/home/santhosh/Downloads/states.htm";
		Document doc = null;
		try {
			doc = Jsoup.connect(url).timeout(60*1000).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Elements rows = doc.getElementsByTag("tr");
		JSONObject json = new JSONObject();
		
		JSONArray jsonArray = new JSONArray();

		json.put(STATE_ZIPS, jsonArray);
		int count = 0;
		for(Element row : rows) {
			Elements tds = row.getElementsByTag("td");
			assert tds.size() == 6;
			Element firstTd = tds.get(0);
			Elements pTags = firstTd.select("p font font");
			String state = "", zips =""; 
			if (pTags.size() > 0) {
				Element pTag = pTags.first();
				state = pTag.text();
			} else {
				pTags = firstTd.select("p font");
				if (pTags.size() > 0) {
					Element pTag = pTags.first();
					state = pTag.text();
					if(state.isEmpty()||state.equalsIgnoreCase("State")) {
						continue;
					}
				} else {
					continue;
				}
			}


			Element secondTd = tds.get(1);
			zips = secondTd.text();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(STATE, state);
			jsonObject.put(ZIPS, zips);
			jsonArray.add(jsonObject);
			count++;
			System.out.println(jsonObject+"  "+jsonArray.size());
			
			state = "";
			zips ="";

			Element fourthTd = tds.get(3);
			pTags = fourthTd.select("p font font");
			if (pTags.size() > 0) {
				Element pTag = pTags.first();
				state = pTag.text();
			} else {
				pTags = firstTd.select("p font");
				if (pTags.size() > 0) {
					Element pTag = pTags.first();
					state = pTag.text();
					if(state.isEmpty()||state.equalsIgnoreCase("State")) {
						continue;
					}
				} else {
					continue;
				}
			}

			Element fivthTd = tds.get(4);
			zips = fivthTd.text();

			JSONObject jsonObject1 = new JSONObject();
			jsonObject1.put(STATE, state);
			jsonObject1.put(ZIPS, zips);
			jsonArray.add(jsonObject1);
			
			System.out.println(jsonObject1+"  "+jsonArray.size());
			count++;
		}

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(file);
			pw.write(json.toJSONString());
			pw.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(pw != null)
				pw.close();
		}


	}

}
