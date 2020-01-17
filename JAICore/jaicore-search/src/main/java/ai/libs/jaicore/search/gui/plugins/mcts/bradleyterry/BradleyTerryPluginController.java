package ai.libs.jaicore.search.gui.plugins.mcts.bradleyterry;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.algorithm.events.serializable.IPropertyProcessedAlgorithmEvent;

import ai.libs.jaicore.graphvisualizer.events.gui.GUIEvent;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;
import ai.libs.jaicore.graphvisualizer.plugin.controlbar.ResetEvent;
import ai.libs.jaicore.graphvisualizer.plugin.graphview.NodeClickedEvent;
import ai.libs.jaicore.graphvisualizer.plugin.timeslider.GoToTimeStepEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.RolloutEvent;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.ObservationsUpdatedEvent;
import ai.libs.jaicore.search.gui.plugins.rollouthistograms.RolloutInfo;
import ai.libs.jaicore.search.gui.plugins.rollouthistograms.RolloutInfoAlgorithmEventPropertyComputer;

public class BradleyTerryPluginController extends ASimpleMVCPluginController<BradleyTerryPluginModel, BradleyTerryPluginView> {

	public BradleyTerryPluginController(final BradleyTerryPluginModel model, final BradleyTerryPluginView view) {
		super(model, view);
	}

	@Override
	public void handleGUIEvent(final GUIEvent guiEvent) {
		if (guiEvent instanceof ResetEvent || guiEvent instanceof GoToTimeStepEvent) {
			this.getModel().clear();
		} else if (guiEvent instanceof NodeClickedEvent) {
			this.getModel().setCurrentlySelectedNode(((NodeClickedEvent) guiEvent).getSearchGraphNode());
			this.getView().update();
		}
	}

	@Override
	public void handleAlgorithmEventInternally(final IPropertyProcessedAlgorithmEvent algorithmEvent) {
		if (algorithmEvent.correspondsToEventOfClass(RolloutEvent.class)) {

			RolloutInfo rolloutInfo = (RolloutInfo) algorithmEvent.getProperty(RolloutInfoAlgorithmEventPropertyComputer.ROLLOUT_SCORE_PROPERTY_NAME);

			String lastNode = null;
			for (String n : rolloutInfo.getPath()) {
				if (lastNode != null) {
					List<String> successors = this.getModel().getListsOfKnownSuccessors().computeIfAbsent(lastNode, k -> new ArrayList<>());
					if (!successors.contains(n)) {
						successors.add(n);
					}
					this.getModel().getParents().put(n, lastNode);
				}
				lastNode = n;
			}
		}
		else if (algorithmEvent.correspondsToEventOfClass(ObservationsUpdatedEvent.class)) {
			BradleyTerryUpdate updateInfo = (BradleyTerryUpdate) algorithmEvent.getProperty(BradleyTerryEventPropertyComputer.UPDATE_PROPERTY_NAME);
			this.getModel().setNodeStats(updateInfo);
		}
	}

}
