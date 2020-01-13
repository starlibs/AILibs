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
	public final void addEntry(final ScoredSolutionCandidateInfo scoredSolutionCandidateInfo) {
		ComponentInstance ci = this.deserializeComponentInstance(scoredSolutionCandidateInfo.getSolutionCandidateRepresentation());
		if (ci == null) {
			return;
		}
		UnparametrizedComponentInstance uci = new UnparametrizedComponentInstance(ci);
		if (!this.observedSolutionsGroupedModuloParameters.containsKey(uci)) {
			this.observedSolutionsGroupedModuloParameters.put(uci, new ArrayList<>());
		}
		this.observedSolutionsGroupedModuloParameters.get(uci).add(scoredSolutionCandidateInfo);
		ci.getContainedComponents().forEach(c -> {
			if (!this.knownComponents.containsKey(c.getName())) {
				this.knownComponents.put(c.getName(), c);
			}
		});
		this.getView().update();
	}

	/**
	 * Gets an (unordered) collection of the solutions received so far.
	 *
	 * @return Collection of solutions.
	 */
	public Collection<ScoredSolutionCandidateInfo> getAllSeenSolutionCandidateFoundInfosUnordered() {
		List<ScoredSolutionCandidateInfo> solutionEvents = new ArrayList<>();
		this.observedSolutionsGroupedModuloParameters.values().forEach(solutionEvents::addAll);
		return solutionEvents;
	}

	/**
	 * @return A map that assigns, for each known component, its name to the Component object.
	 */
	public Map<String, Component> getKnownComponents() {
		return this.knownComponents;
	}

	/**
	 *
	 * @param composition
	 * @return
	 */
	public DescriptiveStatistics getPerformanceStatisticsForComposition(final UnparametrizedComponentInstance composition) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		this.observedSolutionsGroupedModuloParameters.get(composition).forEach(e -> stats.addValue(this.parseScoreToDouble(e.getScore())));
		return stats;
	}

	/**
	 * Clears the model (and subsequently the view)
	 */
	@Override
	public void clear() {
		this.observedSolutionsGroupedModuloParameters.clear();
		this.knownComponents.clear();
		this.getView().clear();
	}

	public ComponentInstance deserializeComponentInstance(final String serializedComponentInstance) {
		try {
			return this.componentInstanceSerializer.deserializeComponentInstance(serializedComponentInstance);
		} catch (IOException e) {
			LOGGER.warn("Cannot deserialize component instance {}.", serializedComponentInstance, e);
		}
		return null;
	}

	public double parseScoreToDouble(final String score) {
		return Double.parseDouble(score);
	}

}
