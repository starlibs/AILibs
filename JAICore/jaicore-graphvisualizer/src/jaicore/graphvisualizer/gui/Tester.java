package jaicore.graphvisualizer.gui;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.stage.Stage;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.Graph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.Viewer;

public class Tester extends Application {

    protected static String styleSheet =
            "graph {"+
                    "	padding: 60px;"+
                    "}";


    public static void main(String args[]) {
        Application.launch(Tester.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
//        Graph graph = new MultiGraph("mg");
//
//        FxViewer viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
//        graph.addNode("A");
//        graph.addNode("B");
//        graph.addNode("C");
//        graph.addEdge("AB", "A", "B");
//        graph.addEdge("BC", "B", "C");
//        graph.addEdge("CA", "C", "A");
//        graph.setAttribute( "ui.antialias" );
//        graph.setAttribute( "ui.quality" );
//        graph.setAttribute( "ui.stylesheet", styleSheet );
//
//        graph.getNode("A").setAttribute("xyz", -1, 0, 0 );
//        graph.getNode("B").setAttribute("xyz",  1, 0, 0 );
//        graph.getNode("C").setAttribute("xyz", 0, 1, 0 );
//
//        FxViewPanel v = (FxViewPanel) viewer.addDefaultView(false);
//        Scene scene = new Scene(v, 800, 600);
//        stage.setScene(scene);
//        stage.show();

//        styleSheet += "node {fill-color:red;}";
//        graph.setAttribute("ui.stylesheet", styleSheet);

        FXCode code = new FXCode(null);

    }
}
