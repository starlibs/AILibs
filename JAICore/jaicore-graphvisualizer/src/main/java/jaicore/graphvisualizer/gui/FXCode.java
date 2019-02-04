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
	 * Number of Ticks at the timeline. This number should not be greater then 1999
	 */
	private int numberOfTicks;

	/**
	 * Constructor
	 */
	public FXCode(final Recorder<V, E> rec, final String title, final ObjectEvaluator eval) {
		/*initialize object variables;*/
		this.index = 0;
		this.maxIndex = 0;
		this.maxDisplayIndex = 0;
		this.displayIndex = 0;

		this.sleepTime = 1;

		this.eventBus = new EventBus();
		this.eventBus.register(rec);

		rec.registerInfoListener(this);

		this.log = new Text();
		this.numberOfTicks = 150;

		/*declare  and initialize FX-elements*/

		/*create Main-BorderPane*/
		BorderPane root = new BorderPane();

		/*top*/
		ToolBar toolBar = new ToolBar();
		BorderPane top = new BorderPane();
		Slider sleepTimeSlider = new Slider(0, 200, 200 - this.sleepTime);

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
		if (eval != null) {
			this.fillToolbar(toolBar.getItems(), true);
			this.colouring = true;
		} else {
			this.fillToolbar(toolBar.getItems(), false);
		}
		this.setSleepTimeSlider(sleepTimeSlider);
		top.setTop(toolBar);
		top.setBottom(sleepTimeSlider);
		root.setTop(top);

		/*Center*/
		rec.registerReplayListener(this.visualization);
		this.visualization.addNodeListener(this);
		this.eventBus.register(this.visualization);

		splitPane.setDividerPosition(0, 0.25);
		splitPane.getItems().add(this.tabPane);
		splitPane.getItems().add(this.visualization.getFXNode());
		root.setCenter(splitPane);

		/*Bottom*/
		this.setTimelineSlider();
		root.setBottom(this.timeline);

		stage.setScene(scene);
		stage.setTitle(title);
		stage.setMaximized(true);
		stage.show();

		Tab logTab = new Tab("Log");
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(this.log);
		logTab.setContent(scrollPane);
		this.tabPane.getTabs().add(logTab);

		rec.getSupplier();
		// this.startPlayThread();
		this.startUpdateRestriction(35);

	}

	/**
	 * Sets the settings for the timeline
	 */
	private void setTimelineSlider() {
		this.timeline.setShowTickLabels(true);
		this.timeline.setShowTickMarks(true);
		this.timeline.setOnMouseReleased((final MouseEvent event) -> {
			int newIndex = (int) this.timeline.getValue();
			this.jumpToIndex(newIndex);
		});
		this.timeline.setOnKeyReleased((final KeyEvent event) -> {
			int newIndex = (int) this.timeline.getValue();
			this.jumpToIndex(newIndex);
		});
		this.timeline.setOnKeyPressed((final KeyEvent event) -> {
			int newIndex = (int) this.timeline.getValue();
			this.jumpToIndex(newIndex);
		});
		this.timeline.setBlockIncrement(1);
		// this.timeline.setMinorTickCount(5);
		this.timeline.setMinorTickCount(0);
	}

	/**
	 * Sets the settings for the sleepTimeSlider
	 */
	private void setSleepTimeSlider(final Slider sleepTimeSlider) {
		sleepTimeSlider.setShowTickLabels(true);
		sleepTimeSlider.setShowTickMarks(true);
		sleepTimeSlider.setBlockIncrement(1);
		sleepTimeSlider.setOnMouseReleased((final MouseEvent event) -> {
			double sliderValue = sleepTimeSlider.getValue();
			this.sleepTime = (long) (200 - sliderValue);
		});
		sleepTimeSlider.setOnKeyPressed((final KeyEvent event) -> {
			double sliderValue = sleepTimeSlider.getValue();
			this.sleepTime = (long) (200 - sliderValue);
		});
		sleepTimeSlider.setOnKeyReleased((final KeyEvent event) -> {
			double sliderValue = sleepTimeSlider.getValue();
			this.sleepTime = (long) (200 - sliderValue);
		});

		sleepTimeSlider.setLabelFormatter(new StringConverter<Double>() {
			@Override
			public String toString(final Double object) {
				Double speed = 200 - object;
				return String.valueOf(speed.longValue());
			}

			@Override
			public Double fromString(final String string) {
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
					int i = this.index;
					while (i >= 0) {

						this.eventBus.post(new StepEvent(true, 1));
						TimeUnit.MILLISECONDS.sleep(this.sleepTime);
						this.updateIndex(1, false);
						i = this.index;
						if (Thread.currentThread().isInterrupted()) {
							i = -1;
						}
					}
				}
			} catch (InterruptedException e) {

			}
		};
		if (this.playThread == null) {
			this.playThread = new Thread(run, "play");
			this.playThread.start();
		}
	}

	/**
	 * Creates the controll-buttons and adds them to the given List
	 *
	 * @param nodeList
	 *            A list which shall contain the nodes of the buttons
	 */
	private void fillToolbar(final List<Node> nodeList, final boolean eval) {
		/* playbutton*/
		Button playButton = new Button("Play");
		playButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent actionEvent) {
				FXCode.this.startPlayThread();
			}
		});

		nodeList.add(playButton);
		/* stepButton*/
		Button stepButton = new Button("Step");
		stepButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent actionEvent) {

				FXCode.this.eventBus.post(new StepEvent(true, 1));
				if (FXCode.this.index != FXCode.this.maxIndex) {
					FXCode.this.updateIndex(1, false);
				}
			}
		});
		nodeList.add(stepButton);

		/* stopButton*/
		Button stopButton = new Button("Stop");
		stopButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent actionEvent) {
				if (FXCode.this.playThread != null) {
					FXCode.this.playThread.interrupt();
					FXCode.this.playThread = null;
				}
			}
		});
		nodeList.add(stopButton);

		/* BackButton*/
		Button backButton = new Button("Back");
		backButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent actionEvent) {
				if (FXCode.this.index == 0) {
					return;
				}
				if (FXCode.this.index == 1) {
					FXCode.this.reset();
					return;
				}
				FXCode.this.eventBus.post(new StepEvent(false, 1));
				FXCode.this.updateIndex(-1, false);
			}
		});
		nodeList.add(backButton);

		/*  resetButton*/
		Button resetButton = new Button("reset");
		resetButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent actionEvent) {
				FXCode.this.reset();
			}
		});
		nodeList.add(resetButton);

		/* loadButton*/
		Button loadButton = new Button("load");
		loadButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent actionEvent) {
				FileChooser chooser = new FileChooser();
				File file = chooser.showOpenDialog(null);
				if (file != null) {
					FXCode.this.eventBus.post(new FileEvent(true, file));
				}
			}
		});
		nodeList.add(loadButton);

		/* saveButton*/
		Button saveButton = new Button("save");
		saveButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent actionEvent) {
				FileChooser chooser = new FileChooser();
				File file = chooser.showSaveDialog(null);
				if (file != null) {
					FXCode.this.eventBus.post(new FileEvent(false, file));
				}
			}
		});
		nodeList.add(saveButton);

		/*Colouring button*/
		this.colouringButton = new Button("colouring");
		this.colouringButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent actionEvent) {
				if (!FXCode.this.colouring) {
					FXCode.this.colouring = true;

				} else {
					FXCode.this.colouring = false;
				}
				FXCode.this.eventBus.post(new EnableColouring(FXCode.this.colouring));
			}
		});
		nodeList.add(this.colouringButton);
		if (!eval) {
			this.colouringButton.setDisable(true);
		}

	}

	/**
	 * Receive Info-Events from the recorder to update the timeline and the maximum index
	 *
	 * @param event
	 *            The info-event.
	 */
	@Subscribe
	public void receiveInfoEvent(final InfoEvent event) {
		try {
			this.maxIndex = event.getMaxIndex();

			this.sem.release();
			if (event.updateIndex()) {
				this.updateIndex(this.maxIndex, true);
			}
		} catch (NullPointerException e) {

		}
	}

	/**
	 * Updates the index if a new step is done. Depending on the type of step it is possible to either get the actual new index (<code>isRealIndex = true</code> or an additive one.
	 *
	 * @param newIndex
	 *            A variable which is used to compute the new index. Either it is the actual new index or this number has to be added to the current index.
	 * @param isRealIndex
	 *            <code>true</code> if the newIndex is the actual new index, <code>false</code> if newIndex is additive.
	 */
	private void updateIndex(int newIndex, final boolean isRealIndex) {
		if (!isRealIndex) {
			newIndex += this.index;
		}

		if (newIndex > this.maxIndex || newIndex < 0) {
			return;
		}

		this.index = newIndex;
		this.sem.release();
		if (this.index > this.maxDisplayIndex) {
			this.timeline.setValue(this.maxDisplayIndex);
			this.displayIndex = this.maxDisplayIndex;
		} else {
			this.timeline.setValue(this.index);
			this.displayIndex = this.index;
		}
		this.updateLog("Index: " + this.index);

	}

	/**
	 * Resets the GUI
	 */
	public void reset() {
		if (this.playThread != null) {
			this.playThread.interrupt();
			this.playThread = null;
		}
		this.updateIndex(0, true);
		this.visualization.reset();
		this.eventBus.post(new ResetEvent());
	}

	/**
	 * Jumps to a specific index at the timeline
	 *
	 * @param newIndex
	 */
	public void jumpToIndex(final int newIndex) {
		if (this.playThread != null) {
			this.playThread.interrupt();
			this.playThread = null;
		}
		if (newIndex == 0) {
			this.reset();
			return;
		}
		if (newIndex > this.index) {
			this.eventBus.post(new StepEvent(true, newIndex - this.index));
		} else {
			this.eventBus.post(new StepEvent(false, this.index - newIndex));
		}
		this.updateIndex(newIndex, true);
	}

	public TabPane getTabPane() {
		return this.tabPane;
	}

	/**
	 * Registers a new supplier to the Controller. In addition the cooresponging Visualizers are searched and also added.
	 *
	 * @param supplier
	 *            The new supplier.
	 */
	public void addDataSupplier(final ISupplier supplier) {

		try {
			ClassPath path = ClassPath.from(ClassLoader.getSystemClassLoader());
			Set<?> set = path.getAllClasses();
			try {
				set.stream().forEach(cls -> {
					if (cls instanceof ClassPath.ClassInfo) {
						/* search for a Visualizer.
						To identify a visualizer the package name has to contain .dataVisualizer.*/
						if (((ClassPath.ClassInfo) cls).getName().contains(".dataVisualizer.")) {
							IVisualizer v = (IVisualizer) this.findClassByName(((ClassPath.ClassInfo) cls).getName());
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
	 * @param name
	 *            The name of the searched class.
	 * @return
	 */
	private Object findClassByName(final String name) {
		try {
			Class<?> cls = Class.forName(name);
			if (cls.isInterface()) {
				return null;
			}
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
	public void receiveAddSupplierEvent(final AddSupplierEvent event) {
		this.addDataSupplier(event.getSupplier());
	}

	@Override
	public void mouseOver(final Object node) {

	}

	@Override
	public void mouseLeft(final Object node) {

	}

	@Override
	public void buttonReleased(final Object node) {
	}

	@Override
	public void buttonPushed(final Object node) {
		this.eventBus.post(new NodePushed<Object>(node));
	}

	/**
	 * Updates the log
	 *
	 * @param logEntry
	 *            the next log entry
	 */
	private void updateLog(final String logEntry) {
		String currentLog = this.log.getText() + "\n - " + logEntry;
		Platform.runLater(() -> {
			this.log.setText(currentLog);
		});

	}

	private void startUpdateRestriction(final long delay) {
		this.sem = new Semaphore(0);
		Runnable run = () -> {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Thread.sleep(delay);
					this.sem.acquire();
					try {
						this.updateTimelineIndex();
					} catch (IllegalArgumentException e) {
						//
					}
					this.sem.drainPermits();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
		this.updateThread = new Thread(run, "Update");
		this.updateThread.start();
	}

	/**
	 * Updates the timeline
	 */
	private void updateTimelineIndex() {
		this.timeline.setMax(this.maxIndex);
		int tickUnit = this.maxIndex / this.numberOfTicks;
		if (tickUnit != 0) {
			this.timeline.setMajorTickUnit(tickUnit);
		}
		this.maxDisplayIndex = this.maxIndex;
		if (this.displayIndex < this.maxDisplayIndex || this.displayIndex < this.index) {
			if (this.index <= this.maxDisplayIndex) {
				this.displayIndex = this.index;
			} else {
				this.displayIndex = this.maxDisplayIndex;
			}
			this.timeline.setValue(this.displayIndex);
		}

	}

	public int getNumberOfTicks() {
		return this.numberOfTicks;
	}

	public void setNumberOfTicks(final int numberOfTicks) {
		this.numberOfTicks = numberOfTicks;
	}

}