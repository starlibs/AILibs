package hasco.gui.statsplugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hasco.events.HASCOSolutionEvent;
import hasco.model.ComponentInstance;
import hasco.model.UnparametrizedComponentInstance;
import jaicore.basic.sets.SetUtil.Pair;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.FlowPane;

public class HASCOComponentSelectionTray extends TreeItem<FlowPane> {

	private final static Logger logger = LoggerFactory.getLogger(HASCOComponentSelectionTray.class);
	private final HASCOComponentSelectionTray parent;
	private final String requiredInterface;
	private final ComboBox<String> componentSelector;
	private final HASCOModelStatisticsPluginModel model;
	private final List<HASCOComponentSelectionTray> trayChildren = new ArrayList<>();

	public String getComponentSelectedInRoot() {
		if (parent == null)
			return componentSelector.getValue();
		return parent.getComponentSelectedInRoot();
	}

	/**
	 * gets the selections made on the tree elements of the path from the root to here
	 * 
	 * @return
	 */
	public List<Pair<String, String>> getSelectionsOnPathToRoot() {
		if (parent == null)
			return new ArrayList<>();
		List<Pair<String, String>> path = parent.getSelectionsOnPathToRoot();
		path.add(new Pair<>(this.requiredInterface, componentSelector.getValue()));
		return path;
	}

	public Collection<List<Pair<String, String>>> getAllSelectionsOnPathToAnyLeaf() {
		Collection<List<Pair<String, String>>> subPaths = new ArrayList<>();
		if (trayChildren.isEmpty()) {
			List<Pair<String, String>> leafRestriction = new ArrayList<>();
			leafRestriction.add(new Pair<>(requiredInterface, componentSelector.getValue()));
			subPaths.add(leafRestriction);
			return subPaths;
		}
		for (HASCOComponentSelectionTray child : trayChildren) {
			subPaths.addAll(child.getAllSelectionsOnPathToAnyLeaf());
		}
		return subPaths.stream().map(p -> {
			p.add(0, new Pair<>(this.requiredInterface, this.componentSelector.getValue()));
			return p;
		}).collect(Collectors.toList());
	}

	/**
	 * this recursively updates the whole tree view under this node with respect to the current selections
	 */
	public void update() {
		String componentChosenInRoot = getComponentSelectedInRoot();
		List<Pair<String, String>> selectionPath = getSelectionsOnPathToRoot();
		List<String> reqInterfacePath = selectionPath.stream().map(p -> p.getX()).collect(Collectors.toList());
		for (HASCOSolutionEvent<?> se : model.getAllSeenSolutionEventsUnordered()) {
			ComponentInstance ci = se.getSolutionCandidate().getComponentInstance();
			if (parent != null && !ci.getComponent().getName().equals(componentChosenInRoot))
				continue;
			if (!ci.matchesPathRestriction(selectionPath)) {
				continue;
			}

			/* determine sub-component relevant for this path */
			UnparametrizedComponentInstance uci = new UnparametrizedComponentInstance(ci).getSubComposition(reqInterfacePath);
			if (this.componentSelector.getItems().contains(uci.getComponentName()))
				continue;
			logger.trace("Relevant UCI of {} for path {} is {}", ci, reqInterfacePath, uci);
			this.componentSelector.getItems().add(uci.getComponentName());
		}
		trayChildren.forEach(ti -> ti.update());
	}

	public HASCOComponentSelectionTray(HASCOModelStatisticsPluginView rootView, HASCOModelStatisticsPluginModel model) {
		this(rootView, null, null, model);
	}

	public HASCOComponentSelectionTray(HASCOModelStatisticsPluginView rootView, HASCOComponentSelectionTray parent, String requiredInterface, HASCOModelStatisticsPluginModel model) {
		this.parent = parent;
		this.requiredInterface = requiredInterface;
		this.model = model;
		componentSelector = new ComboBox<>();
		componentSelector.getItems().add("*");
		componentSelector.setValue("*");
		componentSelector.valueProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

				// new HASCOComponentSelectionTray(this, model);

				// /* determine all choices that are consistent with this choice */
				// List<Pair<String, String>> listOfFiltersOnRequiredInterfaces = new ArrayList<>();
				// listOfFiltersOnRequiredInterfaces.add(new Pair<>("", newValue));
				// Collection<UnparametrizedComponentInstance> compatibleSolutionClasses = model.getSeenUnparametrizedComponentsUnderPath(listOfFiltersOnRequiredInterfaces);
				// List<String> path = new ArrayList<>(Arrays.asList(this.path.split(".")));
				// path.remove(0);
				// Collection<UnparametrizedComponentInstance> compatibleSubSolutions = compatibleSolutionClasses.stream().map(s -> s.getSubComposition(path)).collect(Collectors.toList());
				//

				getChildren().clear();
				trayChildren.clear();

				/* determine required interfaces of this choice  */
				if (!newValue.equals("*")) {
					Map<String, String> requiredInterfacesOfThisChoice = model.getKnownComponents().get(newValue).getRequiredInterfaces();
					for (String requiredInterfaceId : requiredInterfacesOfThisChoice.keySet()) {
						HASCOComponentSelectionTray tray = new HASCOComponentSelectionTray(rootView, HASCOComponentSelectionTray.this, requiredInterfaceId, model);
						getChildren().add(tray);
						trayChildren.add(tray);
					}
				}

				/* update histogram */
				rootView.updateHistogram();
			}
		});

		/* configure flow pane to display */
		FlowPane pane = new FlowPane();
		pane.getChildren().add(new Label((requiredInterface != null ? requiredInterface : "Root") + ": "));
		pane.getChildren().add(componentSelector);
		this.setValue(pane);
		update();
		this.setExpanded(true);
	}

	public HASCOComponentSelectionTray getParentTray() {
		return parent;
	}
}
