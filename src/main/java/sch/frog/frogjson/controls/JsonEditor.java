package sch.frog.frogjson.controls;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import sch.frog.frogjson.MessageEmitter;

public class JsonEditor extends SplitPane {

    private final CodeArea codeArea = new CodeArea();

    private JsonTreeBox jsonTreeBox;

    private final MessageEmitter messageEmitter;

    public JsonEditor(MessageEmitter messageEmitter) {
        super();
        this.messageEmitter = messageEmitter;
        ObservableList<Node> items = super.getItems();
        initCodeArea();
        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(codeArea);
        items.add(scrollPane);
    }

    private void initCodeArea(){
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.prefHeightProperty().bind(this.heightProperty());
        codeArea.prefWidthProperty().bind(this.widthProperty());
    }

    public String getJson() {
        return codeArea.getText();
    }

    public void setJsonContent(String json) {
        codeArea.clear();
        codeArea.appendText(json);
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
