package com.xxhoz.secbox.parserCore.bean

import java.io.Serializable

data class VideoDetailBean(
    val vod_id: String,
    val vod_name: String,
    val vod_pic: String,
    val vod_year: String,
    val vod_remarks: String,
    val vod_actor: String,
    val vod_director: String,
    val vod_content: String,
    val vod_play_from: String,
    val vod_play_url: String
) : Serializable{

    private var datas:  List<ChannelEpisodes>? = null
    fun getChannelFlagsAndEpisodes(): List<ChannelEpisodes>{
        if (datas != null){
            return datas as List<ChannelEpisodes>
        }
        val playInfoList = ArrayList<ChannelEpisodes>()

        val channels = vod_play_from.split("\$\$\$")
        val episodes = vod_play_url.split("\$\$\$")
        for (i in 0..channels.size - 1) {
            val episodesLinkChannel = ArrayList<Value>()
            val EpisodeItems = episodes[i].split("#")
            for (i1 in 0..EpisodeItems.size -1) {
                val split = EpisodeItems[i1].split("\$")
                episodesLinkChannel.add(Value(split[0],split[1]))
            }
            playInfoList.add(ChannelEpisodes(channels[i],episodesLinkChannel))
        }
        datas = playInfoList
        return datas as ArrayList<ChannelEpisodes>
    }

    data class ChannelEpisodes(
        val channelFlag: String,
        val episodes: List<Value>
    )


    data class Value(
        val name: String,
        val urlCode: String
    )
}
/*
* {
            "vod_id": "102684",
            "vod_name": "没用的谎言",
            "vod_pic": "http:\/\/img.facaishiyi.com\/pic\/video\/v\/20230731\/1690787623410.jpg",
            "vod_year": "2023",
            "vod_remarks": "更新至12集",
            "vod_actor": "金所泫,黄旼炫",
            "vod_director": "南成宇",
            "vod_content": "讲述一个能听出谎言的女人和一个拒绝说谎的男人的故事。",
            "vod_play_from": "线路16$$$线路24",
            "vod_play_url": "1$MXwxMDI2ODR8MTZ8aHR0cHM6Ly92aXAubHotY2RuMTQuY29tLzIwMjMwNzMxLzI3NTkwXODR8MTZ8aHR0cHmZ6eXJlYWQxLmNvbS8yMDIzMDgyMS8xNzE4MV9jZWE3NTc2OC9pbmRleC5tM3U4"
        }
*/
