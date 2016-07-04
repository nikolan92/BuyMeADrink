package com.project.mosis.buymeadrink;

import android.app.ProgressDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;
import com.project.mosis.buymeadrink.DataLayer.DataObject.Question;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.VolleyCallBack;
import com.project.mosis.buymeadrink.DataLayer.QuestionHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class AnswerTheQuestionActivity extends AppCompatActivity {

    private final String LOG_TAG = "AnswerTheQuestionAct";
    private final String REQUEST_TAG = "AnswerTheQuestionAct";
    
    private QuestionHandler questionHandler;
    //Layout
    private CoordinatorLayout coordinatorLayout;
    private TextView inputAnswerA,inputAnswerB,inputAnswerC,inputAnswerD,inputQuestion,inputCategory;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_the_question);
        Toolbar toolbar = (Toolbar) findViewById(R.id.answer_the_question_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        coordinatorLayout = (CoordinatorLayout)findViewById(R.id.answer_the_question_coordinator_layout);

        Bundle bundle = getIntent().getExtras();

        if(bundle!=null) {
            setupInput();
            String questionID = bundle.getString("questionID");
            questionHandler = new QuestionHandler(this);
            questionHandler.getQuestion(questionID,REQUEST_TAG,new GetQuestion(this));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        questionHandler.cancelAllRequestWithTag(REQUEST_TAG);
    }
    private void setupInput(){
        inputAnswerA = (TextView) findViewById(R.id.answer_the_question_answer_a);
    }
    private void onQuestionReady(JSONObject result) {
        //TODO:Remove dialog
        Question question;
        try{
            if(result.getBoolean("Success")){
                question = new Gson().fromJson(result.getString("Data"),Question.class);
            }else{
                Snackbar.make(coordinatorLayout,"Something goes wrong try again later.",Snackbar.LENGTH_LONG).show();
                return;
            }
        }catch (JSONException exception){
            Log.e(LOG_TAG,exception.toString());
            return;
        }

    }
    private static class GetQuestion implements VolleyCallBack {
        private final WeakReference<AnswerTheQuestionActivity> mActivity;
        GetQuestion(AnswerTheQuestionActivity answerTheQuestionActivity){
            mActivity = new WeakReference<>(answerTheQuestionActivity);
        }
        @Override
        public void onSuccess(JSONObject result) {
            AnswerTheQuestionActivity answerTheQuestionActivity = mActivity.get();
            if(answerTheQuestionActivity!=null)//If activity still exist then do some job, if not just return;
                answerTheQuestionActivity.onQuestionReady(result);
        }

        @Override
        public void onFailed(String error) {
            AnswerTheQuestionActivity answerTheQuestionActivity = mActivity.get();
            if(answerTheQuestionActivity!=null)//If activity still exist then do some job, if not just return;
                Log.e(answerTheQuestionActivity.LOG_TAG,error);
        }
    }

}
