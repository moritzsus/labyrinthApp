package com.example.labyrinthapp;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class PlayerAdapter extends ArrayAdapter<Player> {
    // Konstruktor
    public PlayerAdapter(Context context, int resource, List<Player> items) {
        super(context, resource, items);
    }

    // Überschreibe die getView-Methode
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.player_list_item, parent, false);
        }

        Player player = getItem(position);

        TextView positionTextView = (TextView) view.findViewById(R.id.lv_positionTextView);
        TextView nameTextView = (TextView) view.findViewById(R.id.lv_nameTextView);
        TextView levelTextView = (TextView) view.findViewById(R.id.lv_levelTextView);
        TextView timeTextView = (TextView) view.findViewById(R.id.lv_timeTextView);

        if(GameScreenFragment.getInstance().getGameFinished()) {
            SQLiteHandler sqLiteHandler = new SQLiteHandler(MainActivity.getInstance());
            int pos = sqLiteHandler.getPlayerPosition(player);
            positionTextView.setText(String.valueOf(pos) + ".");
        }
        else {
            positionTextView.setText(String.valueOf(position + 1) + ".");
        }
        nameTextView.setText(player.getName());
        levelTextView.setText(String.valueOf(player.getLevel()));
        timeTextView.setText(String.valueOf(player.getTime()));

        return view;
    }

}

