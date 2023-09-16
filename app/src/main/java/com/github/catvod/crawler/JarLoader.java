package com.github.catvod.crawler;

import com.umeng.commonsdk.internal.crash.UMCrashManager;
import com.xxhoz.secbox.App;
import com.xxhoz.secbox.bean.exception.GlobalException;
import com.xxhoz.secbox.util.LogUtils;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dalvik.system.DexClassLoader;

public class JarLoader {
    private DexClassLoader classLoader = null;
    private ConcurrentHashMap<String, Spider> spiders = new ConcurrentHashMap<>();
    private Method proxyFun = null;


    // public  void setClassLoad(DexClassLoader classLoader){
    //     this.classLoader = classLoader;
    // }
    /**
     * 不要在主线程调用我
     *
     * @param filePath
     */
    public boolean load(String filePath) {
        spiders.clear();
        proxyFun = null;
        boolean success = true;
        try {
            // dex释放缓存位置
            File cacheDir = new File(App.instance.getCacheDir().getAbsolutePath() + "/catvod_csp");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            // 判断文件是否存在
            if (!new File(filePath).exists()) {
                throw GlobalException.Companion.of("Jar包不存在...");
            }
            classLoader = new DexClassLoader(filePath, cacheDir.getAbsolutePath(), null, App.instance.getClassLoader());
            // make force wait here, some device async dex load
            // int count = 0;
            // do {
            //     try {
            //         Class classInit = classLoader.loadClass("com.github.catvod.spider.Init");
            //         if (classInit != null) {
            //             Method method = classInit.getMethod("init", Context.class);
            //             method.invoke(null, App.instance);
            //             System.out.println("自定义爬虫代码加载成功!");
            //             success = true;
            //             try {
            //                 Class proxy = classLoader.loadClass("com.github.catvod.spider.Proxy");
            //                 Method mth = proxy.getMethod("proxy", Map.class);
            //                 proxyFun = mth;
            //             } catch (Throwable th) {
            //
            //             }
            //             break;
            //         }
            //         Thread.sleep(200);
            //     } catch (Throwable th) {
            //         th.printStackTrace();
            //     }
            //     count++;
            // } while (count < 5);
        } catch (Throwable th) {
            UMCrashManager.reportCrash(App.instance, GlobalException.Companion.of("加载Jar包失败:" + th.getMessage()));
            success = false;
            th.printStackTrace();
        }
        return success;
    }

    /**
     * @param key key
     * @param cls api
     * @param ext ext
     * @return
     */
    public Spider getSpider(String key, String cls, String ext) {
        String clsKey = cls.replace("csp_", "");
        if (spiders.containsKey(key)) {
            return spiders.get(key);
        }
        if (classLoader == null) {
            return new SpiderNull();
        }
        try {
            Spider sp = (Spider) classLoader.loadClass("com.github.catvod.spider." + clsKey).newInstance();
            sp.init(App.instance, ext);
            spiders.put(key, sp);
            return sp;
        } catch (Throwable th) {
            LogUtils.INSTANCE.e("获取Spider["+key+"]错误:" + th.getMessage());
            th.printStackTrace();
        }
        return new SpiderNull();
    }

    public JSONObject jsonExt(String key, LinkedHashMap<String, String> jxs, String url) {
        try {
            String clsKey = "Json" + key;
            String hotClass = "com.github.catvod.parser." + clsKey;
            Class jsonParserCls = classLoader.loadClass(hotClass);
            Method mth = jsonParserCls.getMethod("parse", LinkedHashMap.class, String.class);
            return (JSONObject) mth.invoke(null, jxs, url);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return null;
    }

    public JSONObject jsonExtMix(String flag, String key, String name, LinkedHashMap<String, HashMap<String, String>> jxs, String url) {
        try {
            String clsKey = "Mix" + key;
            String hotClass = "com.github.catvod.parser." + clsKey;
            Class jsonParserCls = classLoader.loadClass(hotClass);
            Method mth = jsonParserCls.getMethod("parse", LinkedHashMap.class, String.class, String.class, String.class);
            return (JSONObject) mth.invoke(null, jxs, name, flag, url);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return null;
    }

    public Object[] proxyInvoke(Map params) {
        try {
            if (proxyFun != null) {
                return (Object[]) proxyFun.invoke(null, params);
            }
        } catch (Throwable th) {

        }
        return null;
    }
}
