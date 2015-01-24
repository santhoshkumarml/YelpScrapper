package utils;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlParserUtil {

	public static final String REVIEW_COMMENT = "ReviewComment";
	public static final String USR_REVIEW_COUNT = "reviewCount";
	public static final String USR_FRIEND_COUNT = "friendCount";
	public static final String USR_LOCATION = "Place";
	public static final String REVIEW_RATING = "Rating";
	public static final String REVIEW_DATE = "Date";
	public static final String USR_ID = "usrId";
	public static final String USR_NAME = "Name";
	public static final String USR_PROFILE_ID = "usrProfileId";

	public static final String BNSS_NAME = "BnssName";
	public static final String BNSS_ADDRESS = "Address";
	public static final String BNSS_URL_SUBSTRING = "BnssUrl";
	public static final String BNSS_RATING = "BnssRating";
	public static final String IS_CORE_BNSS = "isCoreBnss";


	public static final String recommendedJSONReviewConstant = "Recommended";
	public static final String nonrecommendedJSONReviewConstant = "NotRecommended";

	public static void parseBnssPage(String htmlBnssDirName, String dataBnssDirName) throws Exception {

		String bnssHtmlFileName = htmlBnssDirName+File.separatorChar+"bnss.html";

		File bnssHtmlFile = new File(bnssHtmlFileName);

		File dataBnssFile = new File(
				dataBnssDirName+File.separatorChar+
				bnssHtmlFile.getParentFile().getName()+".txt");
		if(dataBnssFile.exists()) {
			return;
		}

		Document doc = Jsoup.parse(bnssHtmlFile, "UTF-8");

		Element bnssStory = doc.getElementsByClass(
				"contentbox").first().getElementsByClass(
						"ending-point").first().getElementsByClass("media-story").first();

		Element bnssLink = bnssStory.getElementsByClass("biz-name").first();
		String bnssUrl = bnssLink.attr("href");
		bnssUrl.replace("/biz/", "");

		String bnssName = bnssLink.text();
		String address = bnssStory.getElementsByTag("address").first().text();

		String parentName = new File(htmlBnssDirName).getParentFile().getName();

		boolean isCoreBnss = false;

		if(parentName.contains("core")) {
			isCoreBnss = true;
		}

		List<JSONObject> reccomendedReviews = parseRecPages(htmlBnssDirName);
		List<JSONObject> nonReccomendedReviews = parseNonRecPages(htmlBnssDirName);

		JSONObject business = new JSONObject();

		business.put(BNSS_NAME, bnssName);
		business.put(BNSS_ADDRESS, address);
		business.put(BNSS_URL_SUBSTRING, bnssUrl);
		business.put(IS_CORE_BNSS, new Boolean(isCoreBnss).toString());

		JSONArray reccomendedReviewArray = new JSONArray();
		JSONArray nonReccomendedReviewArray = new JSONArray();

		for(JSONObject reccomended :reccomendedReviews ) {
			reccomendedReviewArray.add(reccomended);
		}
		for(JSONObject nonReccomended :nonReccomendedReviews ) {
			nonReccomendedReviewArray.add(nonReccomended);
		}

		business.put(recommendedJSONReviewConstant, reccomendedReviewArray);
		business.put(nonrecommendedJSONReviewConstant, nonReccomendedReviewArray);

		File parentFile = dataBnssFile.getParentFile();

		if(!parentFile.exists()) {
			parentFile.mkdirs();
		}

		PrintWriter writer = new PrintWriter(dataBnssFile, "UTF-8");

		writer.println(business.toJSONString());
		writer.flush();
		writer.close();
	}

	public static List<JSONObject> parseRecPages(String htmlBnssDirName) throws Exception {
		List<JSONObject> recommendedReviews = new ArrayList<JSONObject>();

		File recFolder = new File(htmlBnssDirName+File.separatorChar+
				CrawlerUtil.RECOMMENDED_REVIEWS_FOLDER);
		if(!recFolder.exists()) {
			return recommendedReviews;	
		}
		File[] reviewFiles = recFolder.listFiles();
		for(int i=0;i<reviewFiles.length;i++) {
			Document doc = Jsoup.parse(reviewFiles[i], "UTF-8");
			Elements reviewList = doc.getElementsByClass("review-list");
			Elements reviews = reviewList.get(0).getElementsByClass("review");
			for(int j=0;j<reviews.size();j++) {
				Elements usrDisplay = 
						reviews.get(i).getElementsByClass(
								"user-name").first().getElementsByClass(
										"user-display-name");

				if(usrDisplay.isEmpty()){
					continue;
				}

				Elements metaRating = reviews.get(i).select("meta[itemprop=ratingValue]");
				Elements metaPublished = reviews.get(i).select("meta[itemprop=datePublished]");

				String rating = metaRating.first().attr("content").toString();
				String date =  metaPublished.first().attr("content").toString();
				String place = reviews.get(i).getElementsByClass("user-location").text();
				String friendCount= reviews.get(i).getElementsByClass("friend-count").text();
				String reviewCount = reviews.get(i).getElementsByClass("review-count").text();
				String metaComment = reviews.get(i).getElementsByClass("review-content").select("[itemprop=description]").text();

				String metaDataHoverIdForUsr = usrDisplay.first().attr(
						"data-hovercard-id").toString();
				String authorName =  usrDisplay.first().text();
				String usrProfileId = usrDisplay.first().attr("href");
				usrProfileId.replace("/user_details?userid=", "");

				assert !metaDataHoverIdForUsr.isEmpty();

				JSONObject jsonObject = new JSONObject();
				jsonObject.put(USR_NAME, authorName);
				jsonObject.put(USR_ID, metaDataHoverIdForUsr);
				jsonObject.put(USR_PROFILE_ID, usrProfileId);
				jsonObject.put(REVIEW_DATE, date);
				jsonObject.put(REVIEW_RATING, rating);
				jsonObject.put(USR_LOCATION, place);
				jsonObject.put(USR_FRIEND_COUNT, friendCount);
				jsonObject.put(USR_REVIEW_COUNT, reviewCount);
				jsonObject.put(REVIEW_COMMENT, metaComment);
				recommendedReviews.add(jsonObject);
			}
		}

		return recommendedReviews;

	}

	public static List<JSONObject> parseNonRecPages(String htmlBnssDirName) throws Exception{

		List<JSONObject> notRecommendedReviews = new ArrayList<JSONObject>();

		File nonRecFolder = new File(htmlBnssDirName+File.separatorChar+
				CrawlerUtil.NON_RECOMMENDED_REVIEWS_FOLDER);
		if(!nonRecFolder.exists()) {
			return notRecommendedReviews;
		}

		File[] reviewFiles = nonRecFolder.listFiles();

		for(int i=0;i<reviewFiles.length;i++) {

			Document doc = Jsoup.parse(reviewFiles[i], "UTF-8");

			Elements reviewList = doc.getElementsByClass("not-recommended-reviews");
			Elements reviews = reviewList.get(0).getElementsByClass("review");
			for(int j=0;j<reviews.size();j++) {
				Elements usrDisplay = 
						reviews.get(i).getElementsByClass(
								"user-name").first().getElementsByClass(
										"user-display-name");

				if(usrDisplay.isEmpty()){
					continue;
				}

				String rating = reviews.get(i).getElementsByClass(
						"rating-very-large").get(0).getElementsByTag("i").attr("title").substring(0,3);
				String date = reviews.get(i).getElementsByClass("rating-qualifier").text();
				String place = reviews.get(i).getElementsByClass("user-location").text();
				String friendCount= reviews.get(i).getElementsByClass("friend-count").text();
				String reviewCount = reviews.get(i).getElementsByClass("review-count").text();
				String reviewComment = reviews.get(i).getElementsByClass(
						"review-content").get(0).getElementsByTag("p").text();


				String metaDataHoverIdForUsr = usrDisplay.first().attr(
						"data-hovercard-id").toString();
				String authorName =  usrDisplay.first().text();
				//				String usrProfileId = usrDisplay.first().attr("href");
				//				usrProfileId.replaceAll("/user_details?userid=", "");

				assert !metaDataHoverIdForUsr.isEmpty();

				JSONObject jsonObject = new JSONObject();
				jsonObject.put(USR_NAME, authorName);
				jsonObject.put(USR_ID, metaDataHoverIdForUsr);
				//jsonObject.put(USR_PROFILE_ID, usrProfileId);
				jsonObject.put(REVIEW_DATE, date);
				jsonObject.put(REVIEW_RATING, rating);
				jsonObject.put(USR_LOCATION, place);
				jsonObject.put(USR_FRIEND_COUNT, friendCount);
				jsonObject.put(USR_REVIEW_COUNT, reviewCount);
				jsonObject.put(REVIEW_COMMENT, reviewComment);
				notRecommendedReviews.add(jsonObject);
			}
		}

		return notRecommendedReviews;
	}
}
