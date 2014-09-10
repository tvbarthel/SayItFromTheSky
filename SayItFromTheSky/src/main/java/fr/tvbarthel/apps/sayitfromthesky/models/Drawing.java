package fr.tvbarthel.apps.sayitfromthesky.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple data class that represents a drawing.
 */
public final class Drawing implements Parcelable {

    private String mTitle;
    private int mCreationTimeInMillis;
    private List<String> mEncodedPolylines;

    public Drawing(String title, int creationTimeInMillis, List<String> encodedPolylines) {
        mTitle = title;
        mCreationTimeInMillis = creationTimeInMillis;
        mEncodedPolylines = encodedPolylines;
    }

    public Drawing(Parcel in) {
        readFromParcel(in);
    }

    public String getTitle() {
        return mTitle;
    }

    public int getCreationTimeInMillis() {
        return mCreationTimeInMillis;
    }

    public List<String> getEncodedPolylines() {
        return mEncodedPolylines;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeInt(mCreationTimeInMillis);
        dest.writeStringList(mEncodedPolylines);
    }

    public void readFromParcel(Parcel in) {
        mTitle = in.readString();
        mCreationTimeInMillis = in.readInt();
        mEncodedPolylines = new ArrayList<String>();
        in.readStringList(mEncodedPolylines);
    }

    public static final Parcelable.Creator<Drawing> CREATOR
            = new Parcelable.Creator<Drawing>() {
        public Drawing createFromParcel(Parcel in) {
            return new Drawing(in);
        }

        public Drawing[] newArray(int size) {
            return new Drawing[size];
        }
    };
}
