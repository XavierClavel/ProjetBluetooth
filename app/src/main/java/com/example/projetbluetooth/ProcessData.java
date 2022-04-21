package com.example.projetbluetooth;

public class ProcessData {
    String processName;
    String uid;
    String RSS;

    String separator = "|";
    String endSeparator = "||";

    public ProcessData(String processName, String uid, String RSS) {    //constructor
        this.processName = processName;
        this.uid = uid;
        this.RSS = RSS;
    }

    public String FormatData()
    {
        return separator + processName + separator + uid + separator + RSS + endSeparator;
        //returns data in format |processName|uid|RSS
    }
}
