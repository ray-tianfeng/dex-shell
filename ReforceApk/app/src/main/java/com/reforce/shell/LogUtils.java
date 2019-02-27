package com.reforce.shell;

import android.util.Log;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author zl
 * @time 2019/1/7 0007.
 */

public class LogUtils {
    private final static String Tag = "PreviewProject";
    private final static Map<Integer,StringBuffer> bufferLogs = new LinkedHashMap<>();
    public static void d(Object msg){
        Log.d(Tag,msg.toString());
    }

    private static StringBuffer getBufferLog(int tag){
        if(bufferLogs.get(tag) != null) return bufferLogs.get(tag);
        StringBuffer sb = new StringBuffer();
        bufferLogs.put(tag,sb);
        return sb;
    }

    public static void clearBufferD(int tag){
        StringBuffer sb = getBufferLog(tag);
        d(sb.toString());
        bufferLogs.remove(tag);
    }

    public static void clearBufferD(){
        Iterator<Map.Entry<Integer,StringBuffer>> it = bufferLogs.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<Integer, StringBuffer> entry = it.next();
            d("tag:"+entry.getKey()+" value:"+entry.getValue());
        }
    }

    public static void bufferD(int tag, Object msg){
        StringBuffer sb = getBufferLog(tag);
        if(sb.length() != 0)
            sb.append(" , "+msg);
        else
            sb.append(msg);
    }
}
