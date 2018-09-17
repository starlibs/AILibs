package jaicore.graphvisualizer.gui.dataSupplier;

import java.util.LinkedHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import jaicore.graphvisualizer.events.controlEvents.ControlEvent;
import jaicore.graphvisualizer.events.controlEvents.NodePushed;
import jaicore.graphvisualizer.events.graphEvents.GraphEvent;
import jaicore.graphvisualizer.events.misc.HTMLEvent;

/**
 * A Datasupplier which should be able to reconstruct an HTML-Supplier
 * 
 * @author jkoepe
 *
 */

//TODO reevaluate usefulness here
public class ReconstructionDataSupplier implements ISupplier {

	private LinkedHashMap<?, ?> map;
	private String visualizer;

	private EventBus dataBus;

	private LinkedHashMap<?, ?> nodeData;

	public ReconstructionDataSupplier(LinkedHashMap<?, ?> map) {
		this.map = map;
		this.visualizer = (String) map.get("Visualizer");
		this.dataBus = new EventBus();
		this.nodeData = new LinkedHashMap<Object, Object>();
		this.nodeData = (LinkedHashMap<?, ?>) map.get("Data");
		
	}

	@Override
	public void registerListener(Object listener) {
		dataBus.register(listener);
	}

	@Override
	public void receiveGraphEvent(GraphEvent event) {

	}

	@Override
	@Subscribe
	public void receiveControlEvent(ControlEvent event) {
		String eventName = event.getClass().getSimpleName();
		switch (eventName) {
		case "NodePushed":
			NodePushed<?> nodePushed = (NodePushed<?>) event;
			if (visualizer.equals("HTMLVisualizer")) {
				Integer hashCode = nodePushed.getNode().hashCode();
				System.out.println(hashCode);
				String dataString = (String) nodeData.get(hashCode.toString());
				this.dataBus.post(new HTMLEvent(dataString));
			}

			break;
		}
	}

	@Override
	public JsonNode getSerialization() {
		ObjectMapper mapper = new ObjectMapper();

		return mapper.valueToTree(map);
	}

}
