package com.jonathan.statement;

import android.support.annotation.NonNull;

public class RecordItem {
    public int imageId;
    public String description;
    public String amount;
    public String date;

    public RecordItem(@NonNull String purpose, String description, String amount, String date) {
        this.description = description;
        this.amount = amount;
        this.date = date;
        switch(purpose) {
            case "Entertainment":
                imageId = R.drawable.entertainmenticon;
                break;
            case "Meal":
                imageId = R.drawable.mealicon;
                break;
            case "Taobao":
                imageId = R.drawable.taobaoicon;
                break;
            case "Transportation":
                imageId = R.drawable.transportationicon;
                break;
            default:
                imageId = R.drawable.othericon;
                break;
        }
    }

    public String getDescription() {
        return description;
    }

    public int getImageId() {
        return imageId;
    }
}
