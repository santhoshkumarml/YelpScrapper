package yelpscrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jdk.nashorn.internal.runtime.ParserException;
import models.Business;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class StateCityWiseScrapper {

	private static final String NO_ZIP_CODE = "No Zip Code";
	public static final String[] statesUnordered = {"HI","TX","FL","NV","WA","NY",
		"SC","WI","MA","MD","IA","OH","GA","ID","MI","CA","UT","MN",
		"MO","IL","OR","IN","VA","CO","KY","CT","PA","LA","NC","RI","AZ",
		"TN","NJ","VT","DC","NM"};

	public static final String[] states = {"NJ","VT","CT","PA"};
	//	{"HI","TX","FL","NV","WA",
	//		"SC","WI","MA","MD","IA",
	//		"OH","GA","ID","MI","CA",
	//		"UT","MN","MO","IL","OR",
	//		"IN","VA","CO","KY","PA",
	//		"LA","NC","RI","AZ",
	//		"TN","DC","NM"};

	public static void main(String args[]) throws Exception {
		final String authUser = "RSADINENI";
		final String authPassword = "eBay@456";
		Authenticator.setDefault(
				new Authenticator() {
					public PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(
								authUser, authPassword.toCharArray());
					}
				}
				);

		System.setProperty("http.proxyUser", authUser);
		System.setProperty("http.proxyPassword", authPassword);
		
		for(int i=0;i<states.length;i++) {
			List<String> matchedCities = CityURLUtil.getMatchedCitiesForState(states[i]);
			for(String matchedCity : matchedCities) {
				String url = CityURLUtil.getCityURL(matchedCity);
				getRestaruntList(url);	
			}
		}
		
	}

	public static void getRestaruntList(String cityURL)  throws Exception{
		int count = 0;
		int crawledCount = 0;
		while(Boolean.TRUE) {
			String url = cityURL+"&start="+count;
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
			for(int i =0; i<restarunts.size(); i++) {
				Business restaruntObj = new Business(); 
				Element currentRestarunt = restarunts.get(i).getElementsByClass("biz-listing-large").get(0);
				restaruntObj.Name = currentRestarunt.getElementsByClass("biz-name").text();
				restaruntObj.url = currentRestarunt.getElementsByClass("biz-name").attr("href");
				restaruntObj.street = currentRestarunt.getElementsByClass("neighborhood-str-list").text();
				restaruntObj.Address =  currentRestarunt.getElementsByClass("secondary-attributes").get(0).getElementsByTag("address").text();
				//System.out.println(restaruntObj.Name+ (++sampleCount));
				String regex = "[0-9][0-9][0-9][0-9][0-9]";
				Pattern  p = Pattern.compile(regex);
				Matcher m = p.matcher(restaruntObj.Address);
				String resultingZipCode = "";
				if (m.find()) {
					resultingZipCode = m.group();
				} else {
					System.out.println("No Zip code in "+restaruntObj.Address);
					resultingZipCode = NO_ZIP_CODE;
				}
				boolean isCrawled = loadRestaruntDetails(resultingZipCode, restaruntObj);
				if(isCrawled)
					crawledCount++;
			}
			count += restarunts.size();
			System.out.println(count+"-----"+crawledCount);
		}
	}

	public static boolean loadRestaruntDetails(String zipCode, Business restarunt) throws Exception{
		String fileNamePrefix = StateZipUtil.dirName+zipCode+File.separatorChar;
		File dirFile = new File(fileNamePrefix);
		if(!dirFile.exists()) {
			dirFile.mkdirs();
		}
		String fileName= fileNamePrefix+restarunt.Name.replaceAll("\\?", "")+ ".txt";
		System.out.println(fileName);
		int count=1;
		while(new File(fileName).exists() || !isFilenameValid(fileName)) {
			try {
				// read the json file
				FileReader reader = new FileReader(fileName);

				JSONParser jsonParser = new JSONParser();
				JSONObject reviewObject = (JSONObject) jsonParser.parse(reader);
				String street = (String)reviewObject.get("street");
				String addr = (String)reviewObject.get("Address");
				String url = (String)reviewObject.get("URL");
				if (restarunt.street.equals(street) && restarunt.Address.equals(addr) 
						&& restarunt.url.equals(url)) {
					System.out.println("Already Present Restaurant "+restarunt.Name);
					return false;
				} else {
					fileName = fileNamePrefix+restarunt.Name.replaceAll("\\?", "")+"_"+count+".txt";
					count++;
				}
			} catch(IOException e) {
				e.printStackTrace();
				System.exit(0);
			} catch(ParserException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}

		List<JSONObject> reccomendedReviews = YelpScrappper.loadRecommendedReviewsForRestarunt(restarunt);
		List<JSONObject> nonReccomendedReviews = YelpScrappper.loadNonRecommendedReviewsForRestarunt(restarunt);
		JSONObject reviews = new JSONObject();
		JSONArray reccomendedReviewArray = new JSONArray();
		JSONArray nonReccomendedReviewArray = new JSONArray();
		for(JSONObject reccomended :reccomendedReviews ) {
			reccomendedReviewArray.add(reccomended);
		}
		for(JSONObject nonReccomended :nonReccomendedReviews ) {
			nonReccomendedReviewArray.add(nonReccomended);
		}
		reviews.put("Reccomended", reccomendedReviewArray);
		reviews.put("nonReccomended", nonReccomendedReviewArray);
		reviews.put("street", restarunt.street);
		reviews.put("Address", restarunt.Address);
		reviews.put("URL", restarunt.url);
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(fileName, "UTF-8");
		}catch(Exception e) {
			return false;
		}

		writer.println(reviews.toJSONString());
		writer.flush();
		writer.close();
		return true;
	}



	public static String getHtmlPage(String urlString) throws Exception {
		URL url = new URL(urlString);
		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("us-il.proxymesh.com", 31280)); 
		HttpURLConnection uc = (HttpURLConnection)url.openConnection(proxy);

		uc.connect();

		String line = null;
		StringBuffer tmp = new StringBuffer();
		BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
		while ((line = in.readLine()) != null) {
			tmp.append(line);
		}
		return String.valueOf(tmp);
	}

	public static boolean isFilenameValid(String file) {
		File f = new File(file);
		try {
			f.getCanonicalPath();
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}

}
