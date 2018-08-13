package jaicore.graphvisualizer.gui.dataVisualizer;

import javafx.scene.Node;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class HTMLVisualizer implements IVisualizer {

    protected WebView webview;
    protected WebEngine webEngine;

    public HTMLVisualizer(){
        this.webview = new WebView();
        this.webEngine = this.webview.getEngine();
        //this.webEngine.load("https://start.fedoraproject.org/");
        this.webEngine.loadContent("<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <title>This is a title</title>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <p>Hello world!</p>\n" +
                "  </body>\n" +
                "</html>\n");
    }

    @Override
    public Node getVisualization() {
        return webview;
    }

    @Override
    public String getSupplier() {
        return null;
    }

    @Override
    public String getTitle() {
        return "HTML";
    }
}
