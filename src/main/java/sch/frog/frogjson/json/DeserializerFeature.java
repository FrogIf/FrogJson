package sch.frog.frogjson.json;

/**
 * 反序列化特性
 */
public enum DeserializerFeature {

    /**
     * 对特殊字符进行转义
     */
    Escape,
    /**
     * 词法分析不正确时, 终止
     */
    AbortWhenIncorrect;

}
