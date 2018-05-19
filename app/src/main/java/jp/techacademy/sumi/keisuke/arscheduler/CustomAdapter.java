package jp.techacademy.sumi.keisuke.arscheduler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {

    private ArrayList<String> list;
    private LayoutInflater inflater;

    public CustomAdapter(Context context, ArrayList<String> list) {
        this.list = list;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public boolean add(String data){
        boolean ress = list.add(data);
        if(ress){
            notifyDataSetChanged();
        }
        return ress;
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
}