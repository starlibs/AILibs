package hasco.knowledgebase;


import java.util.List;

import com.google.common.eventbus.Subscribe;

import hasco.core.HASCOSolutionCandidate;
import hasco.core.Util;
import hasco.events.HASCOSolutionEvent;
import hasco.model.ComponentInstance;
import jaicore.planning.EvaluatedPlan;

/**
 * 
 * @author jmhansel
 *
 */
public class PerformanceSampleListener {

	private PerformanceKnowledgeBase performanceKnowledgeBase;
	private String benchmarkName;

	public PerformanceSampleListener(PerformanceKnowledgeBase perfromanceKnowledgeBase, String benchmarkName) {
		this.performanceKnowledgeBase = perfromanceKnowledgeBase;
		this.benchmarkName = benchmarkName;
	}

	@Subscribe
	public void handleEvent(HASCOSolutionEvent event) {
		if(event.getSolutionCandidate() instanceof HASCOSolutionCandidate) {
		HASCOSolutionCandidate solutionCandidate = (HASCOSolutionCandidate) event.getSolutionCandidate();
		System.out.println("SCore sol can: " + solutionCandidate.getScore());
		ComponentInstance ci = solutionCandidate.getComponentInstance();
		double score = (Double) solutionCandidate.getScore();
		performanceKnowledgeBase.addPerformanceSample(benchmarkName, ci, score, false);
		}
	}

	public PerformanceKnowledgeBase getPerformanceKnowledgeBase() {
		return performanceKnowledgeBase;
	}

	public void setPerformanceKnowledgeBase(PerformanceKnowledgeBase performanceKnowledgeBase) {
		this.performanceKnowledgeBase = performanceKnowledgeBase;
	}

	public String getBenchmarkName() {
		return benchmarkName;
	}

	public void setBenchmarkName(String benchmarkName) {
		this.benchmarkName = benchmarkName;
	}

}
