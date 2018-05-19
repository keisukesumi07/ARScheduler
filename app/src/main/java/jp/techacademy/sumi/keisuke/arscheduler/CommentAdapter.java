package jp.techacademy.sumi.keisuke.arscheduler;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import butterknife.BindView;
import butterknife.ButterKnife;

public class CommentAdapter extends BaseAdapter {

    private final Context mContext;
    private final int mAuthorId;
    private final Comments[] mComments;
    private final LayoutInflater mInflater;

    private static final int NUMBER_OF_VIEW_TYPES = 2;
    private static final int VIEW_TYPE_VIEWER = 0;
    private static final int VIEW_TYPE_AUTHOR = 1;

    public CommentAdapter(Context context, int authorId, Comments[] comments) {
        mContext = context;
        mAuthorId = authorId;
        mComments = comments;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mComments.length;
    }

    @Override
    public Object getItem(int position) {
        return mComments[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return NUMBER_OF_VIEW_TYPES;
    }

    @Override
    public int getItemViewType(int position) {
        boolean isAuthor = mComments[position].userId == mAuthorId;
        return isAuthor ? VIEW_TYPE_AUTHOR : VIEW_TYPE_VIEWER;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Comments comment = mComments[position];

        final CommentViewHolder holder;
        if (view == null) {
            int layoutResourceId = R.layout.list_item_viewer;
            view = mInflater.inflate(layoutResourceId, null);
            holder = new CommentViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (CommentViewHolder) view.getTag();
        }

        holder.commentTextView.setText(comment.text);
        holder.iconImageView.setImageDrawable(comment.userIcon);

        Log.d("comment","setok");

        return view;
    }

    static class CommentViewHolder {

        @BindView(R.id.text_user_comment)
        public TextView commentTextView;

        @BindView(R.id.image_user_icon)
        public ImageView iconImageView;

        public CommentViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}