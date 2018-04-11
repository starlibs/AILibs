package jaicore.search.gui;

import com.google.common.eventbus.EventBus;
import jaicore.graphvisualizer.SearchVisualizationPanel;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.FileChooser;

import javax.swing.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class FXController implements Initializable  {

    @FXML
    private SwingNode swingNode;

    private static Recorder rec;

    private Thread controllerThread;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        createSwingContent(swingNode);

    }

    private void createSwingContent(SwingNode swingnode){

        SearchVisualizationPanel panel = new SearchVisualizationPanel<>(rec.getEventBus());

        //JPanel panel = new JPanel();
        //panel.add(new JButton("Test"));
        SwingUtilities.invokeLater(()-> {
            swingnode.setContent(panel);
        });
    }

    /**
     * Starts the playback of the events. For this an own thread is created, in order to not freeze the whole GUI.
     * The playback can be stoped by pressing the stop button.
     */
   @FXML
    protected void play(ActionEvent event){
       System.out.println("play");
       int numberOfEvents = rec.getNumberOfEvents();
<<<<<<< HEAD
       Runnable runPlay = () ->{
          try{
              for(int i = 0; i < numberOfEvents; i ++){
                  rec.step();
                  TimeUnit.MILLISECONDS.sleep(50);
              }
          }
          catch (InterruptedException e){}
       };
       controllerThread = new Thread(runPlay);
       controllerThread.start();
=======
       for(int i = 0; i  < numberOfEvents; i++){
           rec.step();
       }
>>>>>>> Gui based on xml
   }

    /**
     * Takes a single step, if the step-Button is pressed.
     * @param event
     */
   @FXML
    protected void step(ActionEvent event){
        System.out.println("step");
        rec.step();

   }

   @FXML
    protected void back(ActionEvent event){
        System.out.println("back");
        createSwingContent(swingNode);
        rec.back();

   }

   @FXML
    protected void reset(ActionEvent event){
        System.out.println("reset");
        createSwingContent(swingNode);
        rec.reset();
   }

   @FXML
   protected void stop(ActionEvent event){
        controllerThread.interrupt();
        System.out.println("Stop");
   }

   @FXML
   protected void save(ActionEvent event){
       FileChooser chooser = new FileChooser();
       chooser.setTitle("Choose Event-File");

       rec.saveToFile(chooser.showSaveDialog(null));
   }

<<<<<<< HEAD
    @FXML
    protected void load(ActionEvent event){
       FileChooser chooser = new FileChooser();
       chooser.setTitle("Open Event-File");
       rec.loadFromFile(chooser.showOpenDialog(null));
    }

=======
>>>>>>> Gui based on xml
   public static void setRec(Recorder recorder){
        rec = recorder;
   }

   public static void createRec(){
        rec = new Recorder();
   }


}
