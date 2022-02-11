package sch.frog.frogjson.controls;

import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.util.Iterator;
import java.util.Stack;

public class JsonEditor extends SplitPane {

    private final CodeArea codeArea = new CodeArea();

    private TreeView<String> treeView;

    public JsonEditor() {
        super();
        this.setOrientation(Orientation.HORIZONTAL);
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

    private ContextMenu initContextMenu(final TreeView<String> treeView) {
        ContextMenu treeContextMenu = new ContextMenu();
        MenuItem copy = new MenuItem("Copy");
        copy.setOnAction(actionEvent -> {
            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                String value = selectedItem.getValue();
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(value);
                clipboard.setContent(content);
            }
        });
        MenuItem collapse = new MenuItem("Collapse");
        collapse.setOnAction(actionEvent -> {
            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if(selectedItem != null){
                collapseOrExpand(selectedItem, false);
            }
        });
        MenuItem expand = new MenuItem("Expand");
        expand.setOnAction(actionEvent -> {
            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if(selectedItem != null){
                collapseOrExpand(selectedItem, true);
            }
        });
        ObservableList<MenuItem> items = treeContextMenu.getItems();
        items.add(copy);
        items.add(expand);
        items.add(collapse);
        return treeContextMenu;
    }

    private void collapseOrExpand(TreeItem<String> treeItem, boolean expand){
        treeItem.setExpanded(expand);
        ObservableList<TreeItem<String>> items = treeItem.getChildren();
        if(items != null && !items.isEmpty()){
            Stack<Iterator<TreeItem<String>>> rubberBand = new Stack<>();
            rubberBand.push(items.iterator());
            while(!rubberBand.isEmpty()){
                while(rubberBand.peek().hasNext()){
                    TreeItem<String> item = rubberBand.peek().next();
                    item.setExpanded(expand);
                    ObservableList<TreeItem<String>> children = item.getChildren();
                    if(children != null && !children.isEmpty()){
                        rubberBand.push(children.iterator());
                    }
                }
                rubberBand.pop();
            }
        }

    }

    public String getJson() {
        return codeArea.getText();
    }

    public void setJsonContent(String json) {
        codeArea.clear();
        codeArea.appendText(json);
    }

    public void openTree(TreeItem<String> root) {
        if (treeView == null) {
            treeView = new TreeView<>();
            treeView.setContextMenu(initContextMenu(treeView));
            super.getItems().add(treeView);
        }
        treeView.setRoot(root);
        if (root != null) {
            root.setExpanded(true);
        }
    }
}
