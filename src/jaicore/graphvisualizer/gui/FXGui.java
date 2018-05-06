package jaicore.graphvisualizer.gui;

import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionHTNPlanner;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.task.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.model.task.ceocstn.StandardProblemFactory;
import jaicore.search.structure.core.Node;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class FXGui extends Application {



    protected FXMLLoader rootLoader;
    protected FXController rootController;
    protected Recorder rootRecorder;

    @Override
    public void start(Stage stage) throws Exception {
        //load the fxml file of the gui
        this.rootLoader = new FXMLLoader(getClass().getResource("gui.fxml"));
        Parent root = this.rootLoader.load();

        this.rootController = rootLoader.getController();
        rootController.setRecorder(rootRecorder);

        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("Gui");
        stage.setScene(scene);
        stage.show();
    }


    @Override
    public void init() throws Exception {
        super.init();

        this.rootRecorder = new Recorder();
    }
}
