package com.project.mosis.buymeadrink.DataLayer.DataObject;



import android.os.Parcel;
import android.os.Parcelable;

public class ObjectLocation implements Parcelable{

    private double lat,lng;
    private String _id;

    public ObjectLocation(String object_id,double lat, double lng){
        this.lat = lat;
        this.lng = lng;
        this._id = object_id;
    }

    public String getObjectId(){
        return this._id;
    }
    public double getLat(){
        return this.lat;
    }
    public double getLng(){
        return this.lng;
    }

    protected ObjectLocation(Parcel in) {
        _id = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
    }

    public static final Creator<ObjectLocation> CREATOR = new Creator<ObjectLocation>() {
        @Override
        public ObjectLocation createFromParcel(Parcel in) {
            return new ObjectLocation(in);
        }

        @Override
        public ObjectLocation[] newArray(int size) {
            return new ObjectLocation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_id);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
    }
}
