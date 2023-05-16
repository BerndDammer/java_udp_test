package udptest;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class MainframeControllerValues {
    public BooleanProperty disablePropertyStartButton;
    public BooleanProperty isRunning;
    public ObservableList<Worker.State> stateWorker;

    public ObjectProperty<EventHandler<ActionEvent>> onActionPropertyStartButton;
    public ObjectProperty<EventHandler<ActionEvent>> onActionPropertyStopButton;

    public StringProperty message;
    public StringProperty title;
    public DoubleProperty progress;
    public DoubleProperty totalWork;
    public IntegerProperty countIncomming;
    public StringProperty exception;

    public ObservableList<CanMsg> incomming;
    public ObservableList<CanMsg> outgoing;
}
