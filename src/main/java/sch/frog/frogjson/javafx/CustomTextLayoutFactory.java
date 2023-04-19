package sch.frog.frogjson.javafx;

import com.sun.javafx.scene.text.TextLayout;
import com.sun.javafx.scene.text.TextLayoutFactory;

public class CustomTextLayoutFactory implements TextLayoutFactory {

    private static final CustomTextLayout reusableTL = new CustomTextLayout();
    private static boolean inUse;
    private static final CustomTextLayoutFactory factory = new CustomTextLayoutFactory();

    private CustomTextLayoutFactory() {
    }

    @Override
    public TextLayout createLayout() {
        return new CustomTextLayout();
    }

    @Override
    public TextLayout getLayout() {
        if (inUse) {
            return new CustomTextLayout();
        } else {
            synchronized(CustomTextLayoutFactory.class) {
                if (inUse) {
                    return new CustomTextLayout();
                } else {
                    inUse = true;
                    reusableTL.setAlignment(0);
                    reusableTL.setWrapWidth(0.0F);
                    reusableTL.setDirection(0);
                    reusableTL.setContent(null);
                    return reusableTL;
                }
            }
        }
    }

    @Override
    public void disposeLayout(TextLayout textLayout) {
        if (textLayout == reusableTL) {
            inUse = false;
        }
    }

    public static CustomTextLayoutFactory getFactory() {
        return factory;
    }
}
