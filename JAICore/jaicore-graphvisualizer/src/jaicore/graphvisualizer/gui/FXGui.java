package jaicore.graphvisualizer.gui;

import jaicore.graph.observation.IObservableGraphAlgorithm;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class FXGui{

    private List<FXController> controllers;

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

                if(controllers == null)
                    controllers = new ArrayList<>();

                Parent root = null;
                try {
                    root = loader.load();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("test");
                    System.exit(0);
                }

                FXController controller = loader.getController();
//                controller.setRecorder(recorder);
//                recorder.setContoller(controller);
                controller.registerListener(recorder);

                controllers.add(controller);


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
//        controller.setRecorder(recorder);
//        recorder.setContoller(controller);

        Scene scene = new Scene(root, 800,600);

        jfxPanel.setScene(scene);

    }

    public List<FXController> getControllers() {
        return controllers;
    }
}
