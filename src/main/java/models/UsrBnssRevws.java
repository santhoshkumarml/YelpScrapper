package models;

import java.util.HashMap;
import java.util.Map;

public class UsrBnssRevws {
	public Map<String, Business> restaurnts;
	public Map<String, User> users;
	public Map<String, Review> reviews;

	public UsrBnssRevws() {
		this.restaurnts = new HashMap<String, Business>();
		this.users = new HashMap<String, User>();
		this.reviews = new HashMap<String, Review>();
	}

	public Map<String, Business> getBusinesses() {
		return restaurnts;
	}

	public Map<String, User> getUsers() {
		return users;
	}

	public Map<String, Review> getReviews() {
		return reviews;
	}
}