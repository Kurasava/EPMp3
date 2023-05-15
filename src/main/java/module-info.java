module kurasava.epmp3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens kurasava.epmp3 to javafx.fxml;
    exports kurasava.epmp3;
}