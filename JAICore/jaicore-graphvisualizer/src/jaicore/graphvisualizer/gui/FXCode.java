package jaicore.graphvisualizer.gui;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.reflect.ClassPath;

import jaicore.graphvisualizer.events.controlEvents.EnableColouring;
import jaicore.graphvisualizer.events.controlEvents.FileEvent;
import jaicore.graphvisualizer.events.controlEvents.NodePushed;
import jaicore.graphvisualizer.events.controlEvents.ResetEvent;
import jaicore.graphvisualizer.events.controlEvents.StepEvent;
import jaicore.graphvisualizer.events.misc.AddSupplierEvent;
import jaicore.graphvisualizer.events.misc.InfoEvent;
import jaicore.graphvisualizer.gui.dataSupplier.ISupplier;
import jaicore.graphvisualizer.gui.dataVisualizer.IVisualizer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * Class which builds up the GUI
 *
 * @param <V>
 * @param <E>
 */
public class FXCode<V, E> implements NodeListener<V> {
    /*Variables needed for the whole class*/

    /*Tabpane*/
    private TabPane tabPane;

    /*Timeline*/
    private Slider timeline;

    /*EventBus*/
    private EventBus eventBus;


    /*Indices*/
    private int index;
    private int maxIndex;

    private int maxDisplayIndex;
    private int displayIndex;

    /*Visualisation window and visualizaton threads*/
    private GraphVisualization<V, E> visualization;
    private Thread playThread;

    /*Settings*/
    private long sleepTime;

    /*update restriction*/
    private Semaphore sem;
    private Thread updateThread;

    /*Log*/
    private Text log;
    
    private Button colouringButton;
    private boolean colouring;
    
    /**
     *Number of Ticks at the timeline. This number should not be greater then 1999  
     */
    private int numberOfTicks;
    /**
     * Constructor
     */
    public FXCode(Recorder<V, E> rec, String title, ObjectEvaluator eval) {
        /*initialize object variables;*/
        this.index = 0;
        this.maxIndex = 0;
        this.maxDisplayIndex = 0;
        this.displayIndex = 0;

        this.sleepTime = 0;

        this.eventBus = new EventBus();
        this.eventBus.register(rec);

        rec.registerInfoListener(this);

        this.log = new Text();
        this.numberOfTicks= 250;

        /*declare  and initialize FX-elements*/

        /*create Main-BorderPane*/
        BorderPane root = new BorderPane();

        /*top*/
        ToolBar toolBar = new ToolBar();
        BorderPane top = new BorderPane();
        Slider sleepTimeSlider = new Slider(0, 200, 200 - sleepTime);

        /*center*/
        SplitPane splitPane = new SplitPane();
        this.tabPane = new TabPane();
        this.visualization = new GraphVisualization<V, E>(eval);


        /*Bottom*/
        this.timeline = new Slider();

        Scene scene = new Scene(root, 800, 300);
        Stage stage = new Stage();

        /*settings for the gui elements*/
        /*top*/
        if(eval != null) {
        	fillToolbar(toolBar.getItems(), true);
        	colouring = true;
        }
        else
        	fillToolbar(toolBar.getItems(), false);
        setSleepTimeSlider(sleepTimeSlider);
        top.setTop(toolBar);
        top.setBottom(sleepTimeSlider);
        root.setTop(top);
        
        /*Center*/
        rec.registerReplayListener(visualization);
        visualization.addNodeListener(this);
        this.eventBus.register(visualization);
        

        splitPane.setDividerPosition(0, 0.25);
        splitPane.getItems().add(tabPane);
        splitPane.getItems().add(visualization.getFXNode());
        root.setCenter(splitPane);

        /*Bottom*/
        setTimelineSlider();
        root.setBottom(this.timeline);

        stage.setScene(scene);
        stage.setTitle(title);
        stage.setMaximized(true);
        stage.show();

        Tab logTab = new Tab("Log");
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(log);
        logTab.setContent(scrollPane);
        this.tabPane.getTabs().add(logTab);
        
        rec.getSupplier();
        this.startPlayThread();
        this.startUpdateRestriction(35);

    }

    /**
     * Sets the settings for the timeline
     */
    private void setTimelineSlider() {
        this.timeline.setShowTickLabels(true);
        this.timeline.setShowTickMarks(true);
        this.timeline.setOnMouseReleased((MouseEvent event) -> {
            int newIndex = (int) timeline.getValue();
            jumpToIndex(newIndex);
        });
        this.timeline.setOnKeyReleased((KeyEvent event) -> {
            int newIndex = (int) timeline.getValue();
            jumpToIndex(newIndex);
        });
        this.timeline.setOnKeyPressed((KeyEvent event) -> {
            int newIndex = (int) timeline.getValue();
            jumpToIndex(newIndex);
        });
        this.timeline.setBlockIncrement(1);
    }

