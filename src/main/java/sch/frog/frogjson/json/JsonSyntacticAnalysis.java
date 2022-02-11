package sch.frog.frogjson.json;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;

class JsonSyntacticAnalysis {

    private JsonSyntacticAnalysis() {
        // do nothing
    }

    private static final BuildStatusNode ROOT_NODE;

    static {
        ROOT_NODE = buildStatusMap();
    }

    public static JsonElement syntacticAnalysis(List<Token> tokens) throws JsonParseException {
        if (tokens == null || tokens.size() < 2) {
            throw new JsonParseException("json lack necessary structure.");
        }
        Stack<BuildStatusNode> pendingStack = new Stack<>();
        JsonBuilder jsonBuilder = new JsonBuilder();
        BuildStatusNode cursor = ROOT_NODE;
        for (Token token : tokens) {
            BuildStatusNode pre = cursor;
            cursor = cursor.move(token);
            if (cursor == null && pre.isTerminal() && !pendingStack.isEmpty()) {
                cursor = pendingStack.pop().move(token);
            }
            if (cursor == null) {
                triggerException(token);
            }
            cursor.tokenHandler.handle(token, jsonBuilder);

            if (cursor.isNest()) {    // 如果是嵌套节点
                pendingStack.push(cursor);
                cursor = cursor.moveInNest(token);
                if (cursor == null) {
                    triggerException(token);
                }
                cursor.tokenHandler.handle(token, jsonBuilder);
            }
        }
        if (!jsonBuilder.stack.isEmpty()) {
            throw new JsonParseException("json is not end right");
        }
        return jsonBuilder.root.build();
    }

    private static void triggerException(Token token) throws JsonParseException {
        throw new JsonParseException("json format is not right, row : " + token.row + ", col : " + token.col + ", literal : " + token.literal);
    }

    private static BuildStatusNode buildStatusMap() {
        // --- object ---
        BuildStatusNode objectBegin = new BuildStatusNode(JsonWord.OBJECT_BEGIN, TokenType.structure,
                (token, builder) -> builder.newJsonObject());
        BuildStatusNode objectKey = new BuildStatusNode(BuildStatusNode.WILDCARD, TokenType.t_string,
                (token, builder) -> builder.newObjectKey(token.literal));
        BuildStatusNode objectColon = new BuildStatusNode(JsonWord.COLON, TokenType.structure, (token, builder) -> { /* do nothing */ });
        BuildStatusNode objectComma = new BuildStatusNode(JsonWord.COMMA, TokenType.structure, (token, builder) -> {/* do nothing */});
        BuildStatusNode objectEnd = new BuildStatusNode(JsonWord.OBJECT_END, TokenType.structure,
                (token, builder) -> builder.closeElement());
        BuildStatusNode objectNest = new BuildStatusNode(null, null, (token, builder) -> { /* do nothing */ }, true);

        objectBegin.to(objectKey);
        objectBegin.to(objectEnd);

        objectKey.to(objectColon);

        objectColon.to(objectNest);

        objectNest.to(objectComma);
        objectNest.to(objectEnd);

        objectComma.to(objectKey);

        // --- array ---
        BuildStatusNode arrayBegin = new BuildStatusNode(JsonWord.ARRAY_BEGIN, TokenType.structure,
                (token, builder) -> builder.newJsonArray());
        BuildStatusNode arrayEnd = new BuildStatusNode(JsonWord.ARRAY_END, TokenType.structure,
                (token, builder) -> builder.closeElement());
        BuildStatusNode arrayComma = new BuildStatusNode(JsonWord.COMMA, TokenType.structure, (token, builder) -> {/* do nothing */ });
        BuildStatusNode arrayNest = new BuildStatusNode(null, null, (token, builder) -> { /* do nothing */ }, true);

        arrayBegin.to(arrayEnd);
        arrayBegin.to(arrayNest);
        arrayNest.to(arrayComma);
        arrayNest.to(arrayEnd);
        arrayComma.to(arrayNest);

        // --- value ---
        BuildStatusNode stringValue = new BuildStatusNode(BuildStatusNode.WILDCARD, TokenType.t_string,
                (token, builder) -> builder.addArrayValue(new StringJsonValue(token.literal)));
        BuildStatusNode constValue = new BuildStatusNode(BuildStatusNode.WILDCARD, TokenType.t_const,
                (token, builder) -> builder.addArrayValue(new ConstJsonValue(token.literal)));
        BuildStatusNode numberValue = new BuildStatusNode(BuildStatusNode.WILDCARD, TokenType.number,
                (token, builder) -> builder.addArrayValue(new NumberJsonValue(token.literal)));
        arrayNest.nestInclude(arrayBegin);
        arrayNest.nestInclude(objectBegin);
        arrayNest.nestInclude(stringValue);
        arrayNest.nestInclude(constValue);
        arrayNest.nestInclude(numberValue);

        objectNest.nestInclude(arrayBegin);
        objectNest.nestInclude(objectBegin);
        objectNest.nestInclude(stringValue);
        objectNest.nestInclude(constValue);
        objectNest.nestInclude(numberValue);

        BuildStatusNode rootNode = new BuildStatusNode(null, null, (token, builder) -> {/* do nothing */ });
        rootNode.to(objectBegin);
        rootNode.to(arrayBegin);
        return rootNode;
    }

    private static class JsonBuilder {

        private final Stack<JsonElementBuilder> stack = new Stack<>();

        private JsonElementBuilder root;

        public void newJsonObject() {
            stack.push(new JsonObjectBuilder());
            createCallback();
        }

        public void newJsonArray() {
            stack.push(new JsonArrayBuilder());
            createCallback();
        }

        private void createCallback() {
            if (root == null) {
                root = stack.peek();
            }
        }

