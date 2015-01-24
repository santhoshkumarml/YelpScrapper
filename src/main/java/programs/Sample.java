package programs;

import java.io.File;

import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Sample {

	public static void main(String[] args) {
		checkLocalFileJsoupUser();
		checkLocalFileJsoupBnss();
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
