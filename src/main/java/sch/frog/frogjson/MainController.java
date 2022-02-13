package sch.frog.frogjson;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import sch.frog.frogjson.controls.JsonEditor;
import sch.frog.frogjson.json.JsonElement;
import sch.frog.frogjson.json.JsonOperator;
import sch.frog.frogjson.json.JsonParseException;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private Label msgText;

    @FXML
    private TabPane mainTabPane;

    @FXML
    private TextField tabTitleText;

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
    }

    private interface IEditStrategy {
        String edit(String origin) throws Exception;
    }

    @FXML
    protected void onNewTabBtnClick() {
        String tabTitle = tabTitleText.getText();
        tabTitleText.setText(null);
        EditTabManager.addTab(mainTabPane, tabTitle, this.messageEmitter);
    }

    @FXML
    protected void onTreeBtnClick() {
        JsonEditor editor = getSelectEditContainer();
        messageEmitter.clear();
        if (editor != null) {
            String json = editor.getJson();
            if (json == null || json.isBlank()) {
                editor.openTree(new TreeItem<>(FrogJsonConstants.TREE_ROOT_NAME));
                return;
            }
            TreeStructJsonWriter writer = new TreeStructJsonWriter();
            try {
                JsonElement jsonElement = JsonOperator.parse(json);
                jsonElement.customWrite(writer);
                TreeItem<String> root = writer.getRoot();
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
    protected void onAboutBtnClick() {
        if (aboutStage == null) {
            aboutStage = new Stage();
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(FrogJsonApplication.class.getResource("about-view.fxml"));
                Scene secondScene = new Scene(fxmlLoader.load(), 300, 200);
                aboutStage.setScene(secondScene);
                aboutStage.resizableProperty().setValue(false);
                aboutStage.setTitle("About");
                aboutStage.getIcons().add(ImageResources.appIcon);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        aboutStage.show();
        if (aboutStage.isIconified()) {
            aboutStage.setIconified(false);
        }else{
            aboutStage.requestFocus();
        }
    }
}