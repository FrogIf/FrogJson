package sch.frog.frogjson;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import sch.frog.frogjson.util.JsonXmlUtils;
import sch.frog.frogjson.util.JsonYamlUtils;
import sch.frog.frogjson.util.StringUtils;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ExtendController implements Initializable {

    private SetText setText;

    private GetText getText;

    @FXML
    private VBox textContainer;

    @FXML
    private Label msgLabel;

    private MessageEmitter messageEmitter;

    private final CodeArea codeArea = new CodeArea();

    @FXML
    protected void onFromXMLClick(){
        String text = codeArea.getText();
        if(StringUtils.isBlank(text)){ return; }
        try{
            this.setText.set(JsonXmlUtils.xml2Json(text));
        }catch (Exception e){
            messageEmitter.emitError(e.getMessage());
        }
    }

    @FXML
    public void onToXMLClick() {
        String json = this.getText.get();
        if(StringUtils.isBlank(json)){
            return;
        }
        try{
            String text = JsonXmlUtils.json2Xml(json);
            codeArea.clear();
            codeArea.appendText(text);
        }catch (Exception e){
            messageEmitter.emitError(e.getMessage());
        }
    }

    @FXML
    public void onToYAMLClick() {
        String json = this.getText.get();
        if(StringUtils.isBlank(json)){
            return;
        }
        String yml = JsonYamlUtils.json2Yaml(json);
        this.codeArea.clear();
        this.codeArea.appendText(yml);
    }

    @FXML
    public void onFromYAMLClick() {
        String text = this.codeArea.getText();
        if(StringUtils.isBlank(text)){
            return;
        }
        try{
            List<String> jsonList = JsonYamlUtils.yaml2Json(text);
            for (String s : jsonList) {
                this.setText.set(s);
            }
        }catch (Exception e){
            messageEmitter.emitError(e.getMessage());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.messageEmitter = new MessageEmitter(msgLabel);
        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(codeArea);
        textContainer.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
    }

    public void setSetText(SetText setText) {
        this.setText = setText;
    }

    public void setGetText(GetText getText) {
        this.getText = getText;
    }

    public interface SetText{
        void set(String text);
    }

    public interface GetText{
        String get();
    }
}
