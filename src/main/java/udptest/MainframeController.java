package udptest;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import netinf.NetworkInterfaceView;
import udptest.WebsocketStringService.NonFXThreadEventReciever;

public class MainframeController extends MainframeControllerValues implements NonFXThreadEventReciever {
//implements NonFXThreadEventReciever {

	private final FXTimer transmittWorker = new FXTimer(this::onTransmitt);
    private final WebsocketStringService websocketService = new WebsocketStringService(this);
    private final GridPane rootNode;
    private final CanMsgHeartbeat canMsgHeartbeat = new CanMsgHeartbeat();
    
    public MainframeController() {
        new NetworkInterfaceView().doit();

        rootNode = new MainframeLoader(this);

        //////////////////////////////
        /////////// Action Connections
        onActionPropertyStartButton.setValue(this::onStart);
        onActionPropertyStopButton.setValue(this::onStop);

        message.bind(websocketService.messageProperty());
        title.bind(websocketService.titleProperty());

        websocketService.stateProperty().addListener(this::onNewWorkerState);
        
    }

    public void onStart(ActionEvent event) {
        websocketService.reset();
        websocketService.start();
        transmittWorker.setRate(Duration.millis(2300));
        transmittWorker.setEnabled(true);
    }

    public void onStop(ActionEvent event) {
        websocketService.cancel();
        transmittWorker.setEnabled(false);
    }

    void onNewWorkerState(ObservableValue<? extends Worker.State> observable, Worker.State oldValue,
            Worker.State newValue) {

        // stateWorker.setValue(newValue);
        switch (newValue) {
        case FAILED: {
            Throwable t = websocketService.getException();
            t.printStackTrace();
            disablePropertyStartButton.setValue(false);
        }
            break;
        case CANCELLED:
        case READY:
        case SCHEDULED:
        case SUCCEEDED:
            disablePropertyStartButton.setValue(false);
            break;
        case RUNNING:
            disablePropertyStartButton.setValue(true);
            break;
        }
    }

    public GridPane getRootNode() {
        return rootNode;
    }

    private void onTransmitt()
    {
        CanMsg canMsg = canMsgHeartbeat.get();
        if (outgoing.size() > General.LOG_AUTODELETE) {
            outgoing.clear();
        }
        outgoing.add(canMsg);
        websocketService.sendMsg(canMsg);
    }

    @Override
    public void xonNewText(final CanMsg canMsg) {
//        Platform.runLater(() -> {
//            CanMsg newValue;
//            try {
//                newValue = websocketService.getSourceQueue().take();
//                if (incomming.size() > General.LOG_AUTODELETE)
//                    incomming.clear();
//                incomming.add(newValue);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });
        Platform.runLater(() -> {
            if (incomming.size() > General.LOG_AUTODELETE) {
                incomming.clear();
            }
            incomming.add(canMsg);
            canMsgHeartbeat.got(canMsg);
        });
    }
}
