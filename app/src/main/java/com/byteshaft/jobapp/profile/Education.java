package com.byteshaft.jobapp.profile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.byteshaft.jobapp.R;
import com.byteshaft.jobapp.gettersetters.Qualification;
import com.byteshaft.jobapp.utils.AppGlobals;
import com.byteshaft.jobapp.utils.Helpers;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;

public class Education extends AppCompatActivity implements View.OnClickListener {


    private TextView buttonSave;
    private Toolbar toolbarTop;
    private ImageButton backButton;
    private ListView mListView;
    private Button addEducationButton;
    private ArrayList<Qualification> qualificationArrayList;
    private QualificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_education);
        toolbarTop = (Toolbar) findViewById(R.id.add_education_toolbar);
        buttonSave = (TextView) findViewById(R.id.button_save_edu);
        backButton = (ImageButton) findViewById(R.id.back_button);
        mListView = (ListView) findViewById(R.id.education_list);
        addEducationButton = (Button) findViewById(R.id.button_add_education);
        setSupportActionBar(toolbarTop);
        qualificationArrayList = new ArrayList<>();

        backButton.setOnClickListener(this);
        buttonSave.setOnClickListener(this);
        addEducationButton.setOnClickListener(this);
        getQualificationList();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_button:
                onBackPressed();
                break;
            case R.id.button_save_edu:
                System.out.println("save");
                break;
            case R.id.button_add_education:
                Qualification qualification = new Qualification();
                qualification.setQualification("Add Qualification");
                qualification.setSchool("XYZ School");
                qualification.setPeriod("from - till");
                addEducation(qualification);
                break;
        }
    }

    private void addEducation(final Qualification qualification) {
        Helpers.showProgressDialog(Education.this, "Adding...");
        HttpRequest request = new HttpRequest(this);
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        Helpers.dismissProgressDialog();
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_CREATED:
                                qualificationArrayList.add(qualification);
                                adapter.notifyDataSetChanged();

                        }
                }

            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, int readyState, short error, Exception exception) {

            }
        });
        request.open("POST", String.format("%seducation/ ", AppGlobals.BASE_URL));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send(getEducationData(qualification.getPeriod(),
                qualification.getQualification(), qualification.getSchool()));
    }

    private String getEducationData(String period, String qualification, String school) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("period", period);
            jsonObject.put("qualification", qualification);
            jsonObject.put("school", school);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();

    }

    private void getQualificationList() {
        Helpers.showProgressDialog(Education.this, "Please wait...");
        HttpRequest requestQualifications = new HttpRequest(getApplicationContext());
        requestQualifications.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        Helpers.dismissProgressDialog();
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                try {
                                    JSONArray jsonArray = new JSONArray(request.getResponseText());
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        Qualification qualification = new Qualification();
                                        qualification.setId(jsonObject.getInt("id"));
                                        qualification.setUserId(jsonObject.getInt("user"));
                                        qualification.setQualification(jsonObject.getString("qualification"));
                                        qualification.setPeriod(jsonObject.getString("period"));
                                        qualification.setSchool(jsonObject.getString("school"));
                                        qualificationArrayList.add(qualification);
                                    }

                                    adapter = new QualificationAdapter(qualificationArrayList);
                                    mListView.setAdapter(adapter);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                        }
                }
            }
        });
        requestQualifications.open("GET", String.format("%seducation/", AppGlobals.BASE_URL));
        requestQualifications.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        requestQualifications.send();
    }


    private class QualificationAdapter extends BaseAdapter {

        private ViewHolder viewHolder;
        private ArrayList<Qualification> qualificationsList;

        public QualificationAdapter(ArrayList<Qualification> qualificationsList) {
            this.qualificationsList = qualificationsList;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.delegate_education, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.educationNumber = (TextView) convertView.findViewById(R.id.tv_education_number);
                viewHolder.period = (EditText) convertView.findViewById(R.id.et_time_span);
                viewHolder.qualification = (EditText) convertView.findViewById(R.id.et_qualification);
                viewHolder.school = (EditText) convertView.findViewById(R.id.et_school);
                viewHolder.removeButton = (TextView) convertView.findViewById(R.id.remove_education);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Qualification qualification = qualificationsList.get(position);
            viewHolder.period.setText(qualification.getPeriod());
            viewHolder.qualification.setText(qualification.getQualification());
            viewHolder.school.setText(qualification.getSchool());
            viewHolder.educationNumber.setText("Education # " + (position + 1));
            viewHolder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("Edu Remove button click");
                }
            });
            return convertView;
        }

        @Override
        public int getCount() {
            return qualificationsList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }


        private class ViewHolder {
            private TextView educationNumber;
            private TextView removeButton;
            private EditText period;
            private EditText qualification;
            private EditText school;
        }

    }
}
