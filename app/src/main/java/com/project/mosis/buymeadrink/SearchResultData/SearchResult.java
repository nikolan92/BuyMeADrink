package com.project.mosis.buymeadrink.SearchResultData;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;


public class SearchResult implements SearchSuggestion{

    private String searchResult;
    private boolean isHistory = false;

    public SearchResult(String s){
        this.searchResult = s;
    }
    public SearchResult(Parcel source) {
        this.searchResult = source.readString();
        this.isHistory = source.readInt() != 0;
    }

    @Override
    public String getBody() {
        return searchResult;
    }
    public static final Creator<SearchResult> CREATOR = new Creator<SearchResult>() {
        @Override
        public SearchResult createFromParcel(Parcel in) {
            return new SearchResult(in);
        }

        @Override
        public SearchResult[] newArray(int size) {
            return new SearchResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(searchResult);
        dest.writeInt(isHistory ? 1 : 0);
    }
}
