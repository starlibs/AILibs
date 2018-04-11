package jaicore.search.gui;

import com.google.common.eventbus.EventBus;
import jaicore.graphvisualizer.SearchVisualizationPanel;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javax.swing.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class FXController implements Initializable  {

    @FXML
    private SwingNode swingNode;

    private static Recorder rec;



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

   @FXML
    protected void play(ActionEvent event){
       System.out.println("play");
       int numberOfEvents = rec.getNumberOfEvents();
       for(int i = 0; i  < numberOfEvents; i++){
           rec.step();
       }
   }

   @FXML
    protected void step(ActionEvent event){
        System.out.println("step");
        rec.step();

   }

   @FXML
    protected void back(ActionEvent event){
        System.out.println("back");
   }

   @FXML
    protected void reset(ActionEvent event){
        System.out.println("reset");
   }

   public static void setRec(Recorder recorder){
        rec = recorder;
   }

   public static void createRec(){
        rec = new Recorder();
   }


}
