package hasco.knowledgebase;

import java.util.Comparator;
import java.util.HashMap;

import hasco.model.Component;
import jaicore.basic.SQLAdapter;

public class FANOVAWarmstartComparator implements Comparator {

	private HashMap<String, HashMap<String, HashMap<String, Double>>> importanceValues;
	private SQLAdapter adapter;
	
	@Override
	public int compare(Object o1, Object o2) {
		return 0;
	}
	
	public void loadImportanceValuesForComponent(Component component, String benchmarkName) {
		
	}

}
