package utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import models.Business;
import models.Review;
import models.User;
import models.UsrBnssRevws;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DataReaderUtil {

	public static void replicateData(String inputDirName, String outputDirName) {
		File baseDir = new File(inputDirName);
		if (baseDir.exists() && baseDir.isDirectory()) {
			File[] zipDirs = baseDir.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.isDirectory();
				}
			});
			List<String> restaruntFileNames = new ArrayList<String>();
			for(int i=0;i<zipDirs.length;i++) {
				File[] restaruntFileNamesForZip = zipDirs[i].listFiles();
				for(int j=0;j<restaruntFileNamesForZip.length;j++) {
					restaruntFileNames.add(restaruntFileNamesForZip[j].getAbsolutePath());
				}
			}
			try {
				for(String fileName : restaruntFileNames) {
					FileReader reader = new FileReader(fileName);
					JSONParser jsonParser = new JSONParser();
					JSONObject businessObject = (JSONObject) jsonParser.parse(reader);

					businessObject.put("isCoreBnss", "True");

					String outputFileName = outputDirName+fileName.split(inputDirName)[1];

					File outFile = new File(outputFileName);
					File outFileDir = outFile.getParentFile();

					if(!outFileDir.exists()) {
						outFileDir.mkdirs();						
					}

					PrintWriter writer = new PrintWriter(outFile, "UTF-8");
					writer.println(businessObject.toJSONString());
					writer.flush();
					writer.close();
				}
			} catch(IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

	}


	public static UsrBnssRevws readData(String dirName) {
		UsrBnssRevws usrBnssRevws = new UsrBnssRevws();	
		File baseDir = new File(dirName);
		if (baseDir.exists() && baseDir.isDirectory()) {
			File[] zipDirs = baseDir.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.isDirectory();
				}
			});
			List<String> restaruntFileNames = new ArrayList<String>();
			for(int i=0;i<zipDirs.length;i++) {
				File[] restaruntFileNamesForZip = zipDirs[i].listFiles();
				for(int j=0;j<restaruntFileNamesForZip.length;j++) {
					restaruntFileNames.add(restaruntFileNamesForZip[j].getAbsolutePath());
				}
			}
			for(String restaruntFileName : restaruntFileNames) {
				readDataForRestarunt(restaruntFileName, usrBnssRevws);
			}
		}
		return usrBnssRevws;
	}

	private static void readDataForRestarunt(String fileName, UsrBnssRevws usrBnssRevws) {
		try {
			// read the json file
			FileReader reader = new FileReader(fileName);
			JSONParser jsonParser = new JSONParser();
			JSONObject businessObject = (JSONObject) jsonParser.parse(reader);
			String addr = (String)businessObject.get(HtmlParserUtil.BNSS_ADDRESS);
			String url = (String)businessObject.get(HtmlParserUtil.BNSS_URL_SUBSTRING);
			String bnssName = (String)businessObject.get(HtmlParserUtil.BNSS_NAME);
			boolean isCoreBnss = Boolean.parseBoolean((String)businessObject.get(HtmlParserUtil.IS_CORE_BNSS));

			Business bnss = new Business();
			bnss.setAddress(addr);
			bnss.setName(bnssName);
			bnss.setUrl(url);
			bnss.setCoreBnss(isCoreBnss);

			if (usrBnssRevws.getBusinesses().containsKey(Business.getId(bnss))) {
				return;
			}
			usrBnssRevws.getBusinesses().put(Business.getId(bnss), bnss);
			readReviews(businessObject, bnss, usrBnssRevws);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(0);
		}  catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void readReviews(JSONObject businessObject, Business restarunt, UsrBnssRevws usrBnssRevws) {
		JSONArray recommendedReviews = (JSONArray) businessObject.get("Reccomended");
		JSONArray notRecommendedReviews = (JSONArray) businessObject.get("nonReccomended");
		if(recommendedReviews.size() > 0) {
			Iterator<JSONObject> recIterator = recommendedReviews.iterator();
			while(recIterator.hasNext()) {
				JSONObject reviewObject = recIterator.next(); 
				User usr = new User();
				String name = (String) reviewObject.get(HtmlParserUtil.USR_NAME);
				String usrId = (String) reviewObject.get(HtmlParserUtil.USR_ID);
				String usrProfileId = (String) reviewObject.get(HtmlParserUtil.USR_PROFILE_ID);
				String location = (String) reviewObject.get(HtmlParserUtil.USR_LOCATION);

				String friendCountString = (String) reviewObject.get(HtmlParserUtil.USR_FRIEND_COUNT);
				String reviewCountString = (String) reviewObject.get(HtmlParserUtil.USR_REVIEW_COUNT);
				int friendCount = Integer.parseInt((friendCountString.split("[ ]+")[0]).trim());
				int reviewCount = Integer.parseInt((reviewCountString.split("[ ]+")[0]).trim());

				usr.setName(name);
				usr.setLocation(location);
				if(usrProfileId != null) {
					usr.setUsrProfileId(usrProfileId);
				}
				usr.setUsrId(usrId);
				usr.setFriendCount(friendCount);
				usr.setReviewCount(reviewCount);

				Review review = new Review();
				review.setUsrId(usrId);
				review.setBnssId(restarunt.getUrl()+","+restarunt.getAddress());

				String reviewComment = (String)reviewObject.get(HtmlParserUtil.REVIEW_COMMENT);
				String ratingString =  (String)reviewObject.get(HtmlParserUtil.REVIEW_RATING);
				float rating = Float.parseFloat(ratingString);
				String reviewDate = (String)reviewObject.get(HtmlParserUtil.REVIEW_DATE);

				review.setRating(rating);
				review.setReviewComment(reviewComment);
				review.setRecommended(true);
				review.setDate(reviewDate);

				if(!usrBnssRevws.getUsers().containsKey(usr.getUsrId())) {
					usrBnssRevws.getUsers().put(usrId, usr);
				}

				usrBnssRevws.getReviews().put(Review.getId(review), review);

			}
		}

		if(notRecommendedReviews.size() > 0) {
			Iterator<JSONObject> nonRecIterator = notRecommendedReviews.iterator();
			while(nonRecIterator.hasNext()) {
				JSONObject reviewObject = nonRecIterator.next();
				User usr = new User();
				String name = (String) reviewObject.get(HtmlParserUtil.USR_NAME);
				String usrId = (String) reviewObject.get(HtmlParserUtil.USR_ID);
				String location = (String) reviewObject.get(HtmlParserUtil.USR_LOCATION);

				String friendCountString = (String) reviewObject.get(HtmlParserUtil.USR_FRIEND_COUNT);
				String reviewCountString = (String) reviewObject.get(HtmlParserUtil.USR_REVIEW_COUNT);
				int friendCount = Integer.parseInt((friendCountString.split("[ ]+")[0]).trim());
				int reviewCount = Integer.parseInt((reviewCountString.split("[ ]+")[0]).trim());

				usr.setName(name);
				usr.setLocation(location);
				usr.setUsrId(usrId);
				usr.setFriendCount(friendCount);
				usr.setReviewCount(reviewCount);

				Review review = new Review();
				review.setUsrId(usrId);
				review.setBnssId(Business.getId(restarunt));

				String reviewComment = (String)reviewObject.get(HtmlParserUtil.REVIEW_COMMENT);
				String ratingString =  (String)reviewObject.get(HtmlParserUtil.REVIEW_RATING);
				float rating = Float.parseFloat(ratingString);

				String reviewDate = (String)reviewObject.get(HtmlParserUtil.REVIEW_DATE);

				review.setRating(rating);
				review.setReviewComment(reviewComment);
				review.setRecommended(false);
				review.setDate(reviewDate);

				if(!usrBnssRevws.getUsers().containsKey(usr.getUsrId())) {
					usrBnssRevws.getUsers().put(usrId, usr);
				}

				usrBnssRevws.getReviews().put(Review.getId(review), review);
			}

		}
	}
}
