package hasco.knowledgebase;


import com.google.common.eventbus.Subscribe;

import hasco.core.HASCOSolutionCandidate;
import hasco.events.HASCOSolutionEvent;
import hasco.model.ComponentInstance;

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
	}

	@Subscribe
	public void handleEvent(HASCOSolutionEvent event) {
		HASCOSolutionCandidate solutionCandidate = (HASCOSolutionCandidate) event.getSolutionCandidate();
		ComponentInstance ci = solutionCandidate.getComponentInstance();
		double score = (Double) solutionCandidate.getScore();
		performanceKnowledgeBase.addPerformanceSample(benchmarkName, ci, score, false);
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
