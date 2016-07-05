package com.project.mosis.buymeadrink;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
    private String userID;
    private String questionID;
    //Layout
    private int answerNum = -1;
    private CoordinatorLayout coordinatorLayout;
    private TextView inputAnswerA,inputAnswerB,inputAnswerC,inputAnswerD,inputQuestion,inputCategory;
    private RadioButton radioButtonA,radioButtonB,radioButtonC,radioButtonD;
    private Question question;
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
            userID = bundle.getString("userID");
            questionID = bundle.getString("questionID");
            questionHandler = new QuestionHandler(this);
            progressDialog = ProgressDialog.show(this,"Please wait","Waiting for data from the server...",false,false);
            questionHandler.getQuestion(questionID,REQUEST_TAG,new GetQuestion(this));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        questionHandler.cancelAllRequestWithTag(REQUEST_TAG);
    }
    private void setupInput(){
        inputQuestion = (TextView) findViewById(R.id.answer_the_question_question);
        inputCategory = (TextView) findViewById(R.id.answer_the_question_category);
        inputAnswerA = (TextView) findViewById(R.id.answer_the_question_answer_a);
        inputAnswerB = (TextView) findViewById(R.id.answer_the_question_answer_b);
        inputAnswerC = (TextView) findViewById(R.id.answer_the_question_answer_c);
        inputAnswerD = (TextView) findViewById(R.id.answer_the_question_answer_d);

        radioButtonA = (RadioButton)findViewById(R.id.answer_the_question_radio_a);
        radioButtonB = (RadioButton)findViewById(R.id.answer_the_question_radio_b);
        radioButtonC = (RadioButton)findViewById(R.id.answer_the_question_radio_c);
        radioButtonD = (RadioButton)findViewById(R.id.answer_the_question_radio_d);

        radioButtonA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioButtonB.setChecked(false);
                radioButtonC.setChecked(false);
                radioButtonD.setChecked(false);
                answerNum = 0;
            }
        });
        radioButtonB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioButtonA.setChecked(false);
                radioButtonC.setChecked(false);
                radioButtonD.setChecked(false);
                answerNum = 1;
            }
        });
        radioButtonC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioButtonA.setChecked(false);
                radioButtonB.setChecked(false);
                radioButtonD.setChecked(false);
                answerNum = 2;
            }
        });
        radioButtonD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioButtonA.setChecked(false);
                radioButtonC.setChecked(false);
                radioButtonB.setChecked(false);
                answerNum = 3;
            }
        });
        Button answerBtn = (Button)findViewById(R.id.answer_the_question_answer_btn);

        assert answerBtn!=null;
        answerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(answerNum==-1)
                    Snackbar.make(coordinatorLayout,"You need to check some answer.",Snackbar.LENGTH_LONG).show();
                else
                    answerTheQuestion();
            }
        });
    }
    private void answerTheQuestion(){
        questionHandler.answerTheQuestion(questionID,userID,answerNum,REQUEST_TAG,new AnswerTheQuestion(this));
    }
    private void onQuestionAnswer(JSONObject result) {
        progressDialog.dismiss();
        try {
            if(result.getBoolean("Success")){
                String prize = "Your prize is:"+question.getPrize();
                String code = "Your secret code is:"+question.getCode();
                inputCategory.setText(prize);
                inputQuestion.setText(code);
                Snackbar.make(coordinatorLayout,"Congratulations your answer is correct, see your prize in the top of this window.",Snackbar.LENGTH_LONG).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("questionID", question.getID());
                setResult(RESULT_OK,resultIntent);

            }else {
                Snackbar.make(coordinatorLayout,result.getString("Error"),Snackbar.LENGTH_LONG).show();
            }
        }catch (JSONException exception){
            Log.e(LOG_TAG,exception.toString());
        }

    }
    private void onQuestionReady(JSONObject result) {
        progressDialog.dismiss();
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
        inputQuestion.setText(question.getQuestion());
        inputCategory.setText(question.getCategory());
        inputAnswerA.setText(question.getAnswers().get(0));
        inputAnswerB.setText(question.getAnswers().get(1));
        inputAnswerC.setText(question.getAnswers().get(2));
        inputAnswerD.setText(question.getAnswers().get(3));
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
            if (answerTheQuestionActivity != null)//If activity still exist then do some job, if not just return;
            {
                answerTheQuestionActivity.progressDialog.dismiss();
                Snackbar.make(answerTheQuestionActivity.coordinatorLayout,"Something goes wrong try again later.",Snackbar.LENGTH_LONG).show();
                Log.e(answerTheQuestionActivity.LOG_TAG, error);
            }
        }
    }
    private static class AnswerTheQuestion implements VolleyCallBack{
        private final WeakReference<AnswerTheQuestionActivity> mActivity;
        AnswerTheQuestion(AnswerTheQuestionActivity answerTheQuestionActivity){
            mActivity = new WeakReference<>(answerTheQuestionActivity);
        }
        @Override
        public void onSuccess(JSONObject result) {
            AnswerTheQuestionActivity answerTheQuestionActivity = mActivity.get();
            if(answerTheQuestionActivity!=null)//If activity still exist then do some job, if not just return;
                answerTheQuestionActivity.onQuestionAnswer(result);
        }

        @Override
        public void onFailed(String error) {
            AnswerTheQuestionActivity answerTheQuestionActivity = mActivity.get();
            if(answerTheQuestionActivity!=null)//If activity still exist then do some job, if not just return;
            {
                answerTheQuestionActivity.progressDialog.dismiss();
                Snackbar.make(answerTheQuestionActivity.coordinatorLayout,"Something goes wrong try again later.",Snackbar.LENGTH_LONG).show();
                Log.e(answerTheQuestionActivity.LOG_TAG, error);
            }
        }
    }
}
