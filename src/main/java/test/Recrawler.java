package test;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import models.Business;
import models.Review;
import models.User;
import models.UsrBnssRevws;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import utils.DataReaderUtil;
import utils.GraphUtil;

public class Recrawler {
	private static String DIR_NAME = "";

	public static void main(String[] args) {
		String dirName = args[0];
		DIR_NAME = args[1];
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

		Set<String> tobeReCrawledMixedReviewers = 
				new HashSet<String>(usersWithMultipleMixedReviews);
		Set<String> tobeReCrawledNotRecommendedReviewers =
				new HashSet<String>(usersWithMultipleNotRecommendedReviewsAlone);
		Set<String> crawledBusinesses = usrBnssRevws.getBusinesses().keySet();
		Set<String> peripheralBusinesses = new HashSet<String>();

		recrawlCrawledBusinessPages(usrBnssRevws, crawledBusinesses);

		//Map<String,Set<String>> 
		Map<String,Set<String>> peripheralBnssUsrSetMap = 
				getPeripheralBnssUsrSetMap(usrBnssRevws, tobeReCrawledMixedReviewers,
						tobeReCrawledNotRecommendedReviewers,
						crawledBusinesses, peripheralBusinesses);
	}

	public static Map<String,Set<String>> getPeripheralBnssUsrSetMap(
			UsrBnssRevws usrBnssRevws, 
			Set<String> tobeReCrawledMixedReviewers, 
			Set<String> tobeReCrawledNotRecommendedReviewers, 
			Set<String> crawledBusinesses, 
			Set<String> peripheralBusinesses) {
		return Collections.EMPTY_MAP;

	}

	private static void recrawlCrawledBusinessPages(UsrBnssRevws usrBnssRevws, Set<String> crawledbusinesses) {
		int count = 0;
		for(String bnssId : crawledbusinesses) {
			count++;
			Business bnss = usrBnssRevws.getBusinesses().get(bnssId);
			String bnssUrl = bnss.getUrl().replace("/biz/", "");
			String fileName =  DIR_NAME+File.separatorChar+bnssUrl+
					File.separatorChar+"bnss.html";
			File file = new File(fileName);
			if(file.exists()) {
				continue;
			}
			try {
				crawlBusinessPage(bnss);
				crawlNotRecReviewPages(bnss);
				crawlRecReviewPages(bnss);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void crawlBusinessPage(Business bnss) throws Exception{
		String url =  "http://www.yelp.com";
		url += bnss.getUrl();
		Document doc = Jsoup.connect(url).timeout(60*1000).get();
		Thread.sleep(1000);
		String bnssUrl = bnss.getUrl().replace("/biz/", "");
		String fileName =  DIR_NAME+File.separatorChar+bnssUrl+
				File.separatorChar+"bnss.html";

		File file = new File(fileName);
		File parentFile = file.getParentFile();
		if(!parentFile.exists()) {
			parentFile.mkdirs();
		}
		PrintWriter writer = new PrintWriter(file, "UTF-8");
		writer.println(doc.outerHtml());
		writer.flush();
		writer.close();
	}

	public static void crawlRecReviewPages(Business bnss) throws Exception {
		int count = 0;
		while(Boolean.TRUE) {
			String url =  "http://www.yelp.com";
			//Document doc  = null;
			url += bnss.getUrl()+ "?sort_by=date_desc&start=" + count;

			Document doc = Jsoup.connect(url).timeout(60*1000).get();

			//String htmlPage = NYCYelpScrapper.getHtmlPage(url);
			Thread.sleep(1000);

			//doc = Jsoup.parse(htmlPage);

			Elements reviewList = doc.getElementsByClass("review-list");
			if(reviewList.size() == 0) {
				return;
			}
			Elements reviews = reviewList.get(0).getElementsByClass("review");
			if(reviews.size() == 0) {
				return;
			}

			count+=reviews.size();

			String bnssUrl = bnss.getUrl().replace("/biz/", "");
			String recFileName =  DIR_NAME+File.separatorChar+bnssUrl+
					File.separatorChar+"rec"+File.separatorChar+count+".html";

			File recFile = new File(recFileName);
			File parentFile = recFile.getParentFile();
			if(!parentFile.exists()) {
				parentFile.mkdirs();
			}

			PrintWriter writer = new PrintWriter(recFile, "UTF-8");
			writer.println(doc.outerHtml());
			writer.flush();
			writer.close();
		}
	}

	public static void crawlNotRecReviewPages(Business bnss) throws Exception {
		int count = 0;
		while(Boolean.TRUE) {
			String url =  "http://www.yelp.com/not_recommended_reviews";
			url += bnss.getUrl() +  "?not_recommended_start=" + count;
			url = url.replace("/biz","");
			//Document doc = null;
			Document doc = Jsoup.connect(url).timeout(60*1000).get();
			//String htmlPage = NYCYelpScrapper.getHtmlPage(url);
			//doc = Jsoup.parse(htmlPage);

			Thread.sleep(1000);

			Elements reviewList = doc.getElementsByClass("not-recommended-reviews");
			if(reviewList.size() == 0) {
				return;
			}
			Elements reviews = reviewList.get(0).getElementsByClass("review");

			if(reviews.size() == 0) {
				return;
			}

			count+=reviews.size();

			String bnssUrl = bnss.getUrl().replace("/biz/", "");
			String nonrecFileName =  DIR_NAME+File.separatorChar+bnssUrl+
					File.separatorChar+"non-rec"+File.separatorChar+count+".html";


			File nonrecFile = new File(nonrecFileName);
			File parentFile = nonrecFile.getParentFile();

			if(!parentFile.exists()) {
				parentFile.mkdirs();
			}

			PrintWriter writer = new PrintWriter(nonrecFile, "UTF-8");
			writer.println(doc.outerHtml());
			writer.flush();
			writer.close();


		}
	}
}