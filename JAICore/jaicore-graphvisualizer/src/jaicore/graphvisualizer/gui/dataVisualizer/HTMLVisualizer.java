package jaicore.graphvisualizer.gui.dataVisualizer;

import com.google.common.eventbus.Subscribe;
import jaicore.graphvisualizer.events.misc.HTMLEvent;
import javafx.scene.Node;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class HTMLVisualizer implements IVisualizer {

//    protected WebView webview;
//    protected WebEngine webEngine;

    public HTMLVisualizer(){
//        this.webview = new WebView();
//        this.webEngine = this.webview.getEngine();
//        //this.webEngine.load("https://start.fedoraproject.org/");
//        this.webEngine.loadContent("test");

    }

    @Override
    public Node getVisualization() {
//        return webview;
        return null;
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
    public void receiveData(HTMLEvent html){
        StringBuilder sb = new StringBuilder();

        sb.append("<html><div style='padding: 5px;'>");
        sb.append(html.getText());
        sb.append("</div></html>");

//        webEngine.loadContent(sb.toString());
    }
}
