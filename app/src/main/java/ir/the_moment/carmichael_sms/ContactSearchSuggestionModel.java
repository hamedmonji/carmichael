package ir.the_moment.carmichael_sms;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

/**
 * Created by vaas on 7/15/17.
 */

public class ContactSearchSuggestionModel implements SearchSuggestion {
    public String title;
    public long id;
    @Override
    public String getBody() {
        return null;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeLong(this.id);
    }

    public ContactSearchSuggestionModel(String title,long id) {
        this.title = title;
        this.id = id;
    }

    protected ContactSearchSuggestionModel(Parcel in) {
        this.title = in.readString();
        this.id = in.readLong();
    }

    public static final Creator<ContactSearchSuggestionModel> CREATOR = new Creator<ContactSearchSuggestionModel>() {
        @Override
        public ContactSearchSuggestionModel createFromParcel(Parcel source) {
            return new ContactSearchSuggestionModel(source);
        }

        @Override
        public ContactSearchSuggestionModel[] newArray(int size) {
            return new ContactSearchSuggestionModel[size];
        }
    };
}
