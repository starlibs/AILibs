package jaicore.graphvisualizer.guiOld.dataVisualizer;

/**
 * A Variation of the HTML-Visualizer with a different title and supplier.
 * @author jkoepe
 *
 */
public class TooltipVisualizer extends HTMLVisualizer {

    @Override
    public String getSupplier(){
        return "TooltipSupplier";
    }

    @Override
    public String getTitle(){
        return "Tooltips";
    }

}
