package jp.techacademy.sumi.keisuke.arscheduler;

import java.io.Serializable;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Task extends RealmObject implements Serializable {
    private String title; // タイトル
    private String contents,category; // 内容
    private Date date,edate; // 日時
    private String place,date2;

    // id をプライマリーキーとして設定
    @PrimaryKey
    private int id;

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDate2() {
        return date2;
    }

    public void setDate2(String date) {
        this.date2 = date;
    }

    public Date geteDate() {
        return edate;
    }

    public void seteDate(Date date) {
        this.edate = date;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


}