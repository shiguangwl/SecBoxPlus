package com.xxhoz.secbox.parserCore.bean

/**
 * 分类信息
 */
data class CategoryBean(
    val filters: HashMap<String,List<Filter>>,
    val `class`: List<ClassType>,
    val list: List<ListItem>
) {


    data class Filter(
        val key: String,
        val name: String,
        val value: List<Value>
    )

    data class Value(
        val n: String,
        val v: String
    )

    data class ClassType(
        val type_id: String,
        val type_name: String
    )

    data class ListItem(
        val vod_id: String,
        val vod_name: String,
        val vod_pic: String,
        val vod_remarks: String
    )
}
