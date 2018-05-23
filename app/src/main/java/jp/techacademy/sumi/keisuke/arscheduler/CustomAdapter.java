package jp.techacademy.sumi.keisuke.arscheduler;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {

    private ArrayList<String> list;
    private LayoutInflater inflater;
    LinearLayout linearLayout;

    public CustomAdapter(Context context, ArrayList<String> list) {
        this.list = list;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = inflater.inflate(R.layout.list_item_viewer,parent,false);
        }
        String item = getItem(position);

        TextView textView1 = convertView.findViewById(R.id.text_user_comment);

        textView1.setText(item);

        return convertView;
    }


    //ここから追加
    public void add(final String data,View view){
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