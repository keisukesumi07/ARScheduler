package jp.techacademy.sumi.keisuke.arscheduler;


import android.app.Activity;
import android.app.Application;
import android.content.Context;

public class Global extends Application {
    //グローバルに使用する変数たち
    Context thecontext;

    //ぜんぶ初期化するメソッド
    public void GlobalsAllInit() {
        thecontext=null;
    }
}