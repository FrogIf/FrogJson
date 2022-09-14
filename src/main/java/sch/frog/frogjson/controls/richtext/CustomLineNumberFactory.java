package sch.frog.frogjson.controls.richtext;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.TwoDimensional;

import java.util.Collection;
import java.util.function.IntFunction;

public class CustomLineNumberFactory implements IntFunction<Node> {

    private static final Font DEFAULT_FOLD_FONT = Font.font("monospace", FontWeight.BOLD, 13);

    private final LineNumberFactory<?> lineNumberFactory;

    public static IntFunction<Node> get(CodeArea area, CollapsibleExecutor collapsibleExecutor) {
        area.caretPositionProperty().addListener((obs, oldVal, newVal) -> Platform.runLater(() -> {
            int lineNo;
            if(oldVal < area.getLength()){
                lineNo = area.offsetToPosition(oldVal, TwoDimensional.Bias.Backward).getMajor();
                area.recreateParagraphGraphic(lineNo);
            }
            lineNo = area.offsetToPosition(newVal, TwoDimensional.Bias.Backward).getMajor();
            area.recreateParagraphGraphic(lineNo);
        }));
        return new CustomLineNumberFactory(area, collapsibleExecutor);
    }

    private final CodeArea area;

    private final CollapsibleExecutor collapsibleExecutor;

    private CustomLineNumberFactory(CodeArea area, CollapsibleExecutor collapsibleExecutor) {
        lineNumberFactory = (LineNumberFactory<Collection<String>>) LineNumberFactory.get(area);
        this.area = area;
        this.collapsibleExecutor = collapsibleExecutor;
    }

    @Override
    public Node apply(int idx) {
        Label lineNo = (Label) lineNumberFactory.apply(idx);
        int caretPosition = area.getCaretPosition();
        if(caretPosition >= 0){
            int line = area.offsetToPosition(area.getCaretPosition(), TwoDimensional.Bias.Backward).getMajor();
            if(line == idx){
                if(collapsibleExecutor.check(idx, this.area)){
                    Node graphic = lineNo.getGraphic();
                    Label foldIndicator = ((Label) graphic);
                    String text = foldIndicator.getText();
                    if(!text.contains("+") && !text.contains("-")){
                        foldIndicator = new Label("-");
                        lineNo.setGraphic(foldIndicator);
                        foldIndicator.getStyleClass().add("fold-indicator");
                        foldIndicator.setOnMouseClicked(ME -> collapsibleExecutor.fold(idx, area));
                        foldIndicator.setCursor(Cursor.HAND);
                        foldIndicator.setTextFill(Color.BLUE);
                        foldIndicator.setFont( DEFAULT_FOLD_FONT );
                    }
                }
            }
        }
        return lineNo;
    }

    public interface CollapsibleExecutor {
        boolean check(int lineIndex, CodeArea codeArea);

        void fold(int lineIndex, CodeArea codeArea);
    }

}
