package test;

import utils.DataReaderUtil;

public class Replicator {
	public static void main(String[] args) {
		String inputDirName = args[0];
		String outputDirName  = args[1];
		long start = System.currentTimeMillis();
		DataReaderUtil.replicateData(inputDirName, outputDirName);
		long end = System.currentTimeMillis();
		System.out.println("Replicated in:"+((end-start)/1000)+" Seconds");
	}

}
