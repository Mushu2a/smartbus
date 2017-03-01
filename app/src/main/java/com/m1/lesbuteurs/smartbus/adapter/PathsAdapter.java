package com.m1.lesbuteurs.smartbus.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

import com.m1.lesbuteurs.smartbus.R;
import com.m1.lesbuteurs.smartbus.helper.SQLiteHandler;
import com.m1.lesbuteurs.smartbus.qrcode.GenerateQRCode;

public class PathsAdapter extends RecyclerView.Adapter<PathsAdapter.MyViewHolder> {

    private SQLiteHandler db;
    String lastname;
    String firstname;

    private Context mContext;
    private List<Path> pathList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, date;
        public ImageView thumbnail, overflow;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            date = (TextView) view.findViewById(R.id.date);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            overflow = (ImageView) view.findViewById(R.id.overflow);
        }
    }


    public PathsAdapter(Context mContext, List<Path> pathList) {
        this.mContext = mContext;
        this.pathList = pathList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.path_card, parent, false);

        db = new SQLiteHandler(mContext);

        HashMap<String, String> user = db.getUserDetails();

        lastname = user.get("lastname");
        firstname = user.get("firstname");

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Path path = pathList.get(position);
        holder.title.setText(path.getName());
        holder.date.setText(path.getDate());

        final int lastPostion = position;

        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.overflow, lastPostion);
            }
        });
        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.thumbnail, lastPostion);
            }
        });
    }

    /**
     * Montre le popup après clique sur l'icone des 3 petit points
     */
    private void showPopupMenu(View view, final int lastPosition) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_album, popup.getMenu());

        /**
         * Clique sur l'option
         */
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_show_qrcode:
                        Path path = pathList.get(lastPosition);
                        String finalDate = path.getDate().substring(0, 10);
                        Toast.makeText(mContext, "QR Code génération", Toast.LENGTH_SHORT).show();
                        Intent newActivity = new Intent(mContext, GenerateQRCode.class);
                        newActivity.putExtra("qrcode", "Réservation par " + lastname + " " + firstname + " pour " +
                                path.getName() + " le " + finalDate);
                        mContext.startActivity(newActivity);
                        return true;
                    default:
                }
                return false;
            }
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        return pathList.size();
    }
}
