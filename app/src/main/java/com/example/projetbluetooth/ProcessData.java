package com.example.projetbluetooth;

public class ProcessData {
    String processName;
    String uid;
    String RSS;

    static String separator = "|";
    static String endSeparator = "||";

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

    public static String StringFormatDataForUpdate(String processName, String RSS)
    {
        return "update" + separator + processName + separator + "uid" + separator + RSS + endSeparator;
    }
}
