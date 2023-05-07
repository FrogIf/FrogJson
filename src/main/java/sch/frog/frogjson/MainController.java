package sch.frog.frogjson;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import sch.frog.frogjson.json.JsonElement;
import sch.frog.frogjson.json.JsonLexicalAnalyzer;
import sch.frog.frogjson.json.JsonOperator;
import sch.frog.frogjson.json.JsonParseException;
import sch.frog.frogjson.json.JsonToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController implements Initializable {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private String testJson = null;

    private CodeArea codeArea;

    @FXML
    private VBox mainBox;

    @FXML
    protected void onTestClick(){
        codeArea.clear();
        codeArea.appendText(testJson);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.codeArea = new CodeArea();
        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(codeArea);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        mainBox.getChildren().add(scrollPane);

        // ---- highlight ----
        codeArea.multiPlainChanges()
                .successionEnds(Duration.ofMillis(500))
                .retainLatestUntilLater(executor)
                .supplyTask(() ->
                        computeHighlightingAsync(codeArea)
                )
                .awaitLatest(codeArea.multiPlainChanges())
                .filterMap(t -> {
                    if(t.isSuccess()) {
                        return Optional.of(t.get());
                    } else {
                        t.getFailure().printStackTrace();
                        return Optional.empty();
                    }
                })
                .subscribe((highlighting) -> {
                    System.out.println("span count : " + highlighting.getSpanCount());
                    codeArea.setStyleSpans(0, highlighting);
                });

        // shutdown executor when application exit
        GlobalApplicationLifecycleUtil.addOnCloseListener(executor::shutdown);
    }

    private Task<StyleSpans<Collection<String>>> computeHighlightingAsync(CodeArea codeArea) {
        Task<StyleSpans<Collection<String>>> task = new Task<>() {
            @Override
            protected StyleSpans<Collection<String>> call() {
                return computeHighlighting(codeArea);
            }
        };
        executor.execute(task);
        return task;
    }

    private StyleSpans<Collection<String>> computeHighlighting(CodeArea codeArea) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        List<JsonToken> tokens = JsonLexicalAnalyzer.lexicalAnalysis(codeArea.getText(), false);
        if(tokens.isEmpty()){
            spansBuilder.add(Collections.emptyList(), codeArea.getText().length());
            return spansBuilder.create();
        }
        ArrayList<String> preStyles = null;
        ArrayList<String> cursorStyles = null;
        for (JsonToken token : tokens) {
            JsonToken.Type type = token.getType();
            String style;
            switch (type){
                case BOOL:
                    style = "boolean";
                    break;
                case NULL:
                    style = "null";
                    break;
                case NUMBER:
                    style = "number";
                    break;
                case KEY:
                    style = "key";
                    break;
                case STR_VALUE:
                    style = "string-value";
                    break;
                case STRUCTURE:
                    String literal = token.getLiteral();
                    if("{".equals(literal) || "}".equals(literal)){ style = "brace"; }
                    else if ("[".equals(literal) || "]".equals(literal)){ style = "bracket"; }
                    else { style = "splitter"; }
                    break;
                default:
                    style = "unknown";
                    break;
            }
            ArrayList<String> styles = new ArrayList<>();
            if(token.isError() || token.getType() == JsonToken.Type.UNKNOWN){
                styles.add("underlined");
            }
            preStyles = cursorStyles;
            cursorStyles = styles;
            styles.add(style);
            spansBuilder.add(styles, token.getLiteral().length());
        }
        if(preStyles != null){
            JsonToken t = tokens.get(tokens.size() - 1);
            if(t.getType() == JsonToken.Type.BLANK && t.isError()){
                preStyles.add("underlined");
            }
        }
        return spansBuilder.create();
    }

    public MainController() {
        try(
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("test.json");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
        ){
            StringBuilder sb = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null){
                sb.append(line);
            }
            testJson = sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void onPrettyClick() throws JsonParseException {
        String text = codeArea.getText();
        JsonElement jsonElement = JsonOperator.parse(text);
        codeArea.clear();
        codeArea.appendText(jsonElement.toPrettyString());
    }
}