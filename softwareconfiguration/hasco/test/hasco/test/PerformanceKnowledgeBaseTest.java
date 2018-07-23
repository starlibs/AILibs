package hasco.test;

import static org.junit.Assert.*;

import org.junit.Test;

import hasco.knowledgebase.PerformanceKnowledgeBase;
import jaicore.basic.SQLAdapter;

public class PerformanceKnowledgeBaseTest {

	
	@Test
	public void test() {
		SQLAdapter adapter = new SQLAdapter("localhost", "jonas", "password", "mlplan_test");
		PerformanceKnowledgeBase pKB = new PerformanceKnowledgeBase(adapter);
		pKB.loadPerformanceSamplesFromDB();
		System.out.println("normal: \n" + pKB.getPerformanceSamples());
		System.out.println("\n\n by identifier: \n" + pKB.getPerformanceSamplesByIdentifier());
	
	}

}
