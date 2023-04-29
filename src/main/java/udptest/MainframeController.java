package udptest;

import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.scene.layout.GridPane;

public class MainframeController extends MainframeControllerValues {
//implements NonFXThreadEventReciever {

//	private final FXTimer transmittWorker = new FXTimer(this::onTransmitt);
    private final WebsocketStringService websocketService = new WebsocketStringService();
    private final GridPane rootNode;

    public MainframeController() {
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
    }

    public void onStop(ActionEvent event) {
        websocketService.cancel();
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

//	@Override
//	public void xonNewText() {
//		Platform.runLater(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					final String newValue = websocketService.getSourceQueue().take();
//
//					if (itemsReceiveLogger.get().size() > General.LOG_AUTODELETE)
//						itemsReceiveLogger.get().clear();
//					itemsReceiveLogger.get().add(newValue);
//
//					JsonReader factory = Json.createReader(new StringReader(newValue));
//					JsonStructure js = factory.read();
//					JsonObject job = (JsonObject) js;
//					{
//						JsonArray hs = job.getJsonArray("H");
//						bottomSensors[0].setValue(((JsonNumber) hs.get(0)).doubleValue() / 32768.0);
//						bottomSensors[1].setValue(((JsonNumber) hs.get(1)).doubleValue() / 32768.0);
//						bottomSensors[2].setValue(((JsonNumber) hs.get(2)).doubleValue() / 32768.0);
//					}
//					mileage.setValue("Mil: " + job.getInt("C"));
//					speed.setValue("Speed: " + job.getInt("B"));
//					{
//						JsonArray hs = job.getJsonArray("D");
//						int k = hs.getInt(0);
//						int v = hs.getInt(1);
//						sonics.get().put(k, v);
//					}
//				} catch (Exception e) {
//					itemsReceiveLogger.get().add("Json Parsing Problem");
//				}
//			}
//		}); // end of runnable
//	}
}
