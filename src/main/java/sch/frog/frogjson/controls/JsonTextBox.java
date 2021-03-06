package sch.frog.frogjson.controls;

import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import sch.frog.frogjson.MessageEmitter;

import java.util.ArrayList;

public class JsonTextBox extends BorderPane {

    private final CodeArea codeArea = new CodeArea();

    private final SearchBox textSearchBox;

    private final SearchAction searchAction;

    public JsonTextBox(MessageEmitter messageEmitter) {
        initCodeArea();
        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(codeArea);
        super.setCenter(scrollPane);
        this.searchAction = new SearchAction(codeArea, messageEmitter);
        textSearchBox = new SearchBox(this, (text, searchOverviewFetcher) -> {
            messageEmitter.clear();
            this.searchAction.search(text, true, searchOverviewFetcher);
        }, (text, searchOverviewFetcher) -> {
            messageEmitter.clear();
            this.searchAction.search(text, false, searchOverviewFetcher);
        });
        this.setOnKeyPressed(keyEvent -> {
            if(keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.F){
                this.setTop(textSearchBox);
                textSearchBox.focusSearch(codeArea.getSelectedText());
            }
        });
        textSearchBox.onClose(this.codeArea::requestFocus);
    }

    private void initCodeArea(){
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.prefHeightProperty().bind(this.heightProperty());
        codeArea.prefWidthProperty().bind(this.widthProperty());
        codeArea.textProperty().addListener((observableValue, s, t1) -> searchAction.reset());
    }

    public String getContent() {
        return codeArea.getText();
    }

    public void setContent(String json) {
        codeArea.clear();
        codeArea.appendText(json);
    }

    private static class SearchAction {

        private final CodeArea codeArea;

        private final MessageEmitter messageEmitter;

        public SearchAction(CodeArea codeArea, MessageEmitter messageEmitter) {
            this.codeArea = codeArea;
            this.messageEmitter = messageEmitter;
        }

        private ArrayList<SearchResult> searchResults = null;

        private String searchText;

        private boolean reachBottom;

        private boolean reachTop;

        private int lastCaretPos = -1;

        public void search(String searchText, boolean backward/*true -- ????????????, false - ????????????*/, SearchBox.SearchOverviewFetcher overviewFetcher){
            if(!searchText.equals(this.searchText)){ // ???????????????
                this.searchText = searchText;
                this.searchResults = null;
                this.reachBottom = this.reachTop = false;
                buildSearchResult();
            }

            int cursorIndex = -1;
            if(this.searchResults.isEmpty()){
                messageEmitter.emitWarn("no result found for : " + searchText);
            }else{
                int startIndex = backward ? searchResults.size() - 1 : 0;
                int start = codeArea.getCaretPosition();
                if(lastCaretPos != start){
                    lastCaretPos = start;
                    this.reachBottom = this.reachTop = false;
                }
                for(int i = 0, len = this.searchResults.size(); i < len; i++){
                    SearchResult result = searchResults.get(i);
                    int edge = backward ? result.start : (result.end + 1);
                    if(start < edge){
                        if(backward){
                            startIndex = i - 1;
                        }else{
                            startIndex = i;
                        }
                        break;
                    }
                }

                if(backward){
                    int nextIndex = startIndex + 1;
                    if(nextIndex == searchResults.size()){
                        if(!this.reachBottom){
                            cursorIndex = nextIndex - 1;
                            this.messageEmitter.emitWarn("search reach bottom");
                        }else{
                            cursorIndex = 0;
                        }
                        this.reachBottom = !this.reachBottom;
                    }else{
                        cursorIndex = nextIndex;
                    }
                }else{
                    int nextIndex = startIndex - 1;
                    if(nextIndex == -1){
                        if(!this.reachTop){
                            cursorIndex = 0;
                            this.messageEmitter.emitWarn("search reach top");
                        }else{
                            cursorIndex = searchResults.size() - 1;
                        }
                        this.reachTop = !this.reachTop;
                    }else{
                        cursorIndex = nextIndex;
                    }
                }

                if(cursorIndex >= 0){
                    focusFindResult(searchResults.get(cursorIndex));
                    overviewFetcher.setSearchOverview(new SearchBox.SearchOverview(searchResults.size() - 1, cursorIndex));
                }

            }
        }

        private void focusFindResult(SearchResult result){
            codeArea.selectRange(result.start, result.end);
            codeArea.requestFollowCaret();
        }

        private void buildSearchResult(){
            if(searchText == null || searchText.isEmpty()){
                searchResults = new ArrayList<>(0);
                return;
            }
            int len = searchText.length();
            String content = this.codeArea.getText();
            int index = content.indexOf(searchText);
            searchResults = new ArrayList<>();
            while(index >= 0){
                searchResults.add(new SearchResult(index, index + len));
                index = content.indexOf(searchText, index + len);
            }
        }

        public void reset(){
            this.searchText = null;
            this.searchResults = null;
            this.reachBottom = this.reachTop = false;
        }

    }

    private static class SearchResult {
        private final int start;
        private final int end;

        public SearchResult(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
