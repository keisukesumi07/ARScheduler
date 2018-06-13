package jp.techacademy.sumi.keisuke.arscheduler;

/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;


import butterknife.BindView;
import butterknife.ButterKnife;
import jp.techacademy.sumi.keisuke.arscheduler.common.helpers.CameraPermissionHelper;



import java.util.ArrayList;
import io.realm.Realm;
import io.realm.RealmChangeListener;


import android.os.SystemClock;

/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore API. The application will display any detected planes and will allow the user to tap on a
 * plane to place a 3d model of the Android robot.
 */
public class MainActivity extends AppCompatActivity{

    @BindView(R.id.list_comment)
    ListView mCommentListView;
    CommentAdapter adapter;
    ArrayList<String> list;
    CoordinatorLayout coord;

    private static final int RC_PASSCHANGE = 1001;



    //Task管理用
    public final static String EXTRA_TASK = "jp.techacademy.sumi.keisuke.taskapp.TASK";
    private static final String ACTION_SET_CLANENDER = "com.android.example.ACTION_SET_CLANENDER";

    private Realm mRealm;
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
            //reloadListView();
        }
    };
    private ListView mListView;
    private TaskAdapter mTaskAdapter;


    Handler mHandler;
    AlarmReceiver alm;




    private static final String TAG = MainActivity.class.getSimpleName();




    ListView listView;
    Context cons;


    // カメラインスタンス
    private Camera mCam = null;
    // カメラプレビュークラス
    private CameraPreview mCamPreview = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this);
            return;
        }

        //floatボタンのメニューの登録
        findViewById(R.id.fab).setOnClickListener(addlistClickListener);
        //floatボタンのメニューの登録
        findViewById(R.id.fab2).setOnClickListener(checklistClickListener);


        // Realmの設定
        Realm.init(this);
        mRealm = Realm.getDefaultInstance();
        mRealm.addChangeListener(mRealmListener);

        //ListViewの設定
        mTaskAdapter = new TaskAdapter(MainActivity.this);



        //該当のIntentに反応するようにReceiverにIntentFilterを登録
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SET_CLANENDER);
        alm = new AlarmReceiver();
        registerReceiver(alm, filter);
        ButterKnife.bind(this);


        //ListView
        listView = (ListView)findViewById(R.id.list_comment);
        coord = findViewById(R.id.coord);
        list = new ArrayList<String>();

        //adapter
        adapter = new CommentAdapter(MainActivity.this,list);
        listView.setAdapter(adapter);



    }




    View.OnClickListener addlistClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, InputActivity.class);
            startActivityForResult(intent, RC_PASSCHANGE);
        }
    };

    View.OnClickListener checklistClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, TaskManagement.class);
            startActivity(intent);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        cons=this;
        listView.setAdapter(adapter);
        // カメラインスタンスの取得
        try {
            mCam = Camera.open();
        } catch (Exception e) {
            // エラー
            this.finish();
        }

        // FrameLayout に CameraPreview クラスを設定
        FrameLayout preview = (FrameLayout)findViewById(R.id.frames);
        mCamPreview = new CameraPreview(this, mCam);
        preview.addView(mCamPreview);

        coord.bringToFront();
        listView.bringToFront();
    }

    @Override
    public void onPause() {
        super.onPause();
        // カメラ破棄インスタンスを解放
        if (mCam != null) {
            mCam.release();
            mCam = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(alm);
        mRealm.close();
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==RC_PASSCHANGE){

            if(resultCode==RESULT_OK){
                Long calender=data.getLongExtra("calender",0);
                int id = data.getIntExtra("id",0);


                Context context = this;
                //AlarmManagerを取得
                AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

                //PendingIntentを作成
                Intent intent = new Intent();
                intent.setAction(ACTION_SET_CLANENDER);
                intent.putExtra("id",id);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                //AlarmManagerにPendingIntentを登録
                am.set(AlarmManager.RTC_WAKEUP, calender, pendingIntent);

            }
        }

    }






    public class AlarmReceiver extends BroadcastReceiver{

        String str;
        //BroadcastReceiverを拡張したclassでAlarmManagerから発行されたIntentを受信した際の処理を記載
        @Override
        public void onReceive(final Context context, Intent intent) {
            if(intent.getAction().equals(ACTION_SET_CLANENDER)){

                Task mTask;
                int id = intent.getIntExtra("id",0);

                Realm realm = Realm.getDefaultInstance();
                mTask = realm.where(Task.class).equalTo("id", id).findFirst();

                str=mTask.getDatestr()+"~"+mTask.geteDatestr()+"  場所:"+mTask.getPlace()+"\n"+mTask.getTitle()+"\n"+mTask.getContents();


                realm.close();

                mHandler = new Handler(Looper.getMainLooper());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        list.add(str);
                        adapter = new CommentAdapter(MainActivity.this,list);
                        listView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                });
                final String tmstr=str;
                // 3秒後に処理を実行する
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // TODO: ここで処理を実行する
                        list.remove(list.indexOf(tmstr));
                        adapter.notifyDataSetChanged();
                    }
                }, 30000);
            }
        }
    }



}


