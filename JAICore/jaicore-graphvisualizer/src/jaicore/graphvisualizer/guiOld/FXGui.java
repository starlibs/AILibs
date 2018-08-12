package jaicore.graphvisualizer.guiOld;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import jaicore.graph.IObservableGraphAlgorithm;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FXGui{

    private List<FXController> controllers;

    public void open(){
       open(new Recorder(), "GUI");
    }
    
    /**
     * Opens a GUI-Windows by creating a recorder to the algorithm
     * @param algorithm
     */
    public void open(IObservableGraphAlgorithm algorithm){
       open(new Recorder(algorithm),"Gui");
    }

    public void open(IObservableGraphAlgorithm algorithm, String title){
        open(new Recorder(algorithm),title);
    }

    public void open(Recorder recorder){
        open(recorder, "GUI");
    }
    /**
     * Opens a GUI-VisualizationWindow with a given Recorder as the main supplier
     * @param recorder
     * 		The recorder which contains the recorded events
     * @param title
     * 		The  title of the gui window.
     */
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
                controller.registerRecorder(recorder);

                controllers.add(controller);


                Scene scene = new Scene(root, 800,600);


//                    openFX(scene, title);
                Stage stage = new Stage();
                stage.setTitle(title);
                stage.setScene(scene);
                stage.show();
                controller.registerObject(recorder.getAlgorithm());
            }
            catch (IllegalStateException | ExceptionInInitializerError e){
                JFrame frame = new JFrame(title);

                JFXPanel jfxPanel = new JFXPanel();
                frame.add(jfxPanel);

                Platform.runLater(()->initSwingFX(jfxPanel,recorder));

            }


    }

    
    /**
     * Tryed to implement the Graph in Swing. Not sure if this is really used anymore
     * @param jfxPanel
     * @param recorder
     */
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