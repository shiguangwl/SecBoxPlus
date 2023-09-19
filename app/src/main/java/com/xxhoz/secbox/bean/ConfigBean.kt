package com.xxhoz.secbox.bean

data class ConfigBean(
    val version: String,
    val version_min: String,
    val msg: String,
    val downloadUrl: String,
    val notice: String,
    val lru: String,
    val state: String,
    val stateInfo: String,
    val configJsonUrl: String,
    val danmukuApi: String,
    val banner:List<String>,
    val customSite: List<String>
)
