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

public class FXController implements Initializable  {

    @FXML
    private SwingNode swingNode;

    //static Recorder rec = new Recorder();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        createSwingContent(swingNode);
    }

    private void createSwingContent(SwingNode swingnode){
        EventBus bus = new EventBus();
        SearchVisualizationPanel panel = new SearchVisualizationPanel<>(bus);

        //JPanel panel = new JPanel();
        //panel.add(new JButton("Test"));
        SwingUtilities.invokeLater(()-> {
            swingnode.setContent(panel);
        });
    }

   @FXML
    protected void play(ActionEvent event){
       System.out.println("play");
   }

   @FXML
    protected void step(ActionEvent event){
        System.out.println("step");

   }

   @FXML
    protected void back(ActionEvent event){
        System.out.println("back");
   }

   @FXML
    protected void reset(ActionEvent event){
        System.out.println("reset");
   }
}
