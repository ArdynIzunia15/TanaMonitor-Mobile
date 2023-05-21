package com.example.tanamonitor;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.LocalDateTime;

public class History implements Parcelable {
    private String actionType;
    private LocalDateTime dateTime;

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public History(String actionType, LocalDateTime dateTime) {
        this.actionType = actionType;
        this.dateTime = dateTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.actionType);
        dest.writeSerializable(this.dateTime);
    }

    public void readFromParcel(Parcel source) {
        this.actionType = source.readString();
        this.dateTime = (LocalDateTime) source.readSerializable();
    }

    protected History(Parcel in) {
        this.actionType = in.readString();
        this.dateTime = (LocalDateTime) in.readSerializable();
    }

    public static final Creator<History> CREATOR = new Creator<History>() {
        @Override
        public History createFromParcel(Parcel source) {
            return new History(source);
        }

        @Override
        public History[] newArray(int size) {
            return new History[size];
        }
    };
}
