package com.github.catvod.crawler;



import com.github.catvod.util.js.SpiderJS;
import com.xxhoz.secbox.App;

import java.util.concurrent.ConcurrentHashMap;

public class JsLoader {
    private ConcurrentHashMap<String, Spider> spiders = new ConcurrentHashMap<>();

    public Spider getSpider(String key, String cls, String ext, String jar) {
        if (spiders.containsKey(key)) {
            return spiders.get(key);
        }
        try {
            SpiderJS sp = new SpiderJS(key, cls, ext);
            // sp.init(App.instance, ext);
            spiders.put(key, sp);
            return sp;
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return new SpiderNull();
    }
}
