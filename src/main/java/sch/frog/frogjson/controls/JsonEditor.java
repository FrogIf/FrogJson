package sch.frog.frogjson.controls;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import sch.frog.frogjson.MessageEmitter;

public class JsonEditor extends SplitPane {

    private JsonTreeBox jsonTreeBox;

    private final JsonTextBox jsonTextBox;

    private final MessageEmitter messageEmitter;

    public JsonEditor(MessageEmitter messageEmitter) {
        super();
        this.messageEmitter = messageEmitter;
        ObservableList<Node> items = this.getItems();
        jsonTextBox = new JsonTextBox();
        items.add(jsonTextBox);
    }

    public String getJson() {
        return jsonTextBox.getContent();
    }

    public void setJsonContent(String json) {
        jsonTextBox.setContent(json);
    }

    public void openTree(TreeItem<String> root) {
        if (jsonTreeBox == null) {
            jsonTreeBox = new JsonTreeBox(this.messageEmitter);
            super.getItems().add(jsonTreeBox);
        }
        jsonTreeBox.setTreeRoot(root);
        if (root != null) {
            root.setExpanded(true);
        }
    }

}
