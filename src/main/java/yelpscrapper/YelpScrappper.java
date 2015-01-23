package yelpscrapper;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jdk.nashorn.internal.runtime.ParserException;
import models.Business;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class YelpScrappper {
	static int numOfRestarunts=0;
	static Map<String, String> cityUrl = new HashMap<String,String>();
	static Map<String, List<Business>> cityRestarunts = new HashMap<String, List<Business>>();
	public static void main(String args[]) throws Exception {
//		final String authUser = "RSADINENI";
//		final String authPassword = "eBay@456";
//		Authenticator.setDefault(
//				new Authenticator() {
//					public PasswordAuthentication getPasswordAuthentication() {
//						return new PasswordAuthentication(
//								authUser, authPassword.toCharArray());
//					}
//				}
//				);
//
//		System.setProperty("http.proxyUser", authUser);
//		System.setProperty("http.proxyPassword", authPassword);
		
		loadCities();
		loadRestarunts();
		System.exit(0);
	}


	public static void loadRestaruntDetails(String city, Business restarunt) throws Exception{
		int count =0;
		count++;
		String fileNamePrefix = "/media/santhosh/Data/workspace/datalab/data/from ubuntu/"+city;
		String rName = restarunt.getName();
		File dir = new File(fileNamePrefix);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		String fileName = fileNamePrefix+rName.replaceAll("\\?", "")+ ".txt";

		while(new File(fileName).exists() || !isFilenameValid(fileName)) {
			try {
				// read the json file
				FileReader reader = new FileReader(fileName);

				JSONParser jsonParser = new JSONParser();
				JSONObject reviewObject = (JSONObject) jsonParser.parse(reader);
				String street = (String)reviewObject.get("street");
				String addr = (String)reviewObject.get("Address");
				String url = (String)reviewObject.get("URL");
				if (restarunt.getStreet().equals(street) && restarunt.getAddress().equals(addr) 
						&& restarunt.getUrl().equals(url)) {
					System.out.println("Already Present Restaurant "+rName);
					return;
				} else {
					fileName = fileNamePrefix+rName.replaceAll("\\?", "")+"_"+count+".txt";
				}
			} catch(IOException e) {
				e.printStackTrace();
				System.exit(0);
			} catch(ParserException e) {
				e.printStackTrace();
				System.exit(0);
			}
			return;
		}
		System.out.println("Remaining Restarunts" + (numOfRestarunts - count));
		List<JSONObject> reccomendedReviews = loadRecommendedReviewsForRestarunt(restarunt);
		List<JSONObject> nonReccomendedReviews = loadNonRecommendedReviewsForRestarunt(restarunt);
		JSONObject reviews = new JSONObject();
		JSONArray reccomendedReviewArray = new JSONArray();
		JSONArray nonReccomendedReviewArray = new JSONArray();
		for(JSONObject reccomended :reccomendedReviews ) {
			reccomendedReviewArray.add(reccomended);
		}
		for(JSONObject nonReccomended :nonReccomendedReviews ) {
			nonReccomendedReviewArray.add(nonReccomended);
		}
		reviews.put("Reccomnded", reccomendedReviewArray);
		reviews.put("nonReccomnded", nonReccomendedReviewArray);

		PrintWriter writer = new PrintWriter(fileName, "UTF-8");

		writer.println(reviews.toJSONString());
		writer.flush();
		writer.close();
}


public static List<JSONObject>  loadRecommendedReviewsForRestarunt(Business restarunt) throws Exception {
	List<JSONObject> jsonObjectList = new ArrayList<JSONObject>(); 
	int count = 0;
	while(Boolean.TRUE) {
		String url =  "http://www.yelp.com";

		//Document doc  = null;
		url += restarunt.getUrl()+ "?sort_by=date_desc&start=" + count;
		Document doc = Jsoup.connect(url).timeout(60*1000).get();
		System.out.println(doc.body());
		//String htmlPage = NYCYelpScrapper.getHtmlPage(url);
		Thread.sleep(1000);

		//doc = Jsoup.parse(htmlPage);

		Elements reviewList = doc.getElementsByClass("review-list");
		if(reviewList.size() == 0) {
			return jsonObjectList;
		}
		Elements reviews = reviewList.get(0).getElementsByClass("review");
		if(reviews.size() == 0) {
			return jsonObjectList;
		}
		count+= reviews.size();
		for(int i =0; i< reviews.size(); i++) {
			Elements metaName = reviews.get(i).select("meta[itemprop=author]");
			Elements metaRating = reviews.get(i).select("meta[itemprop=ratingValue]");
			Elements metaPublished = reviews.get(i).select("meta[itemprop=datePublished]");
			String authorName = metaName.first().attr("content").toString();
			String rating = metaRating.first().attr("content").toString();
			String date =  metaPublished.first().attr("content").toString();
			String place = reviews.get(i).getElementsByClass("user-location").text();
			String friendCount= reviews.get(i).getElementsByClass("friend-count").text();
			String reviewCount = reviews.get(i).getElementsByClass("review-count").text();
			String metaComment = reviews.get(i).getElementsByClass("review-content").select("[itemprop=description]").text();
			Elements metaDataHoverIdForUsrElements = 
					reviews.get(i).getElementsByClass(
							"user-name").first().getElementsByClass(
									"user-display-name");
			String metaDataHoverIdForUsr = "";
			if(metaDataHoverIdForUsrElements.isEmpty()){
				continue;
			} else {
				metaDataHoverIdForUsr = metaDataHoverIdForUsrElements.first().attr(
						"data-hovercard-id").toString();
			}
			assert !metaDataHoverIdForUsr.isEmpty();

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("Name", authorName);
			jsonObject.put("usrId", metaDataHoverIdForUsr);
			jsonObject.put("Date", date);
			jsonObject.put("Rating", rating);
			jsonObject.put("Place", place);
			jsonObject.put("friendCount", friendCount);
			jsonObject.put("reviewCount", reviewCount);
			jsonObject.put("ReviewComment", metaComment);
			//jsonObject.put("imgSrc", imgSrc);
			jsonObjectList.add(jsonObject);

		}
	}
	return jsonObjectList;
}

public static List<JSONObject>  loadNonRecommendedReviewsForRestarunt(Business restarunt) throws Exception {
	List<JSONObject> jsonObjectList = new ArrayList<JSONObject>(); 
	int count = 0;
	while(Boolean.TRUE) {
		String url =  "http://www.yelp.com/not_recommended_reviews";
		url += restarunt.getUrl() +  "?not_recommended_start=" + count;
		url = url.replace("/biz","");
		//Document doc = null;
		Document doc = Jsoup.connect(url).timeout(60*1000).get();
		//String htmlPage = NYCYelpScrapper.getHtmlPage(url);
		//doc = Jsoup.parse(htmlPage);
		Thread.sleep(1000);
		Elements reviewList = doc.getElementsByClass("not-recommended-reviews");
		if(reviewList.size() == 0) {
			return jsonObjectList;
		}
		Elements reviews = reviewList.get(0).getElementsByClass("review");
		if(reviews.size() == 0) {
			return jsonObjectList;
		}


		if(reviews.size() == 0) {
			return jsonObjectList;
		}
		count+= reviews.size(); 
		for(int i =0; i< reviews.size(); i++) {

			String authorName = reviews.get(i).getElementsByClass("user-display-name").text();
			String rating = reviews.get(i).getElementsByClass("rating-very-large").get(0).getElementsByTag("i").attr("title").substring(0,3);

			String date = reviews.get(i).getElementsByClass("rating-qualifier").text();
			String place = reviews.get(i).getElementsByClass("user-location").text();
			String friendCount= reviews.get(i).getElementsByClass("friend-count").text();
			String reviewCount = reviews.get(i).getElementsByClass("review-count").text();
			String ReviewComment = reviews.get(i).getElementsByClass("review-content").get(0).getElementsByTag("p").text();
			Elements metaDataHoverIdForUsrElements = 
					reviews.get(i).getElementsByClass(
							"user-name").first().getElementsByClass(
									"user-display-name");
			String metaDataHoverIdForUsr = "";
			if(metaDataHoverIdForUsrElements.isEmpty()){
				continue;
			} else {
				metaDataHoverIdForUsr = metaDataHoverIdForUsrElements.first().attr(
						"data-hovercard-id").toString();
			}
			assert !metaDataHoverIdForUsr.isEmpty();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("Name", authorName);
			jsonObject.put("usrId", metaDataHoverIdForUsr);
			jsonObject.put("Date", date);
			jsonObject.put("Rating", rating);
			jsonObject.put("Place", place);
			jsonObject.put("friendCount", friendCount);
			jsonObject.put("reviewCount", reviewCount);
			jsonObject.put("ReviewComment", ReviewComment);
			jsonObjectList.add(jsonObject);

		}
	}
	return jsonObjectList;
}


public static void loadRestarunts() throws Exception{
	for (Map.Entry<String, String> entry : cityUrl.entrySet())
	{
		loadRestaruntsForUrl(entry.getKey(), entry.getValue());
	}
}


public static List<Business>loadRestaruntsForUrl(String city, String url) throws Exception {
	url = "http://www.yelp.com/c" + url +"/restaurants"; 
	Document doc = Jsoup.connect(url).timeout(60*1000).get();
	Elements moreRestaruntUrlElements = doc.getElementsByClass("link-more");
	System.out.println(moreRestaruntUrlElements);
	assert moreRestaruntUrlElements.size() > 0;
	Element moreRestaruntUrlElement = moreRestaruntUrlElements.first();
	System.out.println(moreRestaruntUrlElement);
	assert moreRestaruntUrlElement.text().equals("More Restaurants");
	String moreRestaruntUrl = moreRestaruntUrlElement.attr("href");
	doc = Jsoup.connect(moreRestaruntUrl).timeout(60*1000).get();
	int count = 0;
	List<Business> resObjs = new ArrayList<Business>();
	while(count<1000) {
		Elements elements = doc.getElementsByClass("search-results");
		if(elements == null || elements.size() == 0) {
			return resObjs;
		}
		Elements restarunts = elements.get(0).select("div.search-result.natural-search-result");
		if(restarunts == null || restarunts.size() == 0) {
			return resObjs;
		}
		count += restarunts.size();
		System.out.println(count);
		for(int i =0; i<restarunts.size(); i++) {
			Business restaruntObj = new Business(); 
			Element currentRestarunt = restarunts.get(i).getElementsByClass("biz-listing-large").get(0);
			String rName = currentRestarunt.getElementsByClass("biz-name").text();
			restaruntObj.setName(rName);
			String rurl = currentRestarunt.getElementsByClass("biz-name").attr("href");
			restaruntObj.setUrl(rurl);
			String rstreet = currentRestarunt.getElementsByClass("neighborhood-str-list").text();
			restaruntObj.setStreet(rstreet);
			String rAddress =  currentRestarunt.getElementsByClass("secondary-attributes").get(0).getElementsByTag("address").text();
			restaruntObj.setStreet(rAddress);
			//System.out.println(restaruntObj.Name+ (++sampleCount));
			loadRestaruntDetails(city, restaruntObj);
		}
	}
	//		Elements locationListElements = doc.getElementsByClass("biz-shim");
	//		for(int i=0; i<locationListElements.size(); i++){
	//			Element currentRestarunt = locationListElements.get(i);
	//			Restarunt restarunt = new Restarunt();
	//			restarunt.Name = currentRestarunt.getElementsByTag("a").text();
	//			restarunt.url = currentRestarunt.getElementsByTag("a").attr("href");
	//			restarunts.add(restarunt);
	//			numOfRestarunts++;
	//		}
	return resObjs;		
}


public static String getRedirectionUrl(Document page){
	String RedirectedUrl=null;
	Elements meta = page.select("html head meta");
	if (meta.attr("http-equiv").contains("REFRESH")) {
		RedirectedUrl = meta.attr("content").split("=")[1];
	} else {
		if (page.toString().contains("window.location.href")) {
			meta = page.select("script");
			for (Element script:meta) {
				String s = script.data();
				if (!s.isEmpty() && s.startsWith("window.location.href")) {
					int start = s.indexOf("=");
					int end = s.indexOf(";");
					if (start>0 && end >start) {
						s = s.substring(start+1,end);
						s =s.replace("'", "").replace("\"", "");        
						RedirectedUrl = s.trim();
						break;
					}
				}
			}
		}
	}
	return RedirectedUrl;
}


public static void loadCities() throws Exception {
	Document doc = Jsoup.connect("http://www.yelp.com/locations").timeout(60*1000).get();
	Elements LocationListElements = doc.getElementsByClass("locations-list");
	for(int i =2 ; i <LocationListElements.size(); i++ ) {
		Element currentLocation = LocationListElements.get(i);
		//			System.out.println("Location:");
		//			System.out.println(currentLocation);
		//			System.out.println("----------------------------------------------------------");
		//			System.out.println("----------------------------------------------------------");
		getCities(currentLocation);
	}

	Thread.sleep(5000);
}


public static void  getCities(Element currentRow) throws Exception {
	Elements states = currentRow.getElementsByClass("state");
	for(int i=0; i < states.size(); i++) {
		Element currentState = states.get(i);
		//			System.out.println("State:");
		//			System.out.println(currentState);
		//			System.out.println("----------------------------------------------------------");
		Elements cities = currentState.getElementsByClass("cities");
		for(int k=0; k<cities.size(); k++) {
			Elements currentCities = cities.get(k).getElementsByTag("li");
			for(int j=0; j <currentCities.size(); j++){
				Element	 currentCity = currentCities.get(j);
				String place = currentCity.getElementsByTag("a").text();
				String url = currentCity.getElementsByTag("a").attr("href");
				cityUrl.put(place, url);
			}
		}
	}

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
