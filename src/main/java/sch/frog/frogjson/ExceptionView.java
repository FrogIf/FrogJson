package sch.frog.frogjson;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionView extends VBox {

    private final Label messageLabel;

    private final TextArea stacktraceBox;

    public ExceptionView() {
        this.setPadding(new Insets(20, 20, 20, 20));
        ObservableList<Node> children = this.getChildren();

        HBox hBox = new HBox();
        ObservableList<Node> hBoxChildren = hBox.getChildren();
        Label messageTitle = new Label("Message:");
        hBoxChildren.add(messageTitle);
        messageLabel = new Label();
        hBoxChildren.add(messageLabel);
        children.add(hBox);

        VBox vBox = new VBox();
        VBox.setVgrow(vBox, Priority.ALWAYS);
        ObservableList<Node> vBoxChildren = vBox.getChildren();
        Label stackTitle = new Label("Exception stacktrace:");
        vBoxChildren.add(stackTitle);

        stacktraceBox = new TextArea();
        stacktraceBox.setEditable(false);
        VBox.setVgrow(stacktraceBox, Priority.ALWAYS);
        vBoxChildren.add(stacktraceBox);

        VBox.setMargin(vBox, new Insets(20, 0, 0, 0));
        children.add(vBox);
    }

    public void setException(Throwable e){
        messageLabel.setText(e.getMessage());
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        stacktraceBox.setText(writer.toString());

        Toolkit.getDefaultToolkit().beep();
    }

}
