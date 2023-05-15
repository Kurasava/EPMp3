package kurasava.epmp3;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.Objects;

public class EPMp3 extends Application {

    private MediaPlayer mediaPlayer;
    private File file;

    private Button loopButton;
    private Button openButton;
    private Button playButton;
    private Button pauseButton;
    private Button stopButton;

    private Slider volumeSlider;
    private Slider progressSlider;

    private Label statusLabel;
    private Label timeLabel;

    private boolean isLooping = false;
    private boolean isChangingSlider = false;

    @Override
    public void start(Stage primaryStage) {
        this.openButton = new Button("Открыть");
        this.openButton.setOnAction(event -> openFile(primaryStage));
        this.playButton = new Button("Играть");
        this.playButton.setOnAction(event -> play());
        this.pauseButton = new Button("Пауза");
        this.pauseButton.setOnAction(event -> pause());
        this.stopButton = new Button("Стоп");
        this.stopButton.setOnAction(event -> stop());
        this.loopButton = new Button("Зациклить");
        this.loopButton.setOnAction(event -> toggleLoop());
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png")));
        String css = Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm();
        primaryStage.getIcons().add(icon);
        this.progressSlider = new Slider();
        this.progressSlider.setMin(0);
        this.progressSlider.setMax(100);
        this.progressSlider.setMaxWidth(300);
        this.progressSlider.setDisable(true);
        this.progressSlider.valueChangingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.isChangingSlider = true;
                if (this.mediaPlayer != null) {
                    this.mediaPlayer.seek(Duration.seconds(this.progressSlider.getValue()));
                }
            } else {
                isChangingSlider = false;
            }
        });
        this.timeLabel = new Label("0:00 / 0:00");
        this.volumeSlider = new Slider();
        this.volumeSlider.setMin(0.0001);
        this.volumeSlider.setMax(0.3);
        this.volumeSlider.setMaxWidth(70);
        this.volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (this.mediaPlayer != null) {
                this.mediaPlayer.setVolume(newValue.doubleValue());
            }
        });
        this.statusLabel = new Label(" ");
        VBox root = new VBox(openButton, playButton, pauseButton, stopButton,
                this.volumeSlider, this.loopButton, new VBox(this.progressSlider, this.timeLabel), this.statusLabel);
        root.setSpacing(10);
        root.setPrefSize(400, 250);
        Scene scene = new Scene(root);
        scene.getStylesheets().clear();
        scene.getStylesheets().add(css);
        primaryStage.setTitle("EPMp3");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Открыть mp3 файл");
        this.file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Media media = new Media(file.toURI().toString());
            if (this.mediaPlayer != null) {
                this.mediaPlayer.stop();
            }

            this.mediaPlayer = new MediaPlayer(media);
            this.mediaPlayer.setOnReady(() -> {
                this.progressSlider.setDisable(false);
                this.progressSlider.setMax(this.mediaPlayer.getTotalDuration().toSeconds());
                this.mediaPlayer.setVolume(this.volumeSlider.getValue());
                updateTimeLabel();
            });
            this.mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                if (!this.progressSlider.isValueChanging()) {
                    this.progressSlider.setValue(newValue.toSeconds());
                }

                updateTimeLabel();
            });
            this.progressSlider.setOnMousePressed(event -> {
                this.mediaPlayer.seek(Duration.seconds(this.progressSlider.getValue()));
                this.mediaPlayer.pause();
            });
            this.progressSlider.setOnMouseReleased(event -> {
                this.mediaPlayer.seek(Duration.seconds(this.progressSlider.getValue()));
                this.mediaPlayer.play();
            });
            this.mediaPlayer.play();
            this.statusLabel.setText("Играет: " + file.getName());
        }
    }
    private void toggleLoop() {
        this.isLooping = !this.isLooping;
        if (this.isLooping) {
            this.mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            this.loopButton.setText("Зациклено");
        } else {
            this.mediaPlayer.setCycleCount(1);
            this.loopButton.setText("Не зациклено");
        }
    }

    private void updateTimeLabel() {
        if (this.mediaPlayer != null) {
            Duration currentTime = this.mediaPlayer.getCurrentTime();
            Duration endTime = this.mediaPlayer.getTotalDuration();
            String timeString = String.format("%02d:%02d / %02d:%02d",
                    (int)currentTime.toMinutes(), (int)currentTime.toSeconds()%60,
                    (int)endTime.toMinutes(), (int)endTime.toSeconds()%60);
            this.timeLabel.setText(timeString);
        }
    }

    private void play() {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.play();
            this.statusLabel.setText("Играет: " + this.file.getName());
        }
    }


    private void pause() {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.pause();
            this.statusLabel.setText("На паузе: " + this.file.getName());
        }
    }

    public void stop() {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.stop();
            this.statusLabel.setText("Остановлено");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
