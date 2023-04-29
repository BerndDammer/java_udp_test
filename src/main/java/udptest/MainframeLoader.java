package udptest;

import javafx.concurrent.Worker;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

public class MainframeLoader extends GridPane2 {

    public MainframeLoader(final MainframeControllerValues mainframeControllerValues) {
        super(true);
        final ChoiceBox<Worker.State> transmitSpeed = new ChoiceBox<>();

        final Button startButton = new Button("Start");
        final Button stopButton = new Button("Stop");
        final Label message = new Label("XXXXXXXX");
        final Label title = new Label("WWWWWW");

        final Label progress = new Label();
        final Label totalWork = new Label();
        final Label countIncomming = new Label();
        final Label exception = new Label();

        // colum row
        add(new Label("Title"), 0, 0);
        add(title, 1, 0);

        add(new Label("Message"), 0, 1);
        add(message, 1, 1);

        add(startButton, 0, 8, INSERTING.CENTER);
        add(stopButton, 1, 8, INSERTING.CENTER);

        ///////////////////////////////
        //// Make Property connections
        mainframeControllerValues.disablePropertyStartButton = startButton.disableProperty();
        mainframeControllerValues.onActionPropertyStartButton = startButton.onActionProperty();
        mainframeControllerValues.onActionPropertyStopButton = stopButton.onActionProperty();

        mainframeControllerValues.message = message.textProperty();
        mainframeControllerValues.title = title.textProperty();
        mainframeControllerValues.stateWorker = transmitSpeed.getItems();
    }
}
