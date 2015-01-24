package programs;

import yelpApi.YelpAPI;

public class YelpAPITest {
	private static final String CONSUMER_KEY = "GkXYGCDRhA8vNpBcNASQkg";
	private static final String CONSUMER_SECRET = "IOjx8hFjbnJ98O5SFtQ5KHuMdck";
	private static final String TOKEN = "uh9nvP4TuBDS9hOnqU1HwMWjuUsnv1Fm";
	private static final String TOKEN_SECRET = "tWs6x5JL_yPeyTHJrrGJpvWXV7I";
	public static void main(String args[]) {
		YelpAPI yelpApi = new YelpAPI(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, TOKEN_SECRET);
		String businessID = "boho-cafe-white-river-junction";
		String response = yelpApi.searchByBusinessId(businessID);
		System.out.println(response);
	}
}
