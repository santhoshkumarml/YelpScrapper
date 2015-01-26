package programs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import models.Review;
import models.User;
import models.UsrBnssRevws;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import utils.DataReaderUtil;
import utils.GraphUtil;

public class Sample {

	public static void main(String[] args) {
		checkUsrStats();
	}
	
	private static void checkUsrStats() {
		String dirName = "/media/santhosh/Data/workspace/datalab/data/bnss_data";
		long start = System.currentTimeMillis();
		UsrBnssRevws usrBnssRevws = DataReaderUtil.readData(dirName);
		long end = System.currentTimeMillis();
		System.out.println("Data Read:"+((end-start)/1000)+" Seconds");
		
		Set<String> allUserids = usrBnssRevws.getUsers().keySet();
		System.out.println("User count"+ allUserids.size());
		
		List<Integer> reviewCounts = new ArrayList<Integer>();
		
		for(String usrId : allUserids) {
			User usr = usrBnssRevws.getUsers().get(usrId);
			int reviewCount = usr.getReviewCount();
			if(reviewCount == 0) {
				System.out.println(usr.getName()+"->"+usr.getUsrProfileId());
			}
			reviewCounts.add(reviewCount);
		}
		
		Collections.sort(reviewCounts);
		
		System.out.println(reviewCounts);
		
		System.out.println();
	}
	
	private static void checkOverallStats() {
		String dirName = "/media/santhosh/Data/workspace/datalab/data/bnss_data";
		long start = System.currentTimeMillis();
		UsrBnssRevws usrBnssRevws = DataReaderUtil.readData(dirName);
		long end = System.currentTimeMillis();
		System.out.println("Data Read:"+((end-start)/1000)+" Seconds");
		
		start = System.currentTimeMillis();
		UndirectedGraph<String, DefaultEdge> usrReviewGraph = 
				GraphUtil.createGraph(usrBnssRevws);
		end = System.currentTimeMillis();
		System.out.println("Graph Construction:"+((end-start)/1000)+" Seconds");

		start = System.currentTimeMillis();

		Set<String> allUserids = usrBnssRevws.getUsers().keySet();
		Set<String> singletonReviewers = new HashSet<String>();
		Set<String> usersWithAtleastOneRecommendedReviews = new HashSet<String>();

		for(String usrId: allUserids) {
			User user = usrBnssRevws.getUsers().get(usrId);
			if(user.getReviewCount()==1) {
				singletonReviewers.add(usrId);
			}
			Set<DefaultEdge> reviewEdges = usrReviewGraph.edgesOf(usrId);
			for(DefaultEdge reviewEdge : reviewEdges) {
				String sourceUsrId = (String)usrReviewGraph.getEdgeSource(reviewEdge);
				String bnssId = (String)usrReviewGraph.getEdgeTarget(reviewEdge);
				String reviewId = Review.getId(sourceUsrId, bnssId);
				Review review = usrBnssRevws.getReviews().get(reviewId);
				if(review.isRecommended()) {
					usersWithAtleastOneRecommendedReviews.add(usrId);
					break;
				}
			}
		}

		Set<String> usersWithOnlyNotRecommendedReviews = new HashSet<String>();
		usersWithOnlyNotRecommendedReviews.addAll(allUserids);
		usersWithOnlyNotRecommendedReviews.removeAll(usersWithAtleastOneRecommendedReviews);

		Set<String> usersWithMultipleNotRecommendedReviewsAlone = new HashSet<String>();
		usersWithMultipleNotRecommendedReviewsAlone.addAll(usersWithOnlyNotRecommendedReviews);
		usersWithMultipleNotRecommendedReviewsAlone.removeAll(singletonReviewers);

		Set<String> usersWithMultipleMixedReviews = new HashSet<String>();
		usersWithMultipleMixedReviews.addAll(usersWithAtleastOneRecommendedReviews);
		usersWithMultipleMixedReviews.removeAll(singletonReviewers);

		System.out.println("Total Bnss:"+usrBnssRevws.getBusinesses().size());
		
		System.out.println("Total Reviews:"+usrBnssRevws.getReviews().size());
		
		System.out.println("Total Users:"+allUserids.size());
		System.out.println("Singleton Reviewers:"+ singletonReviewers.size());
		System.out.println("Users With Atleast One Recommended "
				+ "Reviews:"+usersWithAtleastOneRecommendedReviews.size());
		System.out.println("Users With Multiple Mixed Reviews"
				+ ":"+usersWithMultipleMixedReviews.size());
		System.out.println("Users With Not Recommended Reviews"
				+ "Alone:"+usersWithOnlyNotRecommendedReviews.size());
		System.out.println("Users With Multiple Not Recommended Reviews"
				+ "Alone:"+usersWithMultipleNotRecommendedReviewsAlone.size());

		end = System.currentTimeMillis();
		System.out.println("Stats Calculation Time:"+((end-start)/1000)+" Seconds");
	}

