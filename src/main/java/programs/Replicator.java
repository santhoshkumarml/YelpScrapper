package programs;

import java.util.ArrayList;
import java.util.List;

import utils.DataReaderUtil;
import utils.DataReaderUtil.FilterCriteria;
import utils.HtmlParserUtil;

public class Replicator {
	public static void main(String[] args) {
		String inputDirName = args[0];
		String outputDirName  = args[1];
		long start = System.currentTimeMillis();
		FilterCriteria criteria = new FilterCriteria();
		criteria.setAttribute(HtmlParserUtil.BNSS_ADDRESS);
		criteria.setValue(".*[Nn]ew[ ]+[Yy]ork.*[Nn][Yy].*");
		List<FilterCriteria> filterCriterias = new ArrayList<FilterCriteria>();
		filterCriterias.add(criteria);
		DataReaderUtil.replicateData(inputDirName, outputDirName, filterCriterias);
		long end = System.currentTimeMillis();
		System.out.println("Replicated in:"+((end-start)/1000)+" Seconds");
	}

}
