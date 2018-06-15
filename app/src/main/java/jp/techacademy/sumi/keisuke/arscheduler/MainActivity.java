package jp.techacademy.sumi.keisuke.arscheduler;

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
import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.ArrayList;
import io.realm.Realm;

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

    private ListView mListView;
    private TaskAdapter mTaskAdapter;
    Handler mHandler;
    AlarmReceiver alm;
    private static final String TAG = MainActivity.class.getSimpleName();

    ListView listView;
    Context cons;

    private Camera mCam = null;
    private CameraPreview mCamPreview = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // Realmの設定
        Realm.init(this);


        //floatボタンのメニューの登録
        findViewById(R.id.fab).setOnClickListener(addlistClickListener);
        //floatボタンのメニューの登録
        findViewById(R.id.fab2).setOnClickListener(checklistClickListener);




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
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(alm);
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
                Intent intent = new Intent();
                intent.setAction(ACTION_SET_CLANENDER);
                intent.putExtra("id",id);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
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
                // 10分後にタスクの表示を終了
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // TODO: ここで処理を実行する
                        list.remove(list.indexOf(tmstr));
                        adapter.notifyDataSetChanged();
                    }
                }, 600000);
            }
        }
    }
}


