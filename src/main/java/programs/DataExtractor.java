package programs;

import utils.HtmlParserUtil;

public class DataExtractor {

	public static void main(String[] args) {
		String htmlBnssDirName = args[0];
		String bnssOutputDir = args[1];
		try {
			HtmlParserUtil.parseBnssPage(htmlBnssDirName, bnssOutputDir);	
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
