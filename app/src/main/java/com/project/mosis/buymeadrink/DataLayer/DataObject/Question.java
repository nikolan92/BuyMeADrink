package com.project.mosis.buymeadrink.DataLayer.DataObject;

import java.util.ArrayList;

public class Question {
    private String _id;
    private String question;
    private String ownerID;
    private String category;
    private int trueAnswer;
    private ArrayList<String> answers;
    private double lat,lng;
    private String prize;
    private String code;

    public Question(String _id,String ownerID,String category,double lat,double lng,
                    String question,int trueAnswer,ArrayList<String>answers,String prize,String code){
        this._id = _id;
        this.ownerID = ownerID;
        this.question = question;
        this.category = category;
        this.lat = lat;
        this.lng = lng;
        this.trueAnswer = trueAnswer;
        this.answers =answers;
        this.prize = prize;
        this.code = code;
    }
    public Question(String ownerID,String category,double lat,double lng,String question,int trueAnswer,ArrayList<String>answers,String prize,String code){
        this.ownerID = ownerID;
        this.question = question;
        this.category = category;
        this.lat = lat;
        this.lng = lng;
        this.trueAnswer = trueAnswer;
        this.answers =answers;
        this.prize = prize;
        this.code = code;
    }

    public String getID(){
        return this._id;
    }
    public String getOwnerID(){
        return ownerID;
    }
    public String getQuestion(){
        return this.question;
    }
    public int getTrueAnswer(){
        return trueAnswer;
    }
    public String getCategory(){
        return this.category;
    }
    public ArrayList<String> getAnswers(){
        return this.answers;
    }
    public double getLat(){
        return this.lat;
    }
    public double getLng(){
        return this.lng;
    }
}
