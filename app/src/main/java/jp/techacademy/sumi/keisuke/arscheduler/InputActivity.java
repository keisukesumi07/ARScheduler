package jp.techacademy.sumi.keisuke.arscheduler;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.realm.Realm;
import io.realm.RealmResults;

public class InputActivity extends AppCompatActivity {

    public final static String EXTRA_TASK = "jp.techacademy.sumi.keisuke.taskapp.TASK";

    private int mYear, mMonth, mDay, mHour, mMinute,emYear, emMonth, emDay,emHour, emMinute;
    private Button mDateButton, mTimeButton,emTimeButton;
    private EditText mTitleEdit, mContentEdit,mCategoryEdit,mPlaceEdit;
    private Task mTask;
    int sHour,sMinute;

    private View.OnClickListener mOnDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(InputActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            mYear = year;
                            mMonth = monthOfYear;
                            mDay = dayOfMonth;
                            String dateString = mYear + "/" + String.format("%02d",(mMonth + 1)) + "/" + String.format("%02d", mDay);
                            mDateButton.setText(dateString);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
    };

    private View.OnClickListener mOnTimeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(InputActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            mHour = hourOfDay;
                            mMinute = minute;
                            sHour=hourOfDay;
                            sMinute=minute;
                            String timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute);
                            mTimeButton.setText(timeString);
                        }
                    }, mHour, mMinute, true);
            timePickerDialog.show();
        }
    };
    private View.OnClickListener emOnTimeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(InputActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            emHour = hourOfDay;
                            emMinute = minute;
                            String timeString = String.format("%02d", emHour) + ":" + String.format("%02d", emMinute);
                            emTimeButton.setText(timeString);
                        }
                    }, sHour, sMinute, true);
            timePickerDialog.show();
        }
    };

    private View.OnClickListener mOnDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addTask();
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);


        // UI部品の設定
        mDateButton = (Button)findViewById(R.id.date_button);
        mDateButton.setOnClickListener(mOnDateClickListener);
        mTimeButton = (Button)findViewById(R.id.times_button);
        mTimeButton.setOnClickListener(mOnTimeClickListener);
        emTimeButton = (Button)findViewById(R.id.e_times_button);
        emTimeButton.setOnClickListener(emOnTimeClickListener);
        findViewById(R.id.done_button).setOnClickListener(mOnDoneClickListener);
        mTitleEdit = (EditText)findViewById(R.id.title_edit_text);
        mContentEdit = (EditText)findViewById(R.id.content_edit_text);
        mCategoryEdit=(EditText)findViewById(R.id.category_edit_text);
        mPlaceEdit=(EditText)findViewById(R.id.place_edit_text);

        // EXTRA_TASK から Task の id を取得して、 id から Task のインスタンスを取得する
        Intent intent = getIntent();
        int taskId = intent.getIntExtra(MainActivity.EXTRA_TASK, -1);
        Realm realm = Realm.getDefaultInstance();
        mTask = realm.where(Task.class).equalTo("id", taskId).findFirst();
        realm.close();

        if (mTask == null) {
            // 新規作成の場合
            Calendar calendar = Calendar.getInstance();
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);

            Calendar ecalendar = Calendar.getInstance();
            emYear = ecalendar.get(Calendar.YEAR);
            emMonth = ecalendar.get(Calendar.MONTH);
            emDay = ecalendar.get(Calendar.DAY_OF_MONTH);
            emHour = ecalendar.get(Calendar.HOUR_OF_DAY);
            emMinute = ecalendar.get(Calendar.MINUTE);
        } else {
            // 更新の場合
            if(mTask.getTitle()!=null){
                mTitleEdit.setText(mTask.getTitle());
            }

            if(mTask.getContents()!=null){
                mContentEdit.setText(mTask.getContents());
            }

            if(mTask.getCategory()!=null){
                mCategoryEdit.setText(mTask.getCategory());
            }

            if(mTask.getPlace()!=null){
                mPlaceEdit.setText(mTask.getPlace());
            }


            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mTask.getDate());
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);

            if(mTask.geteDate()!=null){
                Calendar ecalendar = Calendar.getInstance();
                ecalendar.setTime(mTask.geteDate());
                emYear = ecalendar.get(Calendar.YEAR);
                emMonth = ecalendar.get(Calendar.MONTH);
                emDay = ecalendar.get(Calendar.DAY_OF_MONTH);
                emHour = ecalendar.get(Calendar.HOUR_OF_DAY);
                emMinute = ecalendar.get(Calendar.MINUTE);
                String etimeString = String.format("%02d", emHour) + ":" + String.format("%02d", emMinute);
                emTimeButton.setText(etimeString);
            }


            String dateString = mYear + "/" + String.format("%02d",(mMonth + 1)) + "/" + String.format("%02d", mDay);
            String timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute);

            mDateButton.setText(dateString);
            mTimeButton.setText(timeString);

        }

        EditText edit1 = (EditText) findViewById(R.id.title_edit_text);
        EditText edit2 = (EditText) findViewById(R.id.content_edit_text);
        EditText edit3 = (EditText) findViewById(R.id.category_edit_text);
        EditText edit4 = (EditText) findViewById(R.id.place_edit_text);

        edit1.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {

                    //ソフトキーボードを閉じる
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        edit2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode == KeyEvent.KEYCODE_ENTER) {

                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    return true;
                }
                return false;
            }
        });

        edit3.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode == KeyEvent.KEYCODE_ENTER) {

                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    return true;
                }
                return false;
            }
        });


        edit4.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode == KeyEvent.KEYCODE_ENTER) {

                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    return true;
                }
                return false;
            }
        });

    }




    private void addTask() {
        Realm realm = Realm.getDefaultInstance();

        realm.beginTransaction();

        if (mTask == null) {
            // 新規作成の場合
            mTask = new Task();

            RealmResults<Task> taskRealmResults = realm.where(Task.class).findAll();

            int identifier;
            if (taskRealmResults.max("id") != null) {
                identifier = taskRealmResults.max("id").intValue() + 1;
            } else {
                identifier = 0;
            }
            mTask.setId(identifier);
        }

        String title = mTitleEdit.getText().toString();
        String content = mContentEdit.getText().toString();
        String category = mCategoryEdit.getText().toString();
        String place = mPlaceEdit.getText().toString();

        mTask.setTitle(title);
        mTask.setContents(content);
        mTask.setCategory(category);
        mTask.setPlace(place);
        GregorianCalendar calendar = new GregorianCalendar(mYear,mMonth,mDay,mHour,mMinute);
        Date date = calendar.getTime();
        GregorianCalendar ecalendar = new GregorianCalendar(emYear,emMonth,emDay,emHour,emMinute);
        Date edate = ecalendar.getTime();
        mTask.setDate(date);
        mTask.seteDate(edate);



        String str=String.format("%02d", mHour)+":"+ String.format("%02d", mMinute);
        mTask.setDatestr(str);

        String estr=String.format("%02d", emHour)+":"+ String.format("%02d", emMinute);
        mTask.seteDatestr(estr);

        realm.copyToRealmOrUpdate(mTask);
        realm.commitTransaction();
        realm.close();


        Intent intent = new Intent();
        intent.putExtra("id",mTask.getId());
        intent.putExtra("calender",calendar.getTimeInMillis());
        setResult(RESULT_OK, intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            // アラートダイアログ
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
            return true;
        }
        return false;
    }

}

