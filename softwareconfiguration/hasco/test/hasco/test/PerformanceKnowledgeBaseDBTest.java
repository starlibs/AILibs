package hasco.test;

import static org.junit.Assert.*;

import org.junit.Test;

import hasco.knowledgebase.PerformanceKnowledgeBase;
import jaicore.basic.SQLAdapter;

public class PerformanceKnowledgeBaseDBTest {
	
	

	@Test
	public void test() {
		SQLAdapter adapter = new SQLAdapter("isys-db.cs.uni-paderborn.de", "pgotfml", "automl2018", "pgotfml_jmhansel");
		PerformanceKnowledgeBase pKB = new PerformanceKnowledgeBase(adapter);
//		PerformanceKnowledgeBase pKB = new PerformanceKnowledgeBase();
//		pKB.loadPerformanceSamplesFromDB();
		pKB.initializeDBTables();
		System.out.println(pKB.getPerformanceSamples());
	}

}
