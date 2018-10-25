package jaicore.graphvisualizer.gui.dataVisualizer;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.google.common.eventbus.Subscribe;

import jaicore.graphvisualizer.events.misc.HTMLEvent;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;

public class HTMLVisualizer implements IVisualizer {

	SwingNode node;
	JLabel label;

	public HTMLVisualizer() {
		this.label = new JLabel();
		this.label.setText("<html></html>");
		this.label.setVerticalAlignment(SwingConstants.TOP);
		this.node = new SwingNode();
		fillSwingnode(node);

	}

	private void fillSwingnode(SwingNode node) {
		JScrollPane pane = new JScrollPane();
		pane.setViewportView(label);
		SwingUtilities.invokeLater(() -> {
			node.setContent(pane);
		});
	}

	@Override
	public Node getVisualization() {
		return this.node;
//        return null;
	}

	@Override
	public String getSupplier() {
		return null;
	}

	@Override
	public String getTitle() {
		return "HTML";
	}

	@Subscribe
	public void receiveData(HTMLEvent html) {
		StringBuilder sb = new StringBuilder();
//						sb.append("<html><div style='padding: 5px; background: #ffffcc; border: 1px solid black;'>");
		sb.append("<html><div style='padding: 5px;'>");
		sb.append(html.getText());
		sb.append("</div></html>");

		label.setText(sb.toString());
	}
}
