package sch.frog.frogjson.controls;

import javafx.concurrent.Task;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import sch.frog.frogjson.GlobalApplicationLifecycleUtil;
import sch.frog.frogjson.json.JsonToken;
import sch.frog.frogjson.json.JsonUtil;
import sch.frog.frogjson.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JsonHighlight {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static JsonHighlight instance = null;

    public static JsonHighlight getInstance(){
        if(instance == null){
            instance = new JsonHighlight();
            GlobalApplicationLifecycleUtil.addOnCloseListener(() -> {
                instance.stop();
            });
        }
        return instance;
    }

    public void enableHighLight(final CodeArea codeArea) {
        // add line numbers to the left of area
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
                    applyHighlighting(highlighting, codeArea);
                });
    }

    private Task<StyleSpans<Collection<String>>> computeHighlightingAsync(CodeArea codeArea) {
        String text = codeArea.getText();
        Task<StyleSpans<Collection<String>>> task = new Task<>() {
            @Override
            protected StyleSpans<Collection<String>> call() {
                return computeHighlighting(text);
            }
        };
        executor.execute(task);
        return task;
    }

    private void applyHighlighting(StyleSpans<Collection<String>> highlighting, CodeArea codeArea) {
        codeArea.setStyleSpans(0, highlighting);
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        if(StringUtils.isBlank(text)){
            spansBuilder.add(Collections.emptyList(), 0);
            return spansBuilder.create();
        }
        List<JsonToken> tokens = JsonUtil.tokenization(text);
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
        if(tokens.isEmpty()){
            spansBuilder.add(Collections.emptyList(), text.length());
        }else if(preStyles != null){
            JsonToken t = tokens.get(tokens.size() - 1);
            if(t.getType() == JsonToken.Type.BLANK && t.isError()){
                preStyles.add("underlined");
            }
        }
        return spansBuilder.create();
    }

    public void stop() {
        executor.shutdown();
    }
}
