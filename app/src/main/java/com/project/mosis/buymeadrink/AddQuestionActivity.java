package com.project.mosis.buymeadrink;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.project.mosis.buymeadrink.DataLayer.DataObject.Question;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.VolleyCallBack;
import com.project.mosis.buymeadrink.DataLayer.QuestionHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class AddQuestionActivity extends AppCompatActivity {

    private final String LOG_TAG = "AddQuestionActivity";
    private final String REQUEST_TAG = "AddQuestionActivity";

    private CoordinatorLayout coordinatorLayout;
    private Spinner categorySpinner;
    private EditText inputQuestion,inputAnswerA,inputAnswerB,inputAnswerC,inputAnswerD;
    private TextInputLayout inputLayoutQuestion,inputLayoutAnswerA,inputLayoutAnswerB,inputLayoutAnswerC,inputLayoutAnswerD;
    private QuestionHandler questionHandler;
    private String ownerId;
    private double lat,lng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);

        Toolbar toolbar = (Toolbar) findViewById(R.id.add_question_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        coordinatorLayout = (CoordinatorLayout)findViewById(R.id.add_question_coordinator_layout);
        questionHandler = new QuestionHandler(this);

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){

            //get intent extras
            ownerId = bundle.getString("ownerID");
            lat = bundle.getDouble("lat");
            lng = bundle.getDouble("lng");
            //setup layout
            setupCategorySpinner();
            setupInput();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        questionHandler.cancelAllRequestWithTag(REQUEST_TAG);
    }

    /**
     *Layout Setup
     *=================================================================================================
     **/
    private void setupCategorySpinner(){
        categorySpinner = (Spinner) findViewById(R.id.add_question_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.add_question_category_array,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        categorySpinner.setAdapter(adapter);
    }
    private void setupInput(){
        inputLayoutQuestion = (TextInputLayout) findViewById(R.id.add_question_input_layout_question);
        inputQuestion = (EditText) findViewById(R.id.add_question_input_question);
        inputLayoutAnswerA = (TextInputLayout) findViewById(R.id.add_question_input_layout_answer_a);
        inputAnswerA = (EditText) findViewById(R.id.add_question_input_answer_a);
        inputLayoutAnswerB = (TextInputLayout) findViewById(R.id.add_question_input_layout_answer_b);
        inputAnswerB = (EditText) findViewById(R.id.add_question_input_answer_b);
        inputLayoutAnswerC = (TextInputLayout) findViewById(R.id.add_question_input_layout_answer_c);
        inputAnswerC = (EditText) findViewById(R.id.add_question_input_answer_c);
        inputLayoutAnswerD = (TextInputLayout) findViewById(R.id.add_question_input_layout_answer_d);
        inputAnswerD = (EditText) findViewById(R.id.add_question_input_answer_d);

        final Button add_btn = (Button)findViewById(R.id.add_question_add_btn);
        assert add_btn!=null;
        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addQuestion();
            }
        });
    }
    //Action add btn
    private void addQuestion(){
        if(validateInputs())
        {
            //if fields are ok then make request to the server and add question
            //Toast.makeText(this,"Success",Toast.LENGTH_LONG).show();
            //TODO:make progress dialog
            //TODO: collect data
            Question question = collectData();

//            ArrayList<String> ans = new ArrayList<>();
//            ans.add("AA");
//            ans.add("BB");
//            ans.add("CC");
//            ans.add("DD");
            //Question question = new Question("576d2647e39c63c016000029","Math",43.3216882,21.8977572,"Some quesertopnsdnasdna???",1,ans);
            //questionHandler.addQuestion(question,REQUEST_TAG,new AddQuestionListener(this));
        }
    }
    /**
     *Input validation and data collection
     *=================================================================================================
     **/
    private Question collectData(){

        //Toast.makeText(this,categorySpinner.getSelectedItem().toString(),Toast.LENGTH_LONG).show();
        //new Question(ownerId,categorySpinner.getSelectedItem().toString(),)
        return null;
    }
    private boolean validateInputs() {
        if (inputQuestion.getText().toString().trim().isEmpty()) {
            inputLayoutQuestion.setError(getString(R.string.add_question_err_msg_empty_input));
            requestFocus(inputQuestion);
            return false;
        } else {
            inputLayoutQuestion.setError("");
        }
        if (inputAnswerA.getText().toString().trim().isEmpty()) {
            inputLayoutAnswerA.setError(getString(R.string.add_question_err_msg_empty_input));
            requestFocus(inputAnswerA);
            return false;
        } else {
            inputLayoutAnswerA.setError("");
        }
        if (inputAnswerB.getText().toString().trim().isEmpty()) {
            inputLayoutAnswerB.setError(getString(R.string.add_question_err_msg_empty_input));
            requestFocus(inputAnswerB);
            return false;
        } else {
            inputLayoutAnswerB.setError("");
        }
        if (inputAnswerC.getText().toString().trim().isEmpty()) {
            inputLayoutAnswerC.setError(getString(R.string.add_question_err_msg_empty_input));
            requestFocus(inputAnswerC);
            return false;
        } else {
            inputLayoutAnswerC.setError("");
        }
        if (inputAnswerD.getText().toString().trim().isEmpty()) {
            inputLayoutAnswerD.setError(getString(R.string.add_question_err_msg_empty_input));
            requestFocus(inputAnswerD);
            return false;
        } else {
            inputLayoutAnswerD.setError("");
        }

        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }
    /**
     *Response handling
     *=================================================================================================
     **/
    public void onQuestionRequestSuccess(JSONObject result){
        //TODO:remove progress dialog
        try{
            if(result.getBoolean("Success")){

                Log.i(LOG_TAG,result.toString());
                //TODO:Finish activity and set result as ok
            }else{
                Snackbar.make(coordinatorLayout,"Something went wrong,try again later.",Snackbar.LENGTH_LONG).show();
            }
        }catch (JSONException exception){
            Log.e(LOG_TAG,exception.toString());
        }
    }
    public void onQuestionRequestFailed(String error){
        //TODO:remove progress dialog
        Snackbar.make(coordinatorLayout,"Something went wrong,try again later.",Snackbar.LENGTH_LONG).show();
        Log.e(LOG_TAG,error);
    }
    /**
     *My VolleyCallBack class
     *=================================================================================================
     **/
    private static class AddQuestionListener implements VolleyCallBack{
        private final WeakReference<AddQuestionActivity> mActivity;

        private AddQuestionListener(AddQuestionActivity mActivity) {
            this.mActivity = new WeakReference<>(mActivity);
        }

        @Override
        public void onSuccess(JSONObject result) {
            AddQuestionActivity addQuestionActivity = mActivity.get();
            if(addQuestionActivity!=null){
                addQuestionActivity.onQuestionRequestSuccess(result);
            }
        }
        @Override
        public void onFailed(String error) {
            AddQuestionActivity addQuestionActivity = mActivity.get();
            if(addQuestionActivity!=null){
                addQuestionActivity.onQuestionRequestFailed(error);
            }
        }
    }
}
