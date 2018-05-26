package jaicore.graphvisualizer.gui;

import jaicore.graph.observation.IObservableGraphAlgorithm;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionHTNPlanner;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.task.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.model.task.ceocstn.StandardProblemFactory;
import jaicore.search.structure.core.Node;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class FXGui{



//    protected FXMLLoader rootLoader;
//    protected FXController rootController;
//    protected Recorder rootRecorder;

//    @Override
//    public void start(Stage stage) throws Exception {
////        //load the fxml file of the gui
////        this.rootLoader = new FXMLLoader(getClass().getResource("gui.fxml"));
////        Parent root = this.rootLoader.load();
////
////        this.rootController = rootLoader.getController();
////        rootController.setRecorder(rootRecorder);
////
////        Scene scene = new Scene(root, 800, 600);
////
////        stage.setTitle("Gui");
////        stage.setScene(scene);
////        stage.show();
//    }
//
////
////    @Override
////    public void init() throws Exception {
////        super.init();
////
////        this.rootRecorder = new Recorder();
////    }

    public void open(){
       open(new Recorder(), "GUI");
    }

    public void open(IObservableGraphAlgorithm algorithm){
       open(new Recorder(algorithm),"Gui");
    }

    public void open(IObservableGraphAlgorithm algorithm, String title){
        open(new Recorder(algorithm),title);
    }

    public void open(Recorder recorder){
        open(recorder, "GUI");
    }

    public void open(Recorder recorder, String title){
        try{
                FXMLLoader loader = new FXMLLoader(getClass().getResource("gui.fxml"));

                Parent root = null;
                try {
                    root = loader.load();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                }

                FXController controller = loader.getController();
                controller.setRecorder(recorder);
                recorder.setContoller(controller);

                Scene scene = new Scene(root, 800,600);


//                    openFX(scene, title);
                Stage stage = new Stage();
                stage.setTitle(title);
                stage.setScene(scene);
                stage.show();
            }
            catch (IllegalStateException | ExceptionInInitializerError e){
                JFrame frame = new JFrame(title);

                JFXPanel jfxPanel = new JFXPanel();
                frame.add(jfxPanel);

                Platform.runLater(()->initSwingFX(jfxPanel,recorder));

            }

//            Stage stage = new Stage();
//
//            stage.setTitle(title);
//            stage.setScene(scene);
//            stage.show();


    }

    private void initSwingFX(JFXPanel jfxPanel, Recorder recorder){

        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui.fxml"));

        Parent root = null;
        try {
            root = loader.load();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        FXController controller = loader.getController();
        controller.setRecorder(recorder);
        recorder.setContoller(controller);

        Scene scene = new Scene(root, 800,600);

        jfxPanel.setScene(scene);

    }
}
