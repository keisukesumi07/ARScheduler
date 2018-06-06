package jp.techacademy.sumi.keisuke.arscheduler;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static jp.techacademy.sumi.keisuke.arscheduler.R.drawable.ic_launcher_foreground;
import android.graphics.drawable.Drawable;

public class CommentAdapter extends BaseAdapter {

    private final Context mContext;
    private final LayoutInflater mInflater;
    private final ArrayList<String> list;

    View views;
    LinearLayout linesr;

    public CommentAdapter(Context context, ArrayList<String> list) {
        this.list = list;
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public String getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }



    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Log.d("a","test2");
        if (view == null) {
            view = mInflater.inflate(R.layout.list_item_viewer, null);
            views=view;
        }

        TextView textView = view.findViewById(R.id.text_user_comment);

        textView.setText(String.valueOf(position));

        String item = getItem(position);
        textView.setText(item);


        return view;
    }





    //ここから追加
    public void add(final String data){
        new Handler(Looper.getMainLooper()).post(new Runnable(){
            @Override
            public void run() {
                if(list.add(data)){
                    notifyDataSetChanged();
                }
            }
        });
    }




}