package jaicore.search.gui;

import com.google.common.eventbus.EventBus;
import jaicore.graphvisualizer.SearchVisualizationPanel;
import jaicore.search.structure.events.GraphInitializedEvent;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import javax.naming.directory.SearchControls;
import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class FXController implements Initializable  {

    @FXML
    private SwingNode swingNode;



    @FXML
    private Slider slider;



    private static Recorder rec;
    private Thread controllerThread;

    private long sleepTime;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        createSwingContent(swingNode);

        sleepTime = 50;
        /*slider.valueChangingProperty().addListener((observable, oldValue, newValue) ->{
            sleepTime = (long) slider.getValue();
            System.out.println(sleepTime);
        });*/
        slider.setOnMouseReleased((MouseEvent event)->{
            sleepTime = (long) (200 - slider.getValue());
            System.out.println(sleepTime);
        });
    }

    private void createSwingContent(SwingNode swingnode){

        SearchVisualizationPanel panel = new SearchVisualizationPanel();
        rec.registerListener(panel);

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
       Runnable runPlay = () ->{
          try{
              for(int i = 0; i < numberOfEvents; i ++){
                  rec.step();
                  TimeUnit.MILLISECONDS.sleep(sleepTime);
              }
          }
          catch (InterruptedException e){}
       };
       controllerThread = new Thread(runPlay);
       controllerThread.start();
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
       //FileChooser chooser = new FileChooser();
       //chooser.setTitle("Choose Event-File");
       //rec.saveToFile(chooser.showSaveDialog(null));


       File file = new File("/home/jkoepe/Documents/Test.txt");
       rec.saveToFile(file);
   }

    @FXML
    protected void load(ActionEvent event){
       //FileChooser chooser = new FileChooser();
       //chooser.setTitle("Open Event-File");
       //rec.loadFromFile(chooser.showOpenDialog(null));
        File file = new File("/home/jkoepe/Documents/Test.txt");
        rec.loadFromFile(file);
    }

   public static void setRec(Recorder recorder){
        rec = recorder;
   }

   public static void createRec(){
        rec = new Recorder();
   }

   @FXML
    protected void sliderTest(ActionEvent event){
       System.out.println(slider.getValue());
   }


}
