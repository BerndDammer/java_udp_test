package udptest;

import java.io.IOException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FXMainApp extends Application {
    public void start(Stage stage) throws IOException {
        final MainframeController mfc = new MainframeController();
        Scene scene = new Scene(mfc.getRootNode(), mfc.getRootNode().getPrefWidth(), mfc.getRootNode().getPrefHeight());
        stage.setTitle("UDP Multicast Test");
        stage.setScene(scene);
        stage.show();
    }
}