	private static void checkLocalFileJsoupUser() {
		String dir =  "/home/santhosh/Downloads/user";
		File[] files = new File(dir).listFiles();
		try {
			for(int j=0;j<files.length;j++) {
				Document doc = Jsoup.parse(files[j], "UTF-8");
				Elements reviewList = doc.select("ul.ytype.ylist.ylist-bordered.reviews");
				if(reviewList.size() == 0) {
					System.out.println("Done");
				}
				Elements reviews = reviewList.get(0).getElementsByClass("review");
				if(reviews.size() == 0) {
					System.out.println("Done");
				}
				for(int i =0; i< reviews.size(); i++) {
					Elements metaBnssHref = reviews.get(i).select("a[class=biz-name]");
					String bnssUrl = metaBnssHref.first().attr("href");
					System.out.println(bnssUrl);
				}
			}

		} catch(Exception e) {
			e.printStackTrace();
		}

	}

	private static void checkLocalFileJsoupBnss() {
		int count = 0;
		try {
			String recFile =  "/media/santhosh/Data/workspace/datalab/data/re/augustines-brick-oven-pizzeria-and-cucina-napoletana-hamilton-township/rec/2.html";
			Document doc = Jsoup.parse(new File(recFile), "UTF-8");
			Elements reviewList = doc.getElementsByClass("review-list");
			if(reviewList.size() == 0) {
				System.out.println("Done");
			}
			Elements reviews = reviewList.get(0).getElementsByClass("review");
			if(reviews.size() == 0) {
				System.out.println("Done");
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
				System.out.println(jsonObject);
			}
			System.out.println(count);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}


	private static void checkBnsses() {
		int count = 0;
		try {
			String dirFile = "/media/santhosh/Data/workspace/datalab/data/re";
			File[] bnssDirs= new File(dirFile).listFiles();
			for(int i=0;i<bnssDirs.length;i++) {
				String bnssFileName = bnssDirs[i].getAbsolutePath()+File.separatorChar+"bnss.html";
				String recFolderName = bnssDirs[i].getAbsolutePath()+File.separatorChar+"rec";
				String nonrecFolderName = bnssDirs[i].getAbsolutePath()+File.separatorChar+"non-rec";
				File bnssFile = new File(bnssFileName);
				if(bnssFile.exists()) {
					Document doc = Jsoup.parse(bnssFile, "UTF-8");
				}
			}
			//			Document doc = Jsoup.parse(new File(recFile), "UTF-8");
			//			Elements reviewList = doc.getElementsByClass("review-list");
			//			if(reviewList.size() == 0) {
			//				System.out.println("Done");
			//			}
			//			Elements reviews = reviewList.get(0).getElementsByClass("review");
			//			if(reviews.size() == 0) {
			//				System.out.println("Done");
			//			}
			//			count+= reviews.size();
			//			for(int i =0; i< reviews.size(); i++) {
			//				Elements metaName = reviews.get(i).select("meta[itemprop=author]");
			//				Elements metaRating = reviews.get(i).select("meta[itemprop=ratingValue]");
			//				Elements metaPublished = reviews.get(i).select("meta[itemprop=datePublished]");
			//				String authorName = metaName.first().attr("content").toString();
			//				String rating = metaRating.first().attr("content").toString();
			//				String date =  metaPublished.first().attr("content").toString();
			//				String place = reviews.get(i).getElementsByClass("user-location").text();
			//				String friendCount= reviews.get(i).getElementsByClass("friend-count").text();
			//				String reviewCount = reviews.get(i).getElementsByClass("review-count").text();
			//				String metaComment = reviews.get(i).getElementsByClass("review-content").select("[itemprop=description]").text();
			//				Elements metaDataHoverIdForUsrElements = 
			//						reviews.get(i).getElementsByClass(
			//								"user-name").first().getElementsByClass(
			//										"user-display-name");
			//				String metaDataHoverIdForUsr = "";
			//				if(metaDataHoverIdForUsrElements.isEmpty()){
			//					continue;
			//				} else {
			//					metaDataHoverIdForUsr = metaDataHoverIdForUsrElements.first().attr(
			//							"data-hovercard-id").toString();
			//				}
			//				assert !metaDataHoverIdForUsr.isEmpty();
			//
			//				JSONObject jsonObject = new JSONObject();
			//				jsonObject.put("Name", authorName);
			//				jsonObject.put("usrId", metaDataHoverIdForUsr);
			//				jsonObject.put("Date", date);
			//				jsonObject.put("Rating", rating);
			//				jsonObject.put("Place", place);
			//				jsonObject.put("friendCount", friendCount);
			//				jsonObject.put("reviewCount", reviewCount);
			//				jsonObject.put("ReviewComment", metaComment);
			//				System.out.println(jsonObject);
			//			}
			//			System.out.println(count);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