        public void newObjectKey(String key) {
            stack.peek().setPendingKey(key);
        }

        public void addArrayValue(JsonValue<?> jsonValue) {
            stack.peek().setJsonValue(jsonValue);
        }

        public void closeElement() {
            JsonElementBuilder builder = stack.pop();
            JsonElementBuilder parent;
            if (stack.isEmpty()) {
                parent = root;
            } else {
                parent = stack.peek();
            }
            if (parent == builder) {
                return;
            }
            parent.setJsonElement(builder.build());
        }
    }

    private interface JsonElementBuilder {
        void setPendingKey(String pendingKey);

        void setJsonValue(JsonValue<?> jsonValue);

        void setJsonElement(JsonElement jsonObject);

        JsonElement build();
    }

    private static class JsonObjectBuilder implements JsonElementBuilder {
        private String pendingKey = null;

        private JsonObject jsonObject = new JsonObject();

        @Override
        public void setPendingKey(String pendingKey) {
            if (this.pendingKey == null) {
                this.pendingKey = pendingKey;
            } else {
                throw new IllegalStateException("repeat assign object key.");
            }
        }

        @Override
        public void setJsonValue(JsonValue<?> jsonValue) {
            if (pendingKey == null) {
                throw new IllegalStateException("key is not assign");
            } else {
                jsonObject.putJsonValue(pendingKey, jsonValue);
            }
            pendingKey = null;
        }

        @Override
        public void setJsonElement(JsonElement jsonElement) {
            if (pendingKey == null) {
                throw new IllegalStateException("key is not assign");
            } else {
                if (jsonElement instanceof JsonObject) {
                    jsonObject.putObject(pendingKey, (JsonObject) jsonElement);
                } else {
                    jsonObject.putArray(pendingKey, (JsonArray) jsonElement);
                }
                this.pendingKey = null;
            }
        }

        @Override
        public JsonElement build() {
            JsonElement result = this.jsonObject;
            this.jsonObject = null;
            return result;
        }
    }

    private static class JsonArrayBuilder implements JsonElementBuilder {

        private JsonArray jsonArray = new JsonArray();

        @Override
        public void setPendingKey(String pendingKey) {
            throw new UnsupportedOperationException("setPendingKey");
        }

        @Override
        public void setJsonValue(JsonValue<?> jsonValue) {
            jsonArray.addJsonValue(jsonValue);
        }

        @Override
        public void setJsonElement(JsonElement jsonElement) {
            if (jsonElement instanceof JsonObject) {
                jsonArray.addObject((JsonObject) jsonElement);
            } else {
                jsonArray.addArray((JsonArray) jsonElement);
            }
        }

        @Override
        public JsonElement build() {
            JsonElement result = this.jsonArray;
            this.jsonArray = null;
            return result;
        }
    }

    private interface ITokenHandler {
        void handle(Token token, JsonBuilder builder);
    }

    private static class BuildStatusNode {

        public static final String WILDCARD = "*";

        private final TokenType tokenType;

        private final String literal;

        private final HashMap<TokenType, HashMap<String, BuildStatusNode>> moveMap = new HashMap<>();

        private final HashMap<TokenType, HashMap<String, BuildStatusNode>> moveNestMap = new HashMap<>(0);

        private BuildStatusNode defaultNode = null;

        private final ITokenHandler tokenHandler;

        // 该节点是否为嵌套节点
        private final boolean nest;

        public BuildStatusNode(String literal, TokenType tokenType, ITokenHandler tokenHandler) {
            this.tokenHandler = tokenHandler;
            this.tokenType = tokenType;
            this.literal = literal;
            this.nest = false;
        }

        public BuildStatusNode(String literal, TokenType tokenType, ITokenHandler tokenHandler, boolean nest) {
            this.tokenHandler = tokenHandler;
            this.tokenType = tokenType;
            this.literal = literal;
            this.nest = nest;
        }

        /**
         * 移动至下一个状态节点
         * 如果返回null, 说明移动失败
         */
        BuildStatusNode move(Token token) {
            BuildStatusNode next = moveByMap(token, this.moveMap);
            if (next == null) {
                next = defaultNode;
            }
            return next;
        }

        void to(BuildStatusNode node) {
            if (node.tokenType == null) {
                if(this.defaultNode != null){
                    throw new IllegalStateException("default node has exists.");
                }
                this.defaultNode = node;
            } else {
                addToMap(node, this.moveMap);
            }
        }

        private BuildStatusNode moveByMap(Token token, HashMap<TokenType, HashMap<String, BuildStatusNode>> map) {
            HashMap<String, BuildStatusNode> literalToNode = map.get(token.type);
            BuildStatusNode next = null;
            if (literalToNode != null) {
                next = literalToNode.get(token.literal);
                if (next == null && literalToNode.containsKey(WILDCARD)) {
                    next = literalToNode.get(WILDCARD);
                }
            }
            return next;
        }

        private void addToMap(BuildStatusNode node, HashMap<TokenType, HashMap<String, BuildStatusNode>> map) {
            HashMap<String, BuildStatusNode> literalToNode = map.computeIfAbsent(node.tokenType, k -> new HashMap<>());
            if (literalToNode.containsKey(node.literal)) {
                throw new IllegalArgumentException("assign map has exists, type : " + node.tokenType + ", literal : " + node.literal);
            }
            literalToNode.put(node.literal, node);
        }

        boolean isNest() {
            return nest;
        }

        void nestInclude(BuildStatusNode node) {
            addToMap(node, this.moveNestMap);
        }

        BuildStatusNode moveInNest(Token token) {
            return moveByMap(token, this.moveNestMap);
        }

        boolean isTerminal(){
            return this.defaultNode == null && this.moveMap.isEmpty() && this.moveNestMap.isEmpty();
        }

    }


}
