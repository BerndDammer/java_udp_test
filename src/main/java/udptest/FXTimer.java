package udptest;

import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.util.Duration;

public class FXTimer {

    private final Timeline timeline = new Timeline();

    private final Runnable timed;

    public FXTimer(final Runnable timed) {
        this.timed = timed;
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.getKeyFrames().add(new KeyFrame(General.COMMAND_DELAY_MS, this::onKeyFrame));
    }

    private void onKeyFrame(ActionEvent event) {
        timed.run();
    }

    public void setEnabled(boolean enable) {
        if (enable) {
            switch (timeline.getStatus()) {
            case PAUSED:
            case STOPPED:
                timeline.playFromStart();
                break;
            case RUNNING: // do nothing
                break;
            }
        } else {
            timeline.stop();
        }
    }

    public void setRate(final Duration time) {
        boolean running = timeline.getStatus() == Status.RUNNING;
        if (running)
            timeline.stop();
        timeline.getKeyFrames().clear();
        timeline.getKeyFrames().add(new KeyFrame(time, this::onKeyFrame));
        if (running)
            timeline.playFromStart();
    }
}
