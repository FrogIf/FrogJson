package sch.frog.frogjson.javafx;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.Shape;
import com.sun.javafx.scene.text.GlyphList;
import com.sun.javafx.scene.text.TextLayout;
import com.sun.javafx.scene.text.TextLine;
import com.sun.javafx.scene.text.TextSpan;
import javafx.scene.shape.PathElement;

public class CustomTextLayout implements TextLayout {

    @Override
    public boolean setContent(TextSpan[] textSpans) {
        return false;
    }

    @Override
    public boolean setContent(String s, Object o) {
        return false;
    }

    @Override
    public boolean setAlignment(int i) {
        return false;
    }

    @Override
    public boolean setWrapWidth(float v) {
        return false;
    }

    @Override
    public boolean setLineSpacing(float v) {
        return false;
    }

    @Override
    public boolean setDirection(int i) {
        return false;
    }

    @Override
    public boolean setBoundsType(int i) {
        return false;
    }

    @Override
    public BaseBounds getBounds() {
        return null;
    }

    @Override
    public BaseBounds getBounds(TextSpan textSpan, BaseBounds baseBounds) {
        return null;
    }

    @Override
    public BaseBounds getVisualBounds(int i) {
        return null;
    }

    @Override
    public TextLine[] getLines() {
        return new TextLine[0];
    }

    @Override
    public GlyphList[] getRuns() {
        return new GlyphList[0];
    }

    @Override
    public Shape getShape(int i, TextSpan textSpan) {
        return null;
    }

    @Override
    public Hit getHitInfo(float v, float v1) {
        return null;
    }

    @Override
    public PathElement[] getCaretShape(int i, boolean b, float v, float v1) {
        return new PathElement[0];
    }

    @Override
    public PathElement[] getRange(int i, int i1, int i2, float v, float v1) {
        return new PathElement[0];
    }
}
