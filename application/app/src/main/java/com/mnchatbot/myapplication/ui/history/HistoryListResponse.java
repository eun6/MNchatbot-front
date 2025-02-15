package com.mnchatbot.myapplication.ui.history;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HistoryListResponse {
    @SerializedName("statusCode")
    private int code;

    @SerializedName("responseMessage")
    private String message;

    @SerializedName("data")
    public List<HistoryDataList> data;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public class HistoryDataList {
        //검색결과
        @SerializedName("apptSerial")
        private int apptSerial;
        @SerializedName("partnerName")
        private String partnerName;
        @SerializedName("apptDate")
        private String HistoryDate;

        public int getapptSerial() {
            return apptSerial;
        }
        public String getpartnerName() {
            return partnerName;
        }
        public String getHistoryDate() {
            return HistoryDate;
        }
    }

}
