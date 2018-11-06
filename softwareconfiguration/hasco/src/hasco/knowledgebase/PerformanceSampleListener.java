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
		HASCOSolutionCandidate solutionCandidate = (HASCOSolutionCandidate) event.getSolutionCandidate();
		ComponentInstance ci = solutionCandidate.getComponentInstance();
		List<ComponentInstance> cis = Util.getComponentInstancesOfComposition(ci);
		for(ComponentInstance i : cis) {
//			System.out.println("set expl. " + i.getParametersThatHaveBeenSetExplicitly());
//			System.out.println("not set expl. " + i.getParametersThatHaveNotBeenSetExplicitly());
			System.out.println("params: " + i.getComponent().getParameters() + " vals: " + i.getParameterValues());
		}
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
