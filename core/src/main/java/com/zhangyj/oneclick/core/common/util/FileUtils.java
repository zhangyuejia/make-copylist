package com.zhangyj.oneclick.core.common.util;

import java.io.File;
import java.net.URL;

/**
 * @author zhangyj
 */
public class FileUtils {

    public static final String CLASSPATH = "classpath:";

    public static String getResourcePath(String resource) {
        if(new File(resource).exists()){
            return resource;
        }
        if(resource.startsWith(CLASSPATH)){
            resource = resource.substring(CLASSPATH.length());
        }
        URL url = FileUtils.class.getClassLoader().getResource("");
        if(url == null){
            throw new IllegalArgumentException("路径不存在");
        }
        return url.getPath().substring(1) + resource;
    }

    public static String getResourcePath() {
        return getResourcePath("");
    }

    public static String getTempDir(String dirName){
        return System.getProperty("java.io.tmpdir") + File.separator +  dirName;
    }
}
