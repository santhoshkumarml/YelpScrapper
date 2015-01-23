package utils;

import models.Review;
import models.UsrBnssRevws;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class GraphUtil {

	public static UndirectedGraph<String, DefaultEdge> createGraph(UsrBnssRevws usrBnssRevws) {
		UndirectedGraph<String, DefaultEdge> usrReviewGraph = new SimpleGraph<String,DefaultEdge>(DefaultEdge.class);
		for(String key: usrBnssRevws.getReviews().keySet()) {
			String[] usrBnss = key.split(Review.REVIEW_DELIMITER);
			String usrId = usrBnss[0];
			String bnssId = usrBnss[1];
			usrReviewGraph.addVertex(usrId);
			usrReviewGraph.addVertex(bnssId);
			usrReviewGraph.addEdge(usrId, bnssId);
		}
		return usrReviewGraph;
	}

}
