package com.utils;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by zoulong on 2019/2/25 0025.
 */
public class DexFile {
    private String dexName;
    private int dexLength;
    //使用transient 和 serialize = false效果等同
    @JSONField(serialize = false)
    private byte[] data;
    public DexFile() {
    }

    public DexFile(String dexName, byte[] data) {
        this.dexName = dexName;
        this.dexLength = data.length;
        this.data = data;
    }

    public String getDexName() {
        return dexName;
    }

    public void setDexName(String dexName) {
        this.dexName = dexName;
    }

    public int getDexLength() {
        return dexLength;
    }

    public void setDexLength(int dexLength) {
        this.dexLength = dexLength;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
