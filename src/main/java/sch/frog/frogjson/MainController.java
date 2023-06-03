package sch.frog.frogjson;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sch.frog.frogjson.controls.JsonEditor;
import sch.frog.frogjson.json.JsonElement;
import sch.frog.frogjson.json.JsonOperator;
import sch.frog.frogjson.json.JsonParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private Label msgText;

    @FXML
    private TabPane mainTabPane;

    private MessageEmitter messageEmitter;

    @FXML
    protected void onCompactBtnClick() {
        this.editJson(origin -> {
            JsonElement jsonElement = JsonOperator.parse(origin);
            return jsonElement.toCompressString();
        });
    }

    @FXML
    protected void onPrettyBtnClick() {
        this.editJson(origin -> {
            JsonElement jsonElement = JsonOperator.parse(origin);
            return jsonElement.toPrettyString();
        });
    }

    @FXML
    protected void onToStringBtnClick() {
        this.editJson(origin -> {
            JsonElement jsonElement = JsonOperator.parse(origin);
            return JsonStringUtil.toString(jsonElement);
        });
    }

    @FXML
    protected void onFromStringBtnClick() {
        this.editJson(origin -> {
            JsonElement element = JsonStringUtil.fromString(origin);
            return element.toPrettyString();
        });
    }

    private void editJson(IEditStrategy strategy) {
        JsonEditor editor = getSelectEditContainer();
        messageEmitter.clear();
        if (editor != null) {
            String json = editor.getJson();
            if(json == null || json.isBlank()){
                return;
            }
            try {
                String result = strategy.edit(json);
                editor.setJsonContent(result);
            } catch (Exception e) {
                messageEmitter.emitError(e.getMessage());
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        messageEmitter = new MessageEmitter(msgText);
        EditTabManager.addTab(mainTabPane, messageEmitter);
        mainTabPane.setContextMenu(initTabPaneContextMenu(mainTabPane));
    }

    private interface IEditStrategy {
        String edit(String origin) throws Exception;
    }

    @FXML
    protected void onNewTabBtnClick() {
        EditTabManager.addTab(mainTabPane, this.messageEmitter);
    }

    @FXML
    protected void onTreeBtnClick() {
        JsonEditor editor = getSelectEditContainer();
        messageEmitter.clear();
        if (editor != null) {
            String json = editor.getJson();
            if (json == null || json.isBlank()) {
                editor.openTree(new TreeItem<>(new TreeNodeInfo(FrogJsonConstants.TREE_ROOT_NAME)));
                return;
            }
            TreeStructJsonWriter writer = new TreeStructJsonWriter();
            try {
                JsonElement jsonElement = JsonOperator.parse(json);
                jsonElement.customWrite(writer);
                TreeItem<TreeNodeInfo> root = writer.getRoot();
                editor.openTree(root);
            } catch (JsonParseException e) {
                messageEmitter.emitError(e.getMessage());
            }
        }
    }

    private JsonEditor getSelectEditContainer() {
        Tab selectedItem = mainTabPane.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            Node content = selectedItem.getContent();
            if (content instanceof JsonEditor) {
                return (JsonEditor) content;
            }
        }
        return null;
    }

    private Stage aboutStage = null;

    @FXML
    protected void onAboutBtnClick() throws IOException {
        if (aboutStage == null) {
            aboutStage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(FrogJsonApplication.class.getResource("about-view.fxml"));
            Scene secondScene = new Scene(fxmlLoader.load(), 300, 200);
            aboutStage.setScene(secondScene);
            aboutStage.resizableProperty().setValue(false);
            aboutStage.setTitle("About");
            aboutStage.getIcons().add(ImageResources.appIcon);
        }
        aboutStage.show();
        if (aboutStage.isIconified()) {
            aboutStage.setIconified(false);
        }else{
            aboutStage.requestFocus();
        }
    }

    private Stage renameStage = null;

    private void openRenameStage() {
        if(renameStage == null){
            renameStage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(FrogJsonApplication.class.getResource("rename-tab.fxml"));
            Scene secondScene = null;
            try {
                secondScene = new Scene(fxmlLoader.load(), 300, 200);
            } catch (IOException e) {
                GlobalExceptionThrower.INSTANCE.throwException(e);
            }
            final RenameTabController renameTabController = fxmlLoader.getController();
            renameTabController.setConfirmCallback(name -> {
                if(name == null){ return; }
                Tab selectTab = mainTabPane.getSelectionModel().getSelectedItem();
                if(selectTab != null){
                    selectTab.setText(name);
                }
            });
            renameStage.setScene(secondScene);
            renameStage.resizableProperty().setValue(false);
            renameStage.setTitle("Rename");
            renameStage.getIcons().add(ImageResources.appIcon);
            renameStage.initStyle(StageStyle.UTILITY);
            renameStage.setAlwaysOnTop(true);
            renameStage.setOnShown(event -> {
                Tab selectTab = mainTabPane.getSelectionModel().getSelectedItem();
                if(selectTab != null){
                    renameTabController.setOriginTabName(selectTab.getText());
                }
            });
        }
        renameStage.show();
        if(renameStage.isIconified()){  // 判断是否最小化
            renameStage.setIconified(false);
        }else{
            renameStage.requestFocus();
        }
    }

    private ContextMenu initTabPaneContextMenu(TabPane tabPane) {
        ContextMenu treeContextMenu = new ContextMenu();
        MenuItem closeSelect = new MenuItem("Close");
        closeSelect.setOnAction(actionEvent -> {
            Tab selectTab = tabPane.getSelectionModel().getSelectedItem();
            if(selectTab != null){
                tabPane.getTabs().remove(selectTab);
            }else{
                messageEmitter.emitWarn("no tab select");
            }
        });

        MenuItem closeOthers = new MenuItem("Close Other");
        closeOthers.setOnAction(actionEvent -> {
            Tab selectTab = tabPane.getSelectionModel().getSelectedItem();
            if(selectTab != null){
                ObservableList<Tab> tabs = tabPane.getTabs();
                tabs.clear();
                tabs.add(selectTab);
            }else{
                messageEmitter.emitWarn("no tab select");
            }
        });

        MenuItem closeAll = new MenuItem("Close All");
        closeAll.setOnAction(actionEvent -> {
            tabPane.getTabs().clear();
        });

        MenuItem closeToLeft = new MenuItem("Close to Left");
        closeToLeft.setOnAction(actionEvent -> {
            Tab selectTab = tabPane.getSelectionModel().getSelectedItem();
            if(selectTab != null){
                ObservableList<Tab> tabs = tabPane.getTabs();
                Iterator<Tab> iterator = tabs.iterator();
                while(iterator.hasNext()){
                    if(iterator.next() == selectTab){
                        break;
                    }
                    iterator.remove();
                }
            }else{
                messageEmitter.emitWarn("no tab select");
            }
        });

        MenuItem closeToRight = new MenuItem("Close to Right");
        closeToRight.setOnAction(actionEvent -> {
            Tab selectTab = tabPane.getSelectionModel().getSelectedItem();
            if(selectTab != null){
                ObservableList<Tab> tabs = tabPane.getTabs();
                Iterator<Tab> iterator = tabs.iterator();
                boolean startRemove = false;
                while(iterator.hasNext()){
                    Tab next = iterator.next();
                    if(startRemove){
                        iterator.remove();
                    }else{
                        startRemove = next == selectTab;
                    }
                }
            }else{
                messageEmitter.emitWarn("no tab select");
            }
        });

        MenuItem renameTab = new MenuItem("Rename");
        renameTab.setOnAction(actionEvent -> {
            Tab selectTab = tabPane.getSelectionModel().getSelectedItem();
            if(selectTab != null){
                openRenameStage();
            }else{
                messageEmitter.emitWarn("no tab select");
            }
        });

        ObservableList<MenuItem> items = treeContextMenu.getItems();
        items.add(renameTab);
        items.add(closeOthers);
        items.add(closeAll);
        items.add(closeToLeft);
        items.add(closeToRight);
        items.add(closeSelect);
        return treeContextMenu;
    }

    private File loadDir = null;

    @FXML
    protected void onLoadClick(){
        FileChooser fileChooser = new FileChooser();
        if(this.loadDir != null){
            fileChooser.setInitialDirectory(this.loadDir);  // 指定上次加载路径为当前加载路径
        }
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("ALL files (*.*)", "*.*");
        fileChooser.getExtensionFilters().add(extFilter);
        List<File> files = fileChooser.showOpenMultipleDialog(FrogJsonApplication.self.getPrimaryStage());
        if(files != null){
            if(!files.isEmpty()){
                File file = files.get(0);
                if(file.isFile()){
                    File dir = file.getParentFile();
                    if(dir.isDirectory()){
                        this.loadDir = dir;
                    }
                }
            }
            for (File file : files) {
                EditTabManager.TabElement tabElement = EditTabManager.addTab(mainTabPane, file.getName(), this.messageEmitter);
                try (
                        FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8);
                        BufferedReader reader = new BufferedReader(fileReader)
                        ){
                    StringBuilder sb = new StringBuilder();
                    String line;
                    boolean start = true;
                    while((line = reader.readLine()) != null){
                        if(!start){
                            sb.append('\n');
                        }else{
                            start = false;
                        }
                        sb.append(line);
                    }
                    tabElement.getJsonEditor().setJsonContent(sb.toString());
                } catch (IOException e) {
                    messageEmitter.emitError(e.getMessage());
                }
            }
        }
    }
}