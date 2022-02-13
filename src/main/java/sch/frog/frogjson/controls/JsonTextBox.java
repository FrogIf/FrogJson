package sch.frog.frogjson.controls;

import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

public class JsonTextBox extends BorderPane {

    private final CodeArea codeArea = new CodeArea();

    private final SearchBox treeSearchBox;

    public JsonTextBox() {
        initCodeArea();
        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(codeArea);
        super.setCenter(scrollPane);
        treeSearchBox = new SearchBox(this, (text) -> {
        }, (text) -> {

        });
        this.setOnKeyPressed(keyEvent -> {
            if(keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.F){
                this.setTop(treeSearchBox);
                treeSearchBox.focusSearch();
            }
        });
    }

    private void initCodeArea(){
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.prefHeightProperty().bind(this.heightProperty());
        codeArea.prefWidthProperty().bind(this.widthProperty());
    }

    public String getContent() {
        return codeArea.getText();
    }

    public void setContent(String json) {
        codeArea.clear();
        codeArea.appendText(json);
    }
}
