package com.example.labyrinthapp;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * A custom ArrayAdapter for displaying Player objects in a ListView.
 */
public class PlayerAdapter extends ArrayAdapter<Player> {

    /**
     * Constructs a new PlayerAdapter.
     * @param context The context.
     * @param resource The resource ID for the layout file.
     * @param items The list of Player objects.
     */
    public PlayerAdapter(Context context, int resource, List<Player> items) {
        super(context, resource, items);
    }

    /**
     * Gets a View that displays the player data which it queries from the player
     * SQLite database and sets it at the specified position in the data set.
     * @param position The position of the item within the adapter's data set.
     * @param convertView The old view to reuse, if possible.
     * @param parent The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
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

