package com.example.gabriel.myapplication;

/**
 * Created by cWX435411 on 2018/1/26.
 */

public class ResultInfo {
    private String wavName;
    private String lable;
    private String rec;
    private String correct;
    private String wavTimeLeng;
    private String recTime;
    public String getRecTime() {
        return recTime;
    }
    public void setRecTime(String recTime) {
        this.recTime = recTime;
    }

    public String getWavName() {
        return wavName;
    }

    public void setWavName(String wavName) {
        this.wavName = wavName;
    }

    public String getLable() {
        return lable;
    }

    public void setLable(String lable) {
        this.lable = lable;
    }

    public String getRec() {
        return rec;
    }

    public void setRec(String rec) {
        this.rec = rec;
    }

    public String getCorrect() {
        return correct;
    }

    public void setCorrect(String correct) {
        this.correct = correct;
    }

    public String getWavTimeLeng() {
        return wavTimeLeng;
    }

    public void setWavTimeLeng(String wavTimeLeng) {
        this.wavTimeLeng = wavTimeLeng;
    }

    @Override
    public String toString() {
        return "ResultInfo{" +
                "wavName='" + wavName + '\'' +
                ", lable='" + lable + '\'' +
                ", rec='" + rec + '\'' +
                ", correct='" + correct + '\'' +
                ", waitTime='" + wavTimeLeng + '\'' +
                ", recTime='" + recTime + '\'' +
                '}';
    }
}
