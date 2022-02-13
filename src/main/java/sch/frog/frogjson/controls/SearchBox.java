package sch.frog.frogjson.controls;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class SearchBox extends BorderPane {

    public SearchBox(BorderPane parentContainer, OnNextClick onNextClick, OnPreviousClick onPreviousClick) {
        this.setPadding(new Insets(5, 10, 5, 10));

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        ObservableList<Node> hBoxChildren = hBox.getChildren();

        Label label = new Label("Search:");
        HBox.setMargin(label, new Insets(0, 10, 0, 0));
        final TextField textField = new TextField();
        HBox.setMargin(textField, new Insets(0, 10, 0, 0));
        hBoxChildren.add(label);
        hBoxChildren.add(textField);

        Button next = new Button("Next");
        HBox.setMargin(next, new Insets(0, 10, 0, 0));
        hBoxChildren.add(next);

        Button previous = new Button("Previous");
        HBox.setMargin(previous, new Insets(0, 10, 0, 0));
        hBoxChildren.add(previous);

        this.setLeft(hBox);
        Button close = new Button("x");
        close.setOnAction(actionEvent -> parentContainer.setTop(null));
        this.setRight(close);

        next.setOnAction(event -> onNextClick.click(textField.getText()));
        textField.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ENTER){
                onNextClick.click(textField.getText());
            }
        });
        previous.setOnAction(event -> onPreviousClick.click(textField.getText()));
    }

    interface OnNextClick {
        void click(String keyword);
    }

    interface OnPreviousClick{
        void click(String keyword);
    }
}
