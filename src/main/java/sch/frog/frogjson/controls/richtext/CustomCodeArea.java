package sch.frog.frogjson.controls.richtext;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.ReadOnlyStyledDocument;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.UnaryOperator;

public class CustomCodeArea extends CodeArea {

    private final CustomCodeAreaContext context = new CustomCodeAreaContext();

    public CustomCodeAreaContext getContext(){
        return context;
    }

    public static class CustomCodeAreaContext {
        private final HashMap<Class, Object> variable = new HashMap<>();

        @SuppressWarnings("unchecked")
        public synchronized <T> T getVariable(Class<T> clazz){
            return (T) variable.get(clazz);
        }
        public synchronized <T> void put(Class<T> clazz, T value){
            variable.put(clazz, value);
        }
    }

    @Override
    protected void fold( int startPos, int endPos, UnaryOperator<Collection<String>> styleMixin )
    {
        ReadOnlyStyledDocument<Collection<String>, String, Collection<String>> subDoc;
        UnaryOperator<Paragraph<Collection<String>, String, Collection<String>>> mapper;

        subDoc = (ReadOnlyStyledDocument<Collection<String>, String, Collection<String>>) subDocument( startPos, endPos );
        mapper = p -> p.setParagraphStyle( styleMixin.apply( p.getParagraphStyle() ) );

        for ( int p = 1; p < subDoc.getParagraphCount(); p++ ) {
            subDoc = subDoc.replaceParagraph( p, mapper ).get1();
        }

        replace( startPos, endPos, subDoc );
        recreateParagraphGraphic( offsetToPosition( startPos, Bias.Backward ).getMajor() );
        moveTo( startPos );
        foldCheck = true;
    }

}
