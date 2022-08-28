package sch.frog.frogjson.controls;

import javafx.application.Platform;
import javafx.concurrent.Task;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import sch.frog.frogjson.GlobalApplicationLifecycleUtil;
import sch.frog.frogjson.json.JsonLexicalAnalyzer;
import sch.frog.frogjson.json.JsonToken;
import sch.frog.frogjson.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JsonAssist {

    private final CodeHighLight codeHighLight = new CodeHighLight();

    private final BracketHighLight bracketHighLight = new BracketHighLight();

    private static JsonAssist instance = null;

    public static JsonAssist getInstance(){
        if(instance == null){
            instance = new JsonAssist();
            GlobalApplicationLifecycleUtil.addOnCloseListener(() -> {
                instance.codeHighLight.stop();
            });
        }
        return instance;
    }

    public void enableAssist(final CustomCodeArea codeArea) {
        codeArea.textProperty().addListener((observableValue, s, t1) -> {
            codeArea.getContext().put(AssistObject.class, null);
        });

        codeHighLight.enable(codeArea);
        bracketHighLight.enable(codeArea);
    }


    private class CodeHighLight {

        private final ExecutorService executor = Executors.newSingleThreadExecutor();

        public void enable(final CustomCodeArea codeArea) {
            // ---- 高亮 ----
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

        private Task<StyleSpans<Collection<String>>> computeHighlightingAsync(CustomCodeArea codeArea) {
            Task<StyleSpans<Collection<String>>> task = new Task<>() {
                @Override
                protected StyleSpans<Collection<String>> call() {
                    return computeHighlighting(codeArea);
                }
            };
            executor.execute(task);
            return task;
        }

        private void applyHighlighting(StyleSpans<Collection<String>> highlighting, CustomCodeArea codeArea) {
            codeArea.setStyleSpans(0, highlighting);
            bracketHighLight.beginHighLight(codeArea);
        }

        private StyleSpans<Collection<String>> computeHighlighting(CustomCodeArea codeArea) {
            StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
            List<JsonToken> tokens = getAssistObject(codeArea).getTokens();
            if(tokens == null || tokens.isEmpty()){
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

        public void stop() {
            executor.shutdown();
        }
    }

    private class BracketHighLight {

        private final String MATCH_STYLE = "match-pair";

        private static final String BRACKET_PAIRS = "{}[]";

        private void match(CodeArea codeArea, Pair pair){
            styleBracket(codeArea, pair.start, MATCH_STYLE);
            styleBracket(codeArea, pair.end, MATCH_STYLE);
        }

        private void clearMatch(CodeArea codeArea, Pair pair){
            removeStyle(codeArea, pair.start, MATCH_STYLE);
            removeStyle(codeArea, pair.end, MATCH_STYLE);
        }

        private void removeStyle(CodeArea codeArea, int pos, String style){
            if (pos < codeArea.getLength()) {
                String text = codeArea.getText(pos, pos + 1);
                if (BRACKET_PAIRS.contains(text)) {
                    StyleSpans<Collection<String>> styleSpans = codeArea.getStyleSpans(pos, pos + 1);
                    HashSet<String> newStyles = new HashSet<>();
                    if(styleSpans != null && styleSpans.length() > 0){
                        for (StyleSpan<Collection<String>> styleSpan : styleSpans) {
                            for (String s : styleSpan.getStyle()) {
                                if(!style.equals(s)){
                                    newStyles.add(s);
                                }
                            }
                        }
                    }
                    codeArea.setStyle(pos, pos + 1, newStyles);
                }
            }
        }

        private void styleBracket(CodeArea codeArea, int pos, String style) {
            if (pos < codeArea.getLength()) {
                String text = codeArea.getText(pos, pos + 1);
                if (BRACKET_PAIRS.contains(text)) {
                    StyleSpans<Collection<String>> styleSpans = codeArea.getStyleSpans(pos, pos + 1);
                    HashSet<String> newStyles = new HashSet<>();
                    if(styleSpans != null && styleSpans.length() > 0){
                        for (StyleSpan<Collection<String>> styleSpan : styleSpans) {
                            newStyles.addAll(styleSpan.getStyle());
                        }
                    }
                    newStyles.add(style);
                    codeArea.setStyle(pos, pos + 1, newStyles);
                }
            }
        }

        private void highlightBracket(CustomCodeArea codeArea, int newVal) {
            AssistObject assistObject = getAssistObject(codeArea);
            if(assistObject.bracketHighlight){
                this.clearBracket(codeArea);

                String prevChar = (newVal > 0 && newVal <= codeArea.getLength()) ? codeArea.getText(newVal - 1, newVal) : "";
                if (BRACKET_PAIRS.contains(prevChar)) newVal--;

                int other = getMatchingBracket(codeArea, newVal);

                if (other < 0) { return; }
                Pair pair = new Pair(newVal, other);
                match(codeArea, pair);
                assistObject.matchPairs.add(pair);
            }
        }

        private int getMatchingBracket(CustomCodeArea codeArea, int index) {
            if (index < 0 || index >= codeArea.getLength()) return -1;

            AssistObject assistObject = getAssistObject(codeArea);
            List<JsonLexicalAnalyzer.BracketPair> bracketPairs = assistObject.getBracketPairs();
            for (JsonLexicalAnalyzer.BracketPair bracketPair : bracketPairs) {
                if(bracketPair.getStart().getStart() == index){
                    return bracketPair.getEnd().getStart();
                }else if(bracketPair.getEnd().getStart() == index){
                    return bracketPair.getStart().getStart();
                }
            }
            return -1;
        }

        public void clearBracket(CustomCodeArea codeArea) {
            AssistObject assistObject = getAssistObject(codeArea);
            Iterator<Pair> iterator = assistObject.matchPairs.iterator();
            while (iterator.hasNext()) {
                Pair pair = iterator.next();
                clearMatch(codeArea, pair);
                iterator.remove();
            }
        }

        public void enable(CustomCodeArea codeArea) {
            codeArea.caretPositionProperty().addListener((obs, oldVal, newVal) -> Platform.runLater(() -> highlightBracket(codeArea, newVal)));
        }

        public void beginHighLight(CustomCodeArea codeArea) {
            AssistObject assistObject = getAssistObject(codeArea);
            assistObject.bracketHighlight = true;
            highlightBracket(codeArea, codeArea.getCaretPosition());
        }
    }

    public AssistObject getAssistObject(CustomCodeArea codeArea){
        CustomCodeArea.CustomCodeAreaContext context = codeArea.getContext();
        AssistObject assistObject = context.getVariable(AssistObject.class);
        if(assistObject == null){
            assistObject = new AssistObject(codeArea);
            context.put(AssistObject.class, assistObject);
        }
        return assistObject;
    }

    private static class Pair{
        final int start;
        final int end;

        public Pair(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    private static class AssistObject{
        private final CustomCodeArea codeArea;

        public AssistObject(CustomCodeArea codeArea){
            this.codeArea = codeArea;
        }

        private boolean bracketHighlight = false;

        private ArrayList<Pair> matchPairs = new ArrayList<>();
        private List<JsonToken> tokens;
        private List<JsonLexicalAnalyzer.BracketPair> bracketPairs;

        public List<JsonToken> getTokens(){
            if(tokens == null){
                String text = codeArea.getText();
                if(StringUtils.isNotBlank(text)){
                    tokens = JsonLexicalAnalyzer.lexicalAnalysis(text, false);
                }
            }
            return tokens;
        }

        public List<JsonLexicalAnalyzer.BracketPair> getBracketPairs(){
            if(bracketPairs == null && tokens != null){
                bracketPairs = JsonLexicalAnalyzer.getBracketPair(tokens);
            }
            return bracketPairs;
        }
    }
}
