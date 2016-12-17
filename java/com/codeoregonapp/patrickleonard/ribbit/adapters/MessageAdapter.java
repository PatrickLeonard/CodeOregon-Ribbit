package com.codeoregonapp.patrickleonard.ribbit.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.codeoregonapp.patrickleonard.ribbit.R;
import com.codeoregonapp.patrickleonard.ribbit.utils.ParseConstants;
import com.parse.ParseObject;

import java.util.Date;
import java.util.List;

/**
 * Customer ArrayAdapter class
 * Created by Patrick Leonard on 11/15/2015.
 */
public class MessageAdapter extends ArrayAdapter<ParseObject> {

    public static final String TAG = MessageAdapter.class.getSimpleName();
    protected Context mContext;
    protected List<ParseObject> mMessages;

    public MessageAdapter(Context context, List<ParseObject> messages) {
        super(context, R.layout.message_item,messages);
        mContext = context; mMessages = messages;
    }

    @Override
    public int getCount() {
        return mMessages.size();
    }

    @Override
    public ParseObject getItem(int position) {
        return mMessages.get(position);
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.message_item,null);
            viewHolder = new ViewHolder();
            viewHolder.nameLabel = (TextView)convertView.findViewById(R.id.senderLabel);
            viewHolder.iconImageView = (ImageView)convertView.findViewById(R.id.messageIcon);
            viewHolder.timeLabel = (TextView) convertView.findViewById(R.id.time_label);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String senderName =  mMessages.get(position).getString(ParseConstants.KEY_SENDER_NAME);
        String fileType = mMessages.get(position).getString(ParseConstants.KEY_FILE_TYPE);
        Date createdAt = mMessages.get(position).getCreatedAt();
        Long now = new Date().getTime();
        String createdDateString = DateUtils.getRelativeTimeSpanString(
                createdAt.getTime(),
                now,
                DateUtils.SECOND_IN_MILLIS).toString();

        Log.v(MessageAdapter.TAG, "Username: " + senderName);
        Log.v(MessageAdapter.TAG, "fileType: " + fileType);
        Log.v(MessageAdapter.TAG, "DateCreated: " + createdDateString);

        if(fileType.equals(ParseConstants.TYPE_IMAGE)) {
            viewHolder.iconImageView.setImageResource(R.drawable.ic_picture);
        }
        else if(fileType.equals(ParseConstants.TYPE_VIDEO)) {
            viewHolder.iconImageView.setImageResource(R.drawable.ic_video);
        }

        viewHolder.nameLabel.setText(senderName);
        viewHolder.timeLabel.setText(createdDateString);
        return convertView;
    }

    private static class ViewHolder {
        TextView nameLabel;
        TextView timeLabel;
        ImageView iconImageView;
    }

    public void refill(List<ParseObject> messages) {
        mMessages.clear();
        mMessages.addAll(messages);
        notifyDataSetChanged();
    }
}
