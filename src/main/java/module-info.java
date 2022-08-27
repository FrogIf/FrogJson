module sch.frog.frogjson {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;
    requires java.desktop;
    requires reactfx;

    opens sch.frog.frogjson to javafx.fxml;
    exports sch.frog.frogjson;
    exports sch.frog.frogjson.controls;
    opens sch.frog.frogjson.controls to javafx.fxml;
}