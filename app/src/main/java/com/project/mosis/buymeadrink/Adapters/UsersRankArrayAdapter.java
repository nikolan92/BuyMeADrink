package com.project.mosis.buymeadrink.Adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;
import com.project.mosis.buymeadrink.DataLayer.UserHandler;
import com.project.mosis.buymeadrink.R;

import java.util.ArrayList;

public class UsersRankArrayAdapter extends ArrayAdapter {

    public UsersRankArrayAdapter(Context context, ArrayList<User> users) {
        super(context, 0, users);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User user = (User) getItem(position);
        UserHandler userHandler = new UserHandler(getContext().getApplicationContext());

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_rank_row, parent, false);
        }

        TextView order = (TextView) convertView.findViewById(R.id.orderNumber);
        TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView email = (TextView) convertView.findViewById(R.id.eMail);
        TextView score = (TextView) convertView.findViewById(R.id.score);
        NetworkImageView userImage = (NetworkImageView)convertView.findViewById(R.id.profilePicture);

        order.setText(String.valueOf(position+1)+ ".");
        name.setText(user.getName());
        email.setText(user.getEmail());
        score.setText(String.valueOf(user.getRating()));

        userHandler.getUserImage(user.getId(),userImage);

        return convertView;
    }
}
