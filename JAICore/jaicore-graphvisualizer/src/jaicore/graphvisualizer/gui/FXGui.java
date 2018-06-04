package jaicore.graphvisualizer.gui;

import java.util.List;

import javax.swing.JFrame;

import jaicore.graph.observation.IObservableGraphAlgorithm;
import jaicore.graphvisualizer.IGraphDataSupplier;
import jaicore.graphvisualizer.INodeDataSupplier;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FXGui{


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

    public void open(IObservableGraphAlgorithm algorithm, String title, List<INodeDataSupplier> nodesupplier, IGraphDataSupplier supplier){
        Recorder rec  = new Recorder<>(algorithm);
        open(rec,title);

        nodesupplier.stream().forEach(s->rec.addNodeDataSupplier(s));


        rec.addGraphDataSupplier(supplier);

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
