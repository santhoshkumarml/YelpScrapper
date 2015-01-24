package programs;

import java.io.File;

import utils.HtmlParserUtil;

public class DataExtractor {

	public static void main(String[] args) {
		String htmlBnssDirName = args[0];
		String bnssOutputDir = args[1];
		try {
			File htmlBnssDirFile = new File(htmlBnssDirName);
			File[] bnssHtmlFiles = htmlBnssDirFile.listFiles();
			for(int i=0;i<bnssHtmlFiles.length;i++) {
				HtmlParserUtil.parseBnssPage(bnssHtmlFiles[i].getAbsolutePath(), bnssOutputDir);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
