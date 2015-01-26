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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.Business;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class NYCYelpScrapper {
	static List<Business> restaruntList = new ArrayList<Business>();
	public static final String dirName = "/media/santhosh/Data/workspace/datalab/data/from ubuntu/";

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
		String city = "Long Island City";
		String url = "http://www.yelp.com/search?cflt=restaurants&find_loc=Long+Island+City%2C+NY%2C+USA";
		getRestaruntList(city, url);
	}


	public static void getRestaruntList(String city, String url)  throws Exception{
		int count = 0;
		Set<Business> resObjs = new HashSet<Business>();
		while(Boolean.TRUE) {
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
			count += restarunts.size();
			System.out.println(count);
			for(int i =0; i<restarunts.size(); i++) {
				Business restaruntObj = new Business(); 
				Element currentRestarunt = restarunts.get(i).getElementsByClass("biz-listing-large").get(0);
				restaruntObj.setName(currentRestarunt.getElementsByClass("biz-name").text());
				restaruntObj.setUrl(currentRestarunt.getElementsByClass("biz-name").attr("href"));
				restaruntObj.setStreet(currentRestarunt.getElementsByClass("neighborhood-str-list").text());
				restaruntObj.setAddress(currentRestarunt.getElementsByClass("secondary-attributes").get(0).getElementsByTag("address").text());
				restaruntObj.setCity(city);	
				//System.out.println(restaruntObj.Name+ (++sampleCount));
				loadRestaruntDetails(restaruntObj);
			}
		}
	}

	public static void loadRestaruntDetails(Business restarunt ) throws Exception{

		//String fileNamePrefix = "D:\\workspace\\datalab\\NYCYelpData2\\";
		String rName = restarunt.getName();
		File dir = new File(dirName+restarunt.getCity());
		if(!dir.exists()) {
			dir.mkdirs();
		}
		String fileNamePrefix = dirName+restarunt.getCity()+File.separatorChar;
		
		String fileName= fileNamePrefix+rName.replaceAll("\\?", "")+ ".txt";
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
				if (restarunt.getAddress().equals(addr) 
						&& restarunt.getUrl().equals(url)) {
					System.out.println("Already Present Restaurant "+rName);
					return;
				} else {
				   fileName = fileNamePrefix+restarunt.getName().replaceAll("\\?", "")+"_"+count+".txt";
				}
			} catch(IOException e) {
				e.printStackTrace();
				System.exit(0);
			} catch(ParseException e) {
				e.printStackTrace();
				System.exit(0);
			}
			return;
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
		reviews.put("street", restarunt.getStreet());
		reviews.put("Address", restarunt.getAddress());
		reviews.put("URL", restarunt.getUrl());
		PrintWriter writer = null;
		try {
			 writer = new PrintWriter(fileName, "UTF-8");
		}catch(Exception e) {
			return;
		}

		writer.println(reviews.toJSONString());
		writer.flush();
		writer.close();

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


