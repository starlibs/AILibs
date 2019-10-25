package ai.libs.hasco.gui.statsplugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.model.UnparametrizedComponentInstance;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginModel;
import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.ScoredSolutionCandidateInfo;

/**
 * 
 * @author fmohr
 * 
 *         Holds all the information to supply the HASCOModelStatisticsPluginView with what it needs.
 */
public class HASCOModelStatisticsPluginModel extends ASimpleMVCPluginModel<HASCOModelStatisticsPluginView, HASCOModelStatisticsPluginController> {

	private static final Logger LOGGER = LoggerFactory.getLogger(HASCOModelStatisticsPluginModel.class);

	private ComponentInstanceSerializer componentInstanceSerializer = new ComponentInstanceSerializer();

	private final Map<UnparametrizedComponentInstance, List<ScoredSolutionCandidateInfo>> observedSolutionsGroupedModuloParameters = new HashMap<>();
	private final Map<String, Component> knownComponents = new HashMap<>();

	/**
	 * Informs the plugin about a new HASCOSolution. This solution will be considered in the combo boxes as well as in the histogram.
	 * 
	 * @param solutionEvent
	 */
	public final void addEntry(ScoredSolutionCandidateInfo scoredSolutionCandidateInfo) {
		ComponentInstance ci = deserializeComponentInstance(scoredSolutionCandidateInfo.getSolutionCandidateRepresentation());
		if (ci == null) {
			return;
		}
		UnparametrizedComponentInstance uci = new UnparametrizedComponentInstance(ci);
		if (!observedSolutionsGroupedModuloParameters.containsKey(uci)) {
			observedSolutionsGroupedModuloParameters.put(uci, new ArrayList<>());
		}
		observedSolutionsGroupedModuloParameters.get(uci).add(scoredSolutionCandidateInfo);
		ci.getContainedComponents().forEach(c -> {
			if (!knownComponents.containsKey(c.getName())) {
				knownComponents.put(c.getName(), c);
			}
		});
		getView().update();
	}

	/**
	 * Gets an (unordered) collection of the solutions received so far.
	 * 
	 * @return Collection of solutions.
	 */
	public Collection<ScoredSolutionCandidateInfo> getAllSeenSolutionCandidateFoundInfosUnordered() {
		List<ScoredSolutionCandidateInfo> solutionEvents = new ArrayList<>();
		observedSolutionsGroupedModuloParameters.values().forEach(l -> solutionEvents.addAll(l));
		return solutionEvents;
	}

	/**
	 * @return A map that assigns, for each known component, its name to the Component object.
	 */
	public Map<String, Component> getKnownComponents() {
		return knownComponents;
	}

	/**
	 * 
	 * @param composition
	 * @return
	 */
	public DescriptiveStatistics getPerformanceStatisticsForComposition(UnparametrizedComponentInstance composition) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		observedSolutionsGroupedModuloParameters.get(composition).forEach(e -> stats.addValue(parseScoreToDouble(e.getScore())));
		return stats;
	}

	/**
	 * Clears the model (and subsequently the view)
	 */
	@Override
	public void clear() {
		observedSolutionsGroupedModuloParameters.clear();
		knownComponents.clear();
		getView().clear();
	}

	public ComponentInstance deserializeComponentInstance(String serializedComponentInstance) {
		try {
			return componentInstanceSerializer.deserializeComponentInstance(serializedComponentInstance);
		} catch (IOException e) {
			LOGGER.warn("Cannot deserialize component instance {}.", serializedComponentInstance, e);
		}
		return null;
	}

	public double parseScoreToDouble(String score) throws NumberFormatException {
		return Double.parseDouble(score);
	}

}
