package hasco.knowledgebase;

import org.w3c.dom.events.Event;

import com.google.common.eventbus.Subscribe;

import hasco.core.HASCOSolutionCandidate;
import hasco.core.Util;
import hasco.events.HASCOSolutionEvent;
import hasco.model.ComponentInstance;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.SolutionCandidateFoundEvent;
import jaicore.search.model.other.EvaluatedSearchGraphPath;

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
