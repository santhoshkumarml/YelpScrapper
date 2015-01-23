package yelpscrapper;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.Business;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class UnwatedUtils {
	

	public static void main(String[] args) throws Exception {
		StateZipUtil.loadZips();
	}
	
	public static void checkReDistribution() {
		File dir = new File(StateZipUtil.dirName);
		int count = 0;
		try {
			File[] files = dir.listFiles();
			for(int i=0; i < files.length;i++) {
				File f = files[i];
				if(f.isDirectory()) {
					count+=f.list().length;
				}
			}
			System.out.println(count);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static void redistributeRestaruntsToZips() {
		File dir = new File("/media/santhosh/Data/workspace/datalab/data/from ubuntu/"+"NY-New York");
		try {
			File[] files = dir.listFiles();
			FileReader reader = null;
			PrintWriter pw = null;
			for(int i=0;i<files.length;i++) {
				File f = files[i];
				reader = new FileReader(f);
				JSONParser jsonParser = new JSONParser();
				JSONObject reviewObject = (JSONObject) jsonParser.parse(reader);
				String url = (String)reviewObject.get("URL");
				String addr = (String)reviewObject.get("Address");
				reader.close();

				String regex = "[0-9][0-9][0-9][0-9][0-9]";
				Pattern  p = Pattern.compile(regex);
				Matcher m = p.matcher(addr);
				if (m.find()) {
					String zipCode = m.group();
					String dirName = StateZipUtil.dirName+zipCode.trim();
					dir = new File(dirName);
					if(!dir.exists()) {
						dir.mkdirs();
					}
					try {
					File newFile = new File(dirName+File.separatorChar+f.getName());
					pw = new PrintWriter(newFile);
					pw.write(reviewObject.toJSONString());
					pw.flush();
					} finally {
						if(pw !=null)
							pw.close();
					}
				} else {
					System.out.println("Something wrong in "+addr+"  "+url);
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

//	private static void printStates() {
//		Set<String> states = new HashSet<String>();
//		for(String city : cityList) {
//			String[] citySplit = city.split("-");
//			String state = citySplit[0].trim();
//			states.add(state);
//		}
//		for(String state : states) {
//			System.out.print(state+",");
//		}
//	}
//
//	public static void loadCities() throws Exception {
//		File file = new File(indexFile);
//		if(file.exists()) {
//			FileReader reader = new FileReader(indexFile);
//			JSONParser jsonParser = new JSONParser();
//			JSONObject citiesJson = (JSONObject) jsonParser.parse(reader);
//			JSONArray citiesArray = (JSONArray) citiesJson.get(citiesJsonString);
//			for (Object cityArrayObject : citiesArray) {
//				JSONObject cityJsonObject = (JSONObject) cityArrayObject;
//				String cityPlace = (String) cityJsonObject.get(cityPlaceJsonString);
//				String cityURL = (String) cityJsonObject.get(cityUrlJsonString);
//				cityList.add(cityPlace);
//				citiesUrl.put(cityPlace, cityURL);
//				System.out.println(cityPlace+"-->"+cityURL);
//			}
//
//		}
//	}


	public static void getRestaruntList(String city, String url)  throws Exception{
		File dir = new File(StateZipUtil.dirName+city);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		File[] files = dir.listFiles();
		int count = files.length;
		int crawledCount = 0;
		while(count<1000 && crawledCount<1000) {
			url = url+"&start="+count;
			Document doc = Jsoup.connect(url).timeout(60*1000).get();
			//String webPage = getHtmlPage(url);
			//Document doc = Jsoup.parse(webPage);
			//System.out.println(doc);
			Elements elements = doc.getElementsByClass("search-results");
			if(elements == null || elements.size() == 0) {
				return ;
			}
			Elements restarunts = elements.get(0).select("div.search-result.natural-search-result");
			if(restarunts == null || restarunts.size() == 0) {
				return ;
			}
			int hitCount=0;
			for(int i =0; i<restarunts.size(); i++) {
				Business restaruntObj = new Business(); 
				Element currentRestarunt = restarunts.get(i).getElementsByClass("biz-listing-large").get(0);
				restaruntObj.Name = currentRestarunt.getElementsByClass("biz-name").text();
				restaruntObj.url = currentRestarunt.getElementsByClass("biz-name").attr("href");
				restaruntObj.street = currentRestarunt.getElementsByClass("neighborhood-str-list").text();
				restaruntObj.Address =  currentRestarunt.getElementsByClass("secondary-attributes").get(0).getElementsByTag("address").text();
				restaruntObj.city  = city;	
				//System.out.println(restaruntObj.Name+ (++sampleCount));
				boolean isCrawled = true; 
				//isCrawled = loadRestaruntDetails(restaruntObj);
				if(isCrawled)
					hitCount++;
				crawledCount++;
			}
			count += hitCount;
			System.out.println(count+"-----"+crawledCount);
		}
	}



}
