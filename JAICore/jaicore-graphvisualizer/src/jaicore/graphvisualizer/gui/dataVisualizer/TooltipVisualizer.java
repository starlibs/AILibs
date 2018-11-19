package jaicore.graphvisualizer.gui.dataVisualizer;

/**
 * A Variation of the HTML-Visualizer with a different title and supplier.
 * 
 */
public class TooltipVisualizer extends HTMLVisualizer {

	@Override
	public String getSupplier() {
		return "TooltipSupplier";
	}

	@Override
	public String getTitle() {
		return "Tooltips";
	}

}
