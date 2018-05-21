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

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.Point.OrientationMode;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.techacademy.sumi.keisuke.arscheduler.common.helpers.CameraPermissionHelper;
import jp.techacademy.sumi.keisuke.arscheduler.common.helpers.DisplayRotationHelper;
import jp.techacademy.sumi.keisuke.arscheduler.common.helpers.FullScreenHelper;
import jp.techacademy.sumi.keisuke.arscheduler.common.helpers.SnackbarHelper;
import jp.techacademy.sumi.keisuke.arscheduler.common.helpers.TapHelper;
import jp.techacademy.sumi.keisuke.arscheduler.common.rendering.BackgroundRenderer;
import jp.techacademy.sumi.keisuke.arscheduler.common.rendering.ObjectRenderer;
import jp.techacademy.sumi.keisuke.arscheduler.common.rendering.ObjectRenderer.BlendMode;
import jp.techacademy.sumi.keisuke.arscheduler.common.rendering.PlaneRenderer;
import jp.techacademy.sumi.keisuke.arscheduler.common.rendering.PointCloudRenderer;
import jp.techacademy.sumi.keisuke.arscheduler.R;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import java.io.IOException;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

import android.os.SystemClock;

/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore API. The application will display any detected planes and will allow the user to tap on a
 * plane to place a 3d model of the Android robot.
 */
public class MainActivity extends AppCompatActivity implements GLSurfaceView.Renderer {

    @BindView(R.id.list_comment)
    ListView mCommentListView;
    CommentAdapter adapter;
    Activity thisactivity;

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

    Global global;

    Handler mHandler;




    private static final String TAG = MainActivity.class.getSimpleName();
    private final int REQUEST_PERMISSION = 1000;

    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    private GLSurfaceView surfaceView;

    private boolean installRequested;

    private Session session;
    private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
    private DisplayRotationHelper displayRotationHelper;
    private TapHelper tapHelper;

    private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
    private final ObjectRenderer virtualObject = new ObjectRenderer();
    private final ObjectRenderer virtualObjectShadow = new ObjectRenderer();
    private final PlaneRenderer planeRenderer = new PlaneRenderer();
    private final PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();

    // Temporary matrix allocated here to reduce number of allocations for each frame.
    private final float[] anchorMatrix = new float[16];

    // Anchors created from taps used for object placing.
    private final ArrayList<Anchor> anchors = new ArrayList<>();


    ArrayList<String> list;
    FrameLayout frameLayout;
    ListView listView;


    boolean test=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView = findViewById(R.id.surfaceview);
        displayRotationHelper = new DisplayRotationHelper(/*context=*/ this);




        // Set up tap listener.
        tapHelper = new TapHelper(/*context=*/ this);
        surfaceView.setOnTouchListener(tapHelper);

        // Set up renderer.
        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);





        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                startActivityForResult(intent,RC_PASSCHANGE);
            }
        });


        // Realmの設定
        Realm.init(this);
        mRealm = Realm.getDefaultInstance();
        mRealm.addChangeListener(mRealmListener);

         //ListViewの設定
        mTaskAdapter = new TaskAdapter(MainActivity.this);
        //mListView = (ListView) findViewById(R.id.listView1);