    /**
     * Sets the settings for the sleepTimeSlider
     */
    private void setSleepTimeSlider(Slider sleepTimeSlider) {
        sleepTimeSlider.setShowTickLabels(true);
        sleepTimeSlider.setShowTickMarks(true);
        sleepTimeSlider.setBlockIncrement(1);
        sleepTimeSlider.setOnMouseReleased((MouseEvent event) -> {
            double sliderValue = sleepTimeSlider.getValue();
            this.sleepTime = (long) (200 - sliderValue);
        });
        sleepTimeSlider.setOnKeyPressed((KeyEvent event) -> {
            double sliderValue = sleepTimeSlider.getValue();
            this.sleepTime = (long) (200 - sliderValue);
        });
        sleepTimeSlider.setOnKeyReleased((KeyEvent event) -> {
            double sliderValue = sleepTimeSlider.getValue();
            this.sleepTime = (long) (200 - sliderValue);
        });

        sleepTimeSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                Double speed = 200 - object;
                return String.valueOf(speed.longValue());
            }

            @Override
            public Double fromString(String string) {
                return null;
            }
        });
    }

    /**
     * Uses a thread to continuously post events
     */
    private void startPlayThread() {
        /* play runs in an own thread to make it stoppable*/
        Runnable run = () -> {

            try {
                while (!Thread.currentThread().isInterrupted()) {
                    int i = index;
                    while (i >= 0) {

                        eventBus.post(new StepEvent(true, 1));
                        TimeUnit.MILLISECONDS.sleep(sleepTime);
                        updateIndex(1, false);
                        i = index;
                        if (Thread.currentThread().isInterrupted()) {
                            i = -1;
                        }
                    }
                }
            } catch (InterruptedException e) {

            }
        };

        playThread = new Thread(run);
        playThread.start();
    }

    /**
     * Creates the controll-buttons and adds them to the given List
     *
     * @param nodeList A list which shall contain the nodes of the buttons
     */
    private void fillToolbar(List<Node> nodeList, boolean eval) {
        /* playbutton*/
        Button playButton = new Button("Play");
        playButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                startPlayThread();
            }
        });
              

        nodeList.add(playButton);
        /* stepButton*/
        Button stepButton = new Button("Step");
        stepButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                eventBus.post(new StepEvent(true, 1));
                if (index != maxIndex)
                    updateIndex(1, false);
            }
        });
        nodeList.add(stepButton);

        /* stopButton*/
        Button stopButton = new Button("Stop");
        stopButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (playThread != null)
                    playThread.interrupt();
            }
        });
        nodeList.add(stopButton);

        /* BackButton*/
        Button backButton = new Button("Back");
        backButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (index == 0)
                    return;
                if (index == 1) {
                    reset();
                    return;
                }
                eventBus.post(new StepEvent(false, 1));
                updateIndex(-1, false);
            }
        });
        nodeList.add(backButton);

      /*  resetButton*/
        Button resetButton = new Button("reset");
        resetButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                reset();
            }
        });
        nodeList.add(resetButton);

       /* loadButton*/
        Button loadButton = new Button("load");
        loadButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                FileChooser chooser = new FileChooser();
                File file = chooser.showOpenDialog(null);
                if(file != null)
                	eventBus.post(new FileEvent(true, file));
            }
        });
        nodeList.add(loadButton);

        /* saveButton*/
        Button saveButton = new Button("save");
        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                FileChooser chooser = new FileChooser();
                File file = chooser.showSaveDialog(null);
                if(file != null)
                	eventBus.post(new FileEvent(false, file));
            }
        });
        nodeList.add(saveButton);
        
        /*Colouring button*/
        colouringButton = new Button("colouring");
        colouringButton.setOnAction(new EventHandler<ActionEvent>() {
        	@Override
        	public void handle(ActionEvent actionEvent) {
        		if(!colouring) {
        			colouring = true;
        			
        			
        		}
        		else {
        			colouring = false;
        		}
        		eventBus.post(new EnableColouring(colouring));
        	}
        });
        nodeList.add(colouringButton);
        if(!eval)
        	colouringButton.setDisable(true);
        
    }

    /**
     * Receive Info-Events from the recorder to update the timeline and the maximum
     * index
     *
     * @param event The info-event.
     */
    @Subscribe
    public void receiveInfoEvent(InfoEvent event) {
        try {
            this.maxIndex = event.getMaxIndex();

            this.sem.release();
            if (event.updateIndex())
                this.updateIndex(maxIndex, true);
        } catch (NullPointerException e) {

        }
    }

    /**
     * Updates the index if a new step is done. Depending on the type of step it is
     * possible to either get the actual new index (<code>isRealIndex = true</code>
     * or an additive one.
     *
     * @param newIndex    A variable which is used to compute the new index. Either
     *                    it is the actual new index or this number has to be added
     *                    to the current index.
     * @param isRealIndex <code>true</code> if the newIndex is the actual new index,
     *                    <code>false</code> if newIndex is additive.
     */
    private void updateIndex(int newIndex, boolean isRealIndex) {
        if (!isRealIndex)
            newIndex += this.index;

        if (newIndex > this.maxIndex || newIndex < 0)
            return;

        this.index = newIndex;
        this.sem.release();
		if(index > this.maxDisplayIndex) {
	        this.timeline.setValue(this.maxDisplayIndex);
	        this.displayIndex = this.maxDisplayIndex;
		}
		else {
			this.timeline.setValue(index);
			this.displayIndex = index;
		}
        this.updateLog("Index: " + index);

    }

    /**
     * Resets the GUI
     */
    public void reset() {
        if (this.playThread != null)
            this.playThread.interrupt();
        this.updateIndex(0, true);
        this.visualization.reset();
        eventBus.post(new ResetEvent());
    }

    /**
     * Jumps to a specific index at the timeline
     *
     * @param newIndex
     */
    public void jumpToIndex(int newIndex) {
        if (this.playThread != null)
            playThread.interrupt();
        if (newIndex == 0) {
            this.reset();
            return;
        }
        if (newIndex > this.index)
            this.eventBus.post(new StepEvent(true, newIndex - this.index));
        else
            this.eventBus.post(new StepEvent(false, index - newIndex));
        this.updateIndex(newIndex, true);
    }

    public TabPane getTabPane() {
        return tabPane;
    }

    /**
     * Registers a new supplier to the Controller. In addition the cooresponging
     * Visualizers are searched and also added.
     *
     * @param supplier The new supplier.
     */
    public void addDataSupplier(ISupplier supplier) {

        try {
            ClassPath path = ClassPath.from(ClassLoader.getSystemClassLoader());
            Set<?> set = path.getAllClasses();
            try {
                set.stream().forEach(cls -> {
                    if (cls instanceof ClassPath.ClassInfo) {
                       /* search for a Visualizer.
                	To identify a visualizer the package name has to contain .dataVisualizer.*/
                        if (((ClassPath.ClassInfo) cls).getName().contains(".dataVisualizer.")) {
                            IVisualizer v = (IVisualizer) findClassByName(((ClassPath.ClassInfo) cls).getName());
                            try {
                                if (v != null) {
                                   /* if the supplier of the visualizer matches the current one, add the visualizer
                                    to the tabpane*/
                                    if (v.getSupplier().equals(supplier.getClass().getSimpleName())) {
                                        supplier.registerListener(v);
                                        this.eventBus.register(supplier);
                                        this.eventBus.register(v);

                                        Tab tab = new Tab();
                                        tab.setContent(v.getVisualization());
                                        tab.setText(v.getTitle());
                                        this.tabPane.getTabs().add(tab);
                                    }
                                }
                            } catch (Exception e) {

                            }
                        }
                    }
                });
            } catch (Exception e) {
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Searches the loaded classes for class with a specific name.
     *
     * @param name The name of the searched class.
     * @return
     */
    private Object findClassByName(String name) {
        try {
            Class<?> cls = Class.forName(name);
            if (cls.isInterface())
                return null;
            return cls.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
            return null;
        }
    }

    @Subscribe
    public void receiveAddSupplierEvent(AddSupplierEvent event) {
        this.addDataSupplier(event.getSupplier());
    }

    @Override
    public void mouseOver(Object node) {

    }

    @Override
    public void mouseLeft(Object node) {

    }

    @Override
    public void buttonReleased(Object node) {
    }

    @Override
    public void buttonPushed(Object node) {
        this.eventBus.post(new NodePushed<Object>(node));
    }

    /**
     * Updates the log
     *
     * @param logEntry the next log entry
     */
    private void updateLog(String logEntry) {
        String currentLog = this.log.getText()+  "\n - " + logEntry;
        Platform.runLater(()->{
        	 log.setText(currentLog);
        });
       
    }

    private void startUpdateRestriction(long delay) {
        sem = new Semaphore(0);
        Runnable run = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(delay);
                    sem.acquire();
                    updateTimelineIndex();
                    sem.drainPermits();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        };
        updateThread = new Thread(run, "Update");
        updateThread.start();
    }
    
    /**
     * Updates the timeline
     */
    private void updateTimelineIndex() {
        this.timeline.setMax(maxIndex);
        int tickUnit= maxIndex / this.numberOfTicks;
        this.timeline.setMajorTickUnit(tickUnit);
        this.maxDisplayIndex = maxIndex;
        if(this.displayIndex < maxDisplayIndex || this.displayIndex < this.index) {
        	if(index <= maxDisplayIndex) {
        		this.displayIndex = this.index;
        	}
        	else {
        		this.displayIndex = this.maxDisplayIndex;
        	}
        	this.timeline.setValue(displayIndex);
        }

    }

	public int getNumberOfTicks() {
		return numberOfTicks;
	}

	public void setNumberOfTicks(int numberOfTicks) {
		this.numberOfTicks = numberOfTicks;
	}

}