package com.reforce.shell;

/**
 * Created by zoulong on 2019/2/25 0025.
 */
public class DexFile {
    private String dexName;
    private int dexLength;

    public DexFile() {
    }

    public DexFile(String dexName, int dexLength) {
        this.dexName = dexName;
        this.dexLength = dexLength;
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
}