//
//        // ListViewをタップしたときの処理
//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                // 入力・編集する画面に遷移させる
//                Task task = (Task) parent.getAdapter().getItem(position);
//
//                Intent intent = new Intent(MainActivity.this, InputActivity.class);
//                intent.putExtra(EXTRA_TASK, task.getId());
//
//                startActivity(intent);
//            }
//        });
//
//
//        // ListViewを長押ししたときの処理
//        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//
//                // タスクを削除する
//
//                final Task task = (Task) parent.getAdapter().getItem(position);
//
//                // ダイアログを表示する
//                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//
//                builder.setTitle("削除");
//                builder.setMessage(task.getTitle() + "を削除しますか");
//                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        RealmResults<Task> results = mRealm.where(Task.class).equalTo("id", task.getId()).findAll();
//
//                        mRealm.beginTransaction();
//                        results.deleteAllFromRealm();
//                        mRealm.commitTransaction();
//
//                        Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
//                        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
//                                MainActivity.this,
//                                task.getId(),
//                                resultIntent,
//                                PendingIntent.FLAG_UPDATE_CURRENT
//                        );
//
//                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//                        alarmManager.cancel(resultPendingIntent);
//
//                        reloadListView();
//                    }
//                });
//                builder.setNegativeButton("CANCEL", null);
//
//                AlertDialog dialog = builder.create();
//                dialog.show();
//
//                return true;
//            }
//        });

        //reloadListView();




        //グローバル変数を取得
        global = (Global) this.getApplication();
        //初期化
        global.GlobalsAllInit();
        global.thecontext=this;




        //該当のIntentに反応するようにReceiverにIntentFilterを登録
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SET_CLANENDER);
        registerReceiver(new AlarmReceiver(), filter);


        ButterKnife.bind(this);


        thisactivity=this;





        //ListView
        listView = (ListView)findViewById(R.id.list_comment);
        ArrayList<String> list = new ArrayList<String>();
        list.add("sample0");

        //adapter
        adapter = new CommentAdapter(this,list);
        listView.setAdapter(adapter);



        installRequested = false;
    }



    @Override
    protected void onResume() {
        super.onResume();


        if (session == null) {
            Exception exception = null;
            String message = null;
            try {
                switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    CameraPermissionHelper.requestCameraPermission(this);
                    return;
                }

                // Create the session.
                session = new Session(/* context= */ this);

            } catch (UnavailableArcoreNotInstalledException
                    | UnavailableUserDeclinedInstallationException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (UnavailableDeviceNotCompatibleException e) {
                message = "This device does not support AR";
                exception = e;
            } catch (Exception e) {
                message = "Failed to create AR session";
                exception = e;
            }

            if (message != null) {
                messageSnackbarHelper.showError(this, message);
                Log.e(TAG, "Exception creating session", exception);
                return;
            }
        }

        // Note that order matters - see the note in onPause(), the reverse applies here.
        try {
            session.resume();
        } catch (CameraNotAvailableException e) {
            // In some cases (such as another camera app launching) the camera may be given to
            // a different app instead. Handle this properly by showing a message and recreate the
            // session at the next iteration.
            messageSnackbarHelper.showError(this, "Camera not available. Please restart the app.");
            session = null;
            return;
        }

        surfaceView.onResume();
        displayRotationHelper.onResume();


    }

    @Override
    public void onPause() {
        super.onPause();
        if (session != null) {
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            displayRotationHelper.onPause();
            surfaceView.onPause();
            session.pause();
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
        try {
            // Create the texture and pass it to ARCore session to be filled during update().
            backgroundRenderer.createOnGlThread(/*context=*/ this);
            planeRenderer.createOnGlThread(/*context=*/ this, "models/trigrid.png");
            pointCloudRenderer.createOnGlThread(/*context=*/ this);
            virtualObject.createOnGlThread(/*context=*/ this, "models/andy.obj", "a");
            virtualObject.setMaterialProperties(0.0f, 2.0f, 0.5f, 6.0f);

            virtualObjectShadow.createOnGlThread(this, "models/andy_shadow.obj", "models/andy_shadow.png");
            virtualObjectShadow.setBlendMode(BlendMode.Shadow);
            virtualObjectShadow.setMaterialProperties(1.0f, 0.0f, 0.0f, 1.0f);

        } catch (IOException e) {
            Log.e(TAG, "Failed to read an asset file", e);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (session == null) {
            return;
        }
        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper.updateSessionIfNeeded(session);

        try {
            session.setCameraTextureName(backgroundRenderer.getTextureId());

            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            Frame frame = session.update();
            Camera camera = frame.getCamera();

            // Handle taps. Handling only one tap per frame, as taps are usually low frequency
            // compared to frame rate.

            MotionEvent tap = tapHelper.poll();
            if (tap != null && camera.getTrackingState() == TrackingState.TRACKING) {
                for (HitResult hit : frame.hitTest(tap)) {
                    // Check if any plane was hit, and if it was hit inside the plane polygon
                    Trackable trackable = hit.getTrackable();
                    // Creates an anchor if a plane or an oriented point was hit.
                    if ((trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose()))
                            || (trackable instanceof Point
                            && ((Point) trackable).getOrientationMode()
                            == OrientationMode.ESTIMATED_SURFACE_NORMAL)) {
                        // Hits are sorted by depth. Consider only closest hit on a plane or oriented point.
                        // Cap the number of objects created. This avoids overloading both the
                        // rendering system and ARCore.
                        if (anchors.size() >= 20) {
                            anchors.get(0).detach();
                            anchors.remove(0);
                        }
                        // Adding an Anchor tells ARCore that it should track this position in
                        // space. This anchor is created on the Plane to place the 3D model
                        // in the correct position relative both to the world and to the plane.
                        anchors.add(hit.createAnchor());
                        break;
                    }
                }
            }

            // Draw background.
            backgroundRenderer.draw(frame);

            // If not tracking, don't draw 3d objects.
            if (camera.getTrackingState() == TrackingState.PAUSED) {
                return;
            }

            // Get projection matrix.
            float[] projmtx = new float[16];
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

            // Get camera matrix and draw.
            float[] viewmtx = new float[16];
            camera.getViewMatrix(viewmtx, 0);

            // Compute lighting from average intensity of the image.
            // The first three components are color scaling factors.
            // The last one is the average pixel intensity in gamma space.
            final float[] colorCorrectionRgba = new float[4];
            frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);

            // Visualize tracked points.
            PointCloud pointCloud = frame.acquirePointCloud();
            pointCloudRenderer.update(pointCloud);
            pointCloudRenderer.draw(viewmtx, projmtx);

            // Application is responsible for releasing the point cloud resources after
            // using it.
            pointCloud.release();

            // Check if we detected at least one plane. If so, hide the loading message.
            if (messageSnackbarHelper.isShowing()) {
                for (Plane plane : session.getAllTrackables(Plane.class)) {
                    if (plane.getType() == com.google.ar.core.Plane.Type.HORIZONTAL_UPWARD_FACING
                            && plane.getTrackingState() == TrackingState.TRACKING) {
                        messageSnackbarHelper.hide(this);
                        break;
                    }
                }
            }

            // Visualize planes.
            planeRenderer.drawPlanes(
                    session.getAllTrackables(Plane.class), camera.getDisplayOrientedPose(), projmtx);

            // Visualize anchors created by touch.
            float scaleFactor = 1.0f;
            for (Anchor anchor : anchors) {
                if (anchor.getTrackingState() != TrackingState.TRACKING) {
                    continue;
                }
                // Get the current pose of an Anchor in world space. The Anchor pose is updated
                // during calls to session.update() as ARCore refines its estimate of the world.
                anchor.getPose().toMatrix(anchorMatrix, 0);

                // Update and draw the model and its shadow.
                virtualObject.updateModelMatrix(anchorMatrix, scaleFactor);
                virtualObjectShadow.updateModelMatrix(anchorMatrix, scaleFactor);
                virtualObject.draw(viewmtx, projmtx, colorCorrectionRgba);
                virtualObjectShadow.draw(viewmtx, projmtx, colorCorrectionRgba);
            }

        } catch (Throwable t) {
            // Avoid crashing the application due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread", t);
        }


    }




