package sch.frog.frogjson.controls;

import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import sch.frog.frogjson.MessageEmitter;
import sch.frog.frogjson.TreeNodeInfo;

public class JsonEditor extends SplitPane {

    private JsonTreeBox jsonTreeBox;

    private final JsonTextBox jsonTextBox;

    private final MessageEmitter messageEmitter;

    public JsonEditor(MessageEmitter messageEmitter) {
        super();
        this.messageEmitter = messageEmitter;
        jsonTextBox = new JsonTextBox(this.messageEmitter);
        this.getItems().add(jsonTextBox);
    }

    public String getJson() {
        return jsonTextBox.getContent();
    }

    public void setJsonContent(String json) {
        jsonTextBox.setContent(json);
    }

    public void openTree(TreeItem<TreeNodeInfo> root) {
        if(jsonTreeBox == null){
            jsonTreeBox = new JsonTreeBox(this.messageEmitter, this);
            super.getItems().add(jsonTreeBox);
        }
        jsonTreeBox.setTreeRoot(root);
        int maxLen = 0;
        for (TreeItem<TreeNodeInfo> child : root.getChildren()) {
            TreeNodeInfo nodeInfo = child.getValue();
            String value = nodeInfo.getValue();
            if(value != null){
                maxLen = Math.max(value.length(), maxLen);
            }
        }
        root.setExpanded(maxLen < 5000);
    }

    public void closeTree(){
        if(jsonTreeBox != null){
            super.getItems().remove(jsonTreeBox);
            jsonTreeBox = null;
        }
    }

}
