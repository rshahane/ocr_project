package com.paragyte.ocrproject.classes;


import java.util.ArrayList;

public class ListWithFlags {
    ArrayList<String> list;
    boolean hasTotalWord = false;
    boolean hasDateWord = false;
    boolean hasPossibleMerchantName = false;

    public ArrayList<String> getList() {
        return list;
    }

    public void setList(ArrayList<String> list) {
        this.list = list;
    }

    public boolean isHasTotalWord() {
        return hasTotalWord;
    }

    public void setHasTotalWord(boolean hasTotalWord) {
        this.hasTotalWord = hasTotalWord;
    }

    public boolean isHasDateWord() {
        return hasDateWord;
    }

    public void setHasDateWord(boolean hasDateWord) {
        this.hasDateWord = hasDateWord;
    }

    public boolean isHasPossibleMerchantName() {
        return hasPossibleMerchantName;
    }

    public void setHasPossibleMerchantName(boolean hasPossibleMerchantName) {
        this.hasPossibleMerchantName = hasPossibleMerchantName;
    }
}
