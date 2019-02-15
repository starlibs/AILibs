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
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.FlowPane;

public class HASCOModelStatisticsComponentSelector extends TreeItem<FlowPane> {

	private final static Logger logger = LoggerFactory.getLogger(HASCOModelStatisticsComponentSelector.class);
	private final HASCOModelStatisticsComponentSelector parent;
	private final String requiredInterface;
	private final ComboBox<String> componentSelector;
	private final HASCOModelStatisticsPluginModel model;
	private final List<HASCOModelStatisticsComponentSelector> trayChildren = new ArrayList<>();

	public String getComponentSelectedInRoot() {
		if (parent == null)
			return componentSelector.getValue();
		return parent.getComponentSelectedInRoot();
	}
	
	public void clear() {
		this.componentSelector.getItems().removeIf(s -> !s.equals("*"));
		getChildren().clear();
		trayChildren.clear();
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
		for (HASCOModelStatisticsComponentSelector child : trayChildren) {
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
		long start = System.currentTimeMillis();
		String componentChosenInRoot = getComponentSelectedInRoot();
		List<Pair<String, String>> selectionPath = getSelectionsOnPathToRoot();
		List<String> reqInterfacePath = selectionPath.stream().map(p -> p.getX()).collect(Collectors.toList());
		ObservableList<String> items = this.componentSelector.getItems();
		for (HASCOSolutionEvent<?> se : model.getAllSeenSolutionEventsUnordered()) {
			ComponentInstance ci = se.getSolutionCandidate().getComponentInstance();
			if (parent != null && !ci.getComponent().getName().equals(componentChosenInRoot))
				continue;
			if (!ci.matchesPathRestriction(selectionPath)) {
				continue;
			}

			/* determine sub-component relevant for this path and add the respective component lexicographically correctly (unless it is already in the list) */
			UnparametrizedComponentInstance uci = new UnparametrizedComponentInstance(ci).getSubComposition(reqInterfacePath);
			if (this.componentSelector.getItems().contains(uci.getComponentName()))
				continue;
			logger.trace("Relevant UCI of {} for path {} is {}", ci, reqInterfacePath, uci);
			int n = items.size();
			String nameOfNewComponent = uci.getComponentName();
			for (int i = 0; i <= n; i++) {
				if (i == n || items.get(i).compareTo(nameOfNewComponent) >= 0) {
					items.add(i, nameOfNewComponent);
					break;
				}
			}
		}
		trayChildren.forEach(ti -> ti.update());
		long duration = System.currentTimeMillis() - start;
		logger.debug("Update of {} took {}ms", this, duration);
	}

	public HASCOModelStatisticsComponentSelector(HASCOModelStatisticsPluginView rootView, HASCOModelStatisticsPluginModel model) {
		this(rootView, null, null, model);
	}

	public HASCOModelStatisticsComponentSelector(HASCOModelStatisticsPluginView rootView, HASCOModelStatisticsComponentSelector parent, String requiredInterface, HASCOModelStatisticsPluginModel model) {
		this.parent = parent;
		this.requiredInterface = requiredInterface;
		this.model = model;
		componentSelector = new ComboBox<>();
		componentSelector.getItems().add("*");
		componentSelector.setValue("*");
		componentSelector.valueProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				getChildren().clear();
				trayChildren.clear();
				
				/* determine required interfaces of this choice  */
				if (!newValue.equals("*")) {
					Map<String, String> requiredInterfacesOfThisChoice = model.getKnownComponents().get(newValue).getRequiredInterfaces();
					for (String requiredInterfaceId : requiredInterfacesOfThisChoice.keySet()) {
						HASCOModelStatisticsComponentSelector tray = new HASCOModelStatisticsComponentSelector(rootView, HASCOModelStatisticsComponentSelector.this, requiredInterfaceId, model);
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

	public HASCOModelStatisticsComponentSelector getParentTray() {
		return parent;
	}
}
