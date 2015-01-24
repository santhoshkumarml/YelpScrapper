package utils;

import java.io.File;
import java.io.PrintWriter;

import models.Business;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class CrawlerUtil {

	public static void crawlBusinessPage(Business bnss, String dirName) throws Exception{
		String url =  "http://www.yelp.com";
		url += bnss.getUrl();
		Document doc = Jsoup.connect(url).timeout(60*1000).get();
		Thread.sleep(1000);
		String bnssUrl = dirName+File.separatorChar+bnss.getUrl().replace("/biz/", "");
		String fileName = bnssUrl+
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

	public static void crawlNotRecReviewPages(Business bnss, String dirName) throws Exception {
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
			String nonrecFileName =  dirName+File.separatorChar+bnssUrl+
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

	public static void crawlRecReviewPages(Business bnss, String dirName) throws Exception {
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
			String recFileName =  dirName+File.separatorChar+bnssUrl+
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

}