//    private void reloadListView() {
//        // Realmデータベースから、「全てのデータを取得して新しい日時順に並べた結果」を取得
//        RealmResults<Task> taskRealmResults = mRealm.where(Task.class).findAllSorted("date", Sort.DESCENDING);
//        // 上記の結果を、TaskList としてセットする
//        mTaskAdapter.setTaskList(mRealm.copyFromRealm(taskRealmResults));
//        // TaskのListView用のアダプタに渡す
//        mListView.setAdapter(mTaskAdapter);
//        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
//        mTaskAdapter.notifyDataSetChanged();
//    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }









    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==RC_PASSCHANGE){

            Long calender=data.getLongExtra("calender",0);
            int id = data.getIntExtra("id",0);


//            Intent resultIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
//            resultIntent.setAction(ACTION_TEXT_UPDATE);
//            resultIntent.putExtra(MainActivity.EXTRA_TASK,id);
//
//            PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
//                    this,
//                    id,
//                    resultIntent,
//                    PendingIntent.FLAG_CANCEL_CURRENT
//            );
//
//            AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
//            alarmManager.set(AlarmManager.RTC_WAKEUP, calender, resultPendingIntent);





            Context context = this;
            //AlarmManagerを取得
            AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

            //PendingIntentを作成
            Intent intent = new Intent();
            intent.setAction(ACTION_SET_CLANENDER);
            //AlarmManager#setRepeating()を使用する場合はflagはPendingIntent.FLAG_ONE_SHOTでは駄目(一回通知されて終了してしまう)
            intent.putExtra(MainActivity.EXTRA_TASK,id);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            //AlarmManagerにPendingIntentを登録
            //ELAPSED_REALTIME_WAKEUPは実機がSleep状態でもwakeしてIntentをBroadcastする
            am.set(AlarmManager.RTC_WAKEUP, calender, pendingIntent);

            ButterKnife.bind(this);

        }
    }






    public class AlarmReceiver extends BroadcastReceiver{

        //BroadcastReceiverを拡張したclassでAlarmManagerから発行されたIntentを受信した際の処理を記載
        @Override
        public void onReceive(final Context context, Intent intent) {
            if(intent.getAction().equals(ACTION_SET_CLANENDER)){

                mHandler = new Handler(Looper.getMainLooper());

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.add("testtest");
                    }
                });
                Toast.makeText(context, "onReceive", Toast.LENGTH_SHORT).show();
            }
        }
    }



}


