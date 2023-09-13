package com.xxhoz.secbox.util;

/**
 * <BQUrlFormat>
 * <BQUrlFormat>
 *
 * @author DengNanYu
 * @version 1.0_2023/9/13
 * @date 2023/9/13 17:30
 */

import androidx.annotation.WorkerThread;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xxhoz.secbox.bean.TxEpisodeEntity;
import com.xxhoz.secbox.network.HttpUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 标准移动端URL为PC端URL
 */
@WorkerThread
public class StandardPlatformLink {


    /**
     * B站获取播放列表或单个列表
     *
     * @param url 目标URL
     * @param one 是否只获取目标URL一个
     */
    public static JsonElement biUrlFormat(String url, boolean one) throws Exception {
        String code = "";
        String regex_epOrss = "(ep|ss)\\d*";
        Pattern pattern1 = Pattern.compile(regex_epOrss);
        Matcher matcher = pattern1.matcher(url);
        matcher.find();
        code = matcher.group(0);
        code = code.trim();

        // 统一为PC页面格式
        String standardURL = "https://www.bilibili.com/bangumi/play/" + code;

        String html = HttpUtil.INSTANCE.get(standardURL);

        // ep获取指定
        String pattern = "\"epMap\":(.+),\"viewAngle";
        Pattern r = Pattern.compile(pattern);
        // 现在创建 matcher 对象
        Matcher m = r.matcher(html);

        JsonArray jsonElements = new JsonArray();
        if (m.find()) {
            String group = m.group(1);
            // epMap 对象
            JsonObject jsonObject = new Gson().fromJson(group, JsonObject.class);
            // 如果确定ep号可直接获取
            if (one && code.startsWith("ep")){
                JsonObject asJsonObject = jsonObject.getAsJsonObject(code.substring(2));
                return asJsonObject;
            }
            // 过滤正片
            for (String key : jsonObject.keySet()) {
                JsonObject object = jsonObject.get(key).getAsJsonObject();
                if ("0".equals(object.get("sectionType").getAsString())){
                    jsonElements.add(object);
                }
            }
        }

        if (one){
            // 如果为ss 获取第一个
            JsonObject asJsonObject = jsonElements.get(0).getAsJsonObject();
            return asJsonObject;
        }
        // 获取全部合集
        return jsonElements;
    }


    /**
     * 腾讯视频 获取播放列表或单个列表
     * @param url 目标URL
     * @return
     * @throws IOException
     */
    private static HashMap<String, String> cacheFormat = new HashMap<>();
    public static String txUrlFormat(String url) throws Exception {

        // 如果为移动端格式则转换为PC
        if (url.contains("m.v.qq.com")){
            Map<String, String> parameters = parseUrl(url);
            String cid = parameters.get("cid");
            String vid = parameters.get("vid");

            if (!(vid == null || vid.length() == 0)){
                url =  "https://v.qq.com/x/cover/"+cid+"/"+vid+".html";
            }else {
                url = "http://v.qq.com/x/cover/"+cid+".html";
            }
        }

        String lastUrl = cacheFormat.get(url);
        if (lastUrl != null){
            return lastUrl;
        }

        // https://v.qq.com/x/cover/mzc00200kwy29cd/f0043di9m10.html
        //http://v.qq.com/x/page/f0043di9m10.html
        if(url.matches("http.+cover/.+?/.+?\\.html.*") || url.matches("http.+page/.+?\\.html.*")){
            lastUrl = url;
        }else if(url.matches("http.+cover/.+?\\.html.*")){
            // 如果只有cid
            // 获取第一个vid
            TxEpisodeEntity txEpisodeEntity = getTxEpisodeMain(url).get(0);
            String vid = txEpisodeEntity.getVid();


            String rgex = "cover/(.+?).html.*";
            Pattern compile = Pattern.compile(rgex);
            Matcher matcher = compile.matcher(url);
            matcher.find();
            String cid = matcher.group(1);
            lastUrl =  "https://v.qq.com/x/cover/"+cid+"/"+vid+".html";
        }


        cacheFormat.put(url,lastUrl);
        return lastUrl;
    }


    /**
     * 获取腾讯视频 episodeMain 对象
     */
    public static List<TxEpisodeEntity> getTxEpisodeMain(String url) throws Exception{
        String html = HttpUtil.INSTANCE.get(url);

        String regex = "\"episodeMain\":(.+?),\"episodeCut\"";

        Pattern compile = Pattern.compile(regex);
        Matcher matcher = compile.matcher(html);
        matcher.find();
        String episodeMainText = matcher.group(1);


        JsonObject episodeMainObj = new Gson().fromJson(episodeMainText, JsonObject.class);

        JsonArray list = episodeMainObj.getAsJsonArray("listData").get(0).getAsJsonObject().getAsJsonArray("list").get(0).getAsJsonArray();


        ArrayList<TxEpisodeEntity> txEpisodeEntities = new ArrayList<>();
        for (JsonElement jsonElement : list) {
            JsonObject jsonObject = (JsonObject) jsonElement;
            TxEpisodeEntity txEpisodeEntity = new Gson().fromJson(jsonObject, TxEpisodeEntity.class);
            txEpisodeEntities.add(txEpisodeEntity);
        }
        return txEpisodeEntities;
    }

    /**
     * 解析url
     *
     * @param url
     * @return
     */
    public static Map<String, String> parseUrl(String url) {
        HashMap<String,String> map = new HashMap<>();
        if (url == null) {
            return map;
        }
        url = url.trim();
        if ("".equals(url)) {
            return map;
        }
        String[] urlParts = url.split("\\?");
        //没有参数
        if (urlParts.length == 1) {
            return map;
        }
        //有参数
        String[] params = urlParts[1].split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 1){
                map.put(keyValue[0], null);
            }else {
                map.put(keyValue[0], keyValue[1]);
            }

        }
        return map;
    }

}
