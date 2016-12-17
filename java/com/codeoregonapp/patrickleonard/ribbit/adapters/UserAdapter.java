package com.codeoregonapp.patrickleonard.ribbit.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.codeoregonapp.patrickleonard.ribbit.R;
import com.codeoregonapp.patrickleonard.ribbit.ui.MainActivity;
import com.codeoregonapp.patrickleonard.ribbit.utils.MD5Util;

import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import java.util.List;

/**
 * Customer ArrayAdapter class
 * Created by Patrick Leonard on 11/15/2015.
 */
public class UserAdapter extends ArrayAdapter<ParseUser> {

    public static final String TAG = UserAdapter.class.getSimpleName();
    protected Context mContext;
    protected List<ParseUser> mUsers;

    public UserAdapter(Context context, List<ParseUser> users) {
        super(context, R.layout.user_item,users);
        mContext = context; mUsers = users;
    }

    @Override
    public int getCount() {
        return mUsers.size();
    }

    @Override
    public ParseUser getItem(int position) {
        return mUsers.get(position);
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.user_item,null);
            viewHolder = new ViewHolder();
            viewHolder.nameLabel = (TextView)convertView.findViewById(R.id.nameLabel);
            viewHolder.userImageView = (ImageView)convertView.findViewById(R.id.user_avatar);
            viewHolder.checkImageView = (ImageView)convertView.findViewById(R.id.checkImageView);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String userName =  mUsers.get(position).getUsername();
        String email = mUsers.get(position).getEmail().toLowerCase();

        String hash="";

        if(email.equals("")) {
            viewHolder.userImageView.setImageResource(R.drawable.avatar_empty);
        }
        else {
            try {
                hash = MD5Util.md5Hex(email);
            }
            catch (UnsupportedEncodingException e) {
                Log.e(UserAdapter.TAG,e.getMessage(),e);
                MainActivity.errorDialogDisplay(mContext,e.getMessage());
            }
            catch (NoSuchAlgorithmException e) {
                Log.e(UserAdapter.TAG,e.getMessage(),e);
                MainActivity.errorDialogDisplay(mContext,e.getMessage());
            }
            String gravatarURL = mContext.getString(R.string.gravatar_URL) + hash
                    + "?s=204&d=404";
            Log.d(UserAdapter.TAG,gravatarURL);
            Picasso.with(mContext)
                    .load(gravatarURL)
                    .placeholder(R.drawable.avatar_empty)
                    .into(viewHolder.userImageView);
        }

        viewHolder.nameLabel.setText(userName);

        GridView gridView = (GridView) parent;

        if(gridView.isItemChecked(position)) {
            viewHolder.checkImageView.setVisibility(View.VISIBLE);
        }
        else {
            viewHolder.checkImageView.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView nameLabel;
        ImageView userImageView;
        ImageView checkImageView;
    }

    public void refill(List<ParseUser> users) {
        mUsers.clear();
        mUsers.addAll(users);
        notifyDataSetChanged();
    }
}
