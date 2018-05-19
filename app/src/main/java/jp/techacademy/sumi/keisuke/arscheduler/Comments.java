package jp.techacademy.sumi.keisuke.arscheduler;

import android.graphics.drawable.Drawable;

public class Comments {

    public int userId; // コメントしたユーザのID
    public Drawable userIcon; // コメントしたユーザの画像
    public String text; // コメントの内容

    public Comments(int userId, Drawable userIcon, String text) {
        this.userId = userId;
        this.userIcon = userIcon;
        this.text = text;
    }
}