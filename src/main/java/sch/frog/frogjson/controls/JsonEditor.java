package sch.frog.frogjson.controls;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import sch.frog.frogjson.MessageEmitter;

import java.util.Iterator;
import java.util.Stack;

public class JsonEditor extends SplitPane {

    private final CodeArea codeArea = new CodeArea();

    private TreeView<String> treeView;

    private MessageEmitter messageEmitter;

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
        foreachTree(treeItem, node -> {
            node.setExpanded(expand);
            return true;
        });
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
            treeSearchForeachActionForNext = new TreeSearchForeachActionForNext(this.treeView, this.messageEmitter);
            treeSearchForeachActionForPrevious = new TreeSearchForeachActionForPrevious(this.treeView, this.messageEmitter);
            treeView.setContextMenu(initContextMenu(treeView));
            VBox vBox = new VBox();
            ObservableList<Node> children = vBox.getChildren();
            children.add(treeView);
            VBox.setVgrow(treeView, Priority.ALWAYS);
            children.add(searchBox());
            super.getItems().add(vBox);
        }
        treeView.setRoot(root);
        if (root != null) {
            root.setExpanded(true);
        }
    }

    private void foreachTree(TreeItem<String> start, ForeachAction action){
        if(!action.doSomething(start)){
            return;
        }
        ObservableList<TreeItem<String>> items = start.getChildren();
        if(items != null && !items.isEmpty()){
            Stack<Iterator<TreeItem<String>>> rubberBand = new Stack<>();
            rubberBand.push(items.iterator());
            while(!rubberBand.isEmpty()){
                while(rubberBand.peek().hasNext()){
                    TreeItem<String> item = rubberBand.peek().next();
                    if(!action.doSomething(item)){
                        return;
                    }
                    ObservableList<TreeItem<String>> children = item.getChildren();
                    if(children != null && !children.isEmpty()){
                        rubberBand.push(children.iterator());
                    }
                }
                rubberBand.pop();
            }
        }
        action.searchFinish();
    }

    private interface ForeachAction{
        boolean doSomething(TreeItem<String> node);
        default void searchFinish(){ /* do nothing */ }
    }

    private HBox searchBox(){
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
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

        next.setOnAction(actionEvent -> {
            this.messageEmitter.clear();
            searchNextForTree(textField.getText(), this.messageEmitter);
        });
        previous.setOnAction(actionEvent -> {
            this.messageEmitter.clear();
            searchPreviousForTree(textField.getText(), this.messageEmitter);
        });
        return hBox;
    }

    private TreeSearchForeachActionForNext treeSearchForeachActionForNext;

    private TreeSearchForeachActionForPrevious treeSearchForeachActionForPrevious;

    private void searchNextForTree(String text, MessageEmitter emitter){
        if(text == null || text.isEmpty()){
            emitter.emitWarn("please input search keyword");
            return;
        }
        TreeItem<String> start = treeView.getSelectionModel().getSelectedItem();
        TreeItem<String> root = treeView.getRoot();
        if(start == null){
            start = root;
        }
        if(start != null){
            treeSearchForeachActionForNext.prepareSearch(start, text);
            foreachTree(root, treeSearchForeachActionForNext);
        }
    }

    private void searchPreviousForTree(String text, MessageEmitter emitter){
        if(text == null || text.isEmpty()){
            emitter.emitWarn("please input search keyword");
            return;
        }
        TreeItem<String> start = treeView.getSelectionModel().getSelectedItem();
        TreeItem<String> root = treeView.getRoot();
        if(start == null){
            start = root;
        }
        if(start != null){
            treeSearchForeachActionForPrevious.prepareSearch(start, text);
            foreachTree(root, treeSearchForeachActionForPrevious);
        }
    }

    private static class TreeSearchForeachActionForNext implements ForeachAction{

        private TreeItem<String> start;

        private final TreeView<String> tree;

        private String searchText;

        private final MessageEmitter emitter;

        private boolean reachBottom = false;

        public TreeSearchForeachActionForNext(TreeView<String> treeView, MessageEmitter emitter) {
            this.tree = treeView;
            this.emitter = emitter;
        }

        boolean matchStart = false;

        @Override
        public boolean doSomething(TreeItem<String> node) {
            if(start == node){
                matchStart = true;
            }else if(matchStart){
                String value = node.getValue();
                if(value != null && value.contains(searchText)){
                    tree.getSelectionModel().select(node);
                    tree.scrollTo(tree.getRow(node));
                    return false;
                }
            }
            return true;
        }

        @Override
        public void searchFinish() {
            emitter.emitWarn("search reach bottom");
            this.reachBottom = true;
        }

        public void prepareSearch(TreeItem<String> start, String text){
            this.start = start;
            if(this.searchText != null && this.searchText.equals(text) && this.reachBottom){
                this.start = tree.getRoot();
            }
            this.searchText = text;
            this.matchStart = false;
            this.reachBottom = false;
        }
    }

    private static class TreeSearchForeachActionForPrevious implements ForeachAction{

        TreeItem<String> preMatch;

        TreeItem<String> start;

        TreeView<String> tree;

        String searchText;

        MessageEmitter emitter;

        boolean reachTop;

        public TreeSearchForeachActionForPrevious(TreeView<String> treeView, MessageEmitter emitter) {
            this.tree = treeView;
            this.emitter = emitter;
        }

        @Override
        public boolean doSomething(TreeItem<String> node) {
            String value = node.getValue();
            if(start != node && value != null && value.contains(searchText)){
                preMatch = node;
            }
            if(start == node){
                if(preMatch != null){
                    tree.getSelectionModel().select(preMatch);
                    tree.scrollTo(tree.getRow(preMatch));
                }else{
                    this.reachTop = true;
                    emitter.emitWarn("search reach top");
                }
                return false;
            }else{
                return true;
            }
        }

        @Override
        public void searchFinish() {
            if(start == null && this.preMatch != null){ // 说明从最后一个开始向前搜索
                tree.getSelectionModel().select(preMatch);
                tree.scrollTo(tree.getRow(preMatch));
            }
        }

        public void prepareSearch(TreeItem<String> start, String text){
            this.start = start;
            if(this.searchText != null && this.searchText.equals(text) && this.reachTop){
                this.start = null;
            }
            this.searchText = text;
            this.preMatch = null;
            this.reachTop = false;
        }
    }


}
