package newVisualizer;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import jaicore.graph.observation.IObservableGraphAlgorithm;
import jaicore.graphvisualizer.SearchVisualizationPanel;
import jaicore.graphvisualizer.events.add.InfoEvent;
import jaicore.graphvisualizer.events.controlEvents.FileEvent;
import jaicore.graphvisualizer.events.controlEvents.ResetEvent;
import jaicore.graphvisualizer.events.controlEvents.StepEvent;
import jaicore.graphvisualizer.gui.Recorder;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.FileChooser;

import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class Controller implements Initializable {
	
    @FXML
    SwingNode graphNode;

    SearchVisualizationPanel visu;




    //control variables
    private int index;
    private int maxIndex;
    private long sleepTime;

    //EventBus
    private EventBus controlEventBus;

    //Thread for playing
    private Thread playThread;

    @FXML
    public void play(ActionEvent actionEvent) {
        Runnable run = ()->{
            try{
                while (index < maxIndex && index >= 0){
                    this.step(null);
                    TimeUnit.MILLISECONDS.sleep(sleepTime);
                }

            }
            catch(InterruptedException e){
//                e.printStackTrace();
            }
        };

        playThread = new Thread(run);
        playThread.start();

    }

    /**
     * Posts a stepEvent which goes one step forward
     * @param actionEvent
     */
    @FXML
    public void step(ActionEvent actionEvent) {
        if(index == maxIndex)
            return;
        this.controlEventBus.post(new StepEvent(true, 1));
        this.index ++;

    }

    @FXML
    public void back(ActionEvent actionEvent) {
        if(index == 0)
            return;
        if(index == 1) {
            this.reset(null);
            return;
        }
        this.controlEventBus.post(new StepEvent(false, 1));
        this.index --;

    }

    @FXML
    public void reset(ActionEvent actionEvent) {
        this.controlEventBus.post(new ResetEvent());
        this.index = 0;
        SearchVisualizationPanel panel = (SearchVisualizationPanel) graphNode.getContent();
        panel.reset();

    }

    /**
     * Stops a replay
     * @param actionEvent
     */
    @FXML
    public void stop(ActionEvent actionEvent) {
        if(playThread != null)
            playThread.interrupt();
    }

    @FXML
    public void save(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Event-File");
//        File file = chooser.showSaveDialog(null);

        File file = new File("/home/jkoepe/Documents/Test.txt");


        this.controlEventBus.post(new FileEvent(false, file));

    }

    @FXML
    public void load(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Event-File");
//        File file = chooser.showOpenDialog(null);

        File file = new File("/home/jkoepe/Documents/Test.txt");

        this.controlEventBus.post(new FileEvent(true, file));

    }

    private void jumpTo(int newIndex){
        if(newIndex == 0) {
            this.reset(null);
            return;
        }
        if(newIndex > index)
            this.controlEventBus.post(new StepEvent(true, newIndex-index));
        else
            this.controlEventBus.post(new StepEvent(false, index-newIndex));
        index = newIndex;
//        timeline.setValue(index);
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.controlEventBus = new EventBus();
        initializeVisualization(graphNode);
    }

    public void register(IObservableGraphAlgorithm algoritm) {
        algoritm.registerListener(visu);
    }

    private void initializeVisualization(SwingNode swingNode) {
        SearchVisualizationPanel visu = new SearchVisualizationPanel();
//        visu.addNodeListener(this);

        SwingUtilities.invokeLater(()->swingNode.setContent(visu));
    }

    public void registerRecorder(Recorder rec){
        this.controlEventBus.register(rec);
        SearchVisualizationPanel visu = (SearchVisualizationPanel) graphNode.getContent();
        rec.registerListener(visu);
        rec.registerInfoListener(this);
    }

    @Subscribe
    public void receiveInfoEvent(InfoEvent event){
        this.maxIndex = event.getMaxIndex();
        //TODO
//        updateTimeline();

    }
}
