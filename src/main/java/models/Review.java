package models;


public class Review {
	
	public static final String REVIEW_DELIMITER = "::";
	
	
	String reviewComment;
	String usrId;
	String bnssId;
	float rating;
	String date;
	boolean isRecommended;	
	
	public String getReviewComment() {
		return reviewComment;
	}
	public void setReviewComment(String reviewComment) {
		this.reviewComment = reviewComment;
	}
	public String getUsrId() {
		return usrId;
	}
	public void setUsrId(String usrId) {
		this.usrId = usrId;
	}
	public String getBnssId() {
		return bnssId;
	}
	public void setBnssId(String bnssId) {
		this.bnssId = bnssId;
	}
	public float getRating() {
		return rating;
	}
	public void setRating(float rating) {
		this.rating = rating;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	
	public boolean isRecommended() {
		return isRecommended;
	}
	public void setRecommended(boolean isRecommended) {
		this.isRecommended = isRecommended;
	}
	
	public static String getId(Review review) {
		return review.getUsrId()+REVIEW_DELIMITER+review.getBnssId();
	}
	
	public static String getId(String usrId, String bnssId) {
		return usrId+REVIEW_DELIMITER+bnssId;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bnssId == null) ? 0 : bnssId.hashCode());
		result = prime * result + ((usrId == null) ? 0 : usrId.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Review other = (Review) obj;
		if (bnssId == null) {
			if (other.bnssId != null)
				return false;
		} else if (!bnssId.equals(other.bnssId))
			return false;
		if (usrId == null) {
			if (other.usrId != null)
				return false;
		} else if (!usrId.equals(other.usrId))
			return false;
		return true;
	}
	
	

}
