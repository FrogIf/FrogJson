# FrogJson

## Overview

json tool. like this:

![image](docs/img/example.png)

* Pretty: 格式化json;
* Compact: 压缩json, 以紧凑的格式输出;
* FromString: 将string数据(前后有双引号, 内部所有双引号都被转义)转为json, 类似于: javascript中的JSON.parse("xxxx");
* ToString: 将json转为字符串, 类似于: javascript中的JSON.stringify(xxx);
* Tree: 解析输出json树;
* 输入框: 用来给新建的tab指定名称.

环境要求: jdk11

## 开发计划


## 手动打包

```
mvn clean package
```