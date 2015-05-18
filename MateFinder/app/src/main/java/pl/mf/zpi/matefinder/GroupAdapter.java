package pl.mf.zpi.matefinder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import pl.mf.zpi.matefinder.helper.SQLiteHandler;

/**
 * Created by root on 04.05.15.
 */
public class GroupAdapter extends BaseAdapter {

    protected SQLiteHandler db;
    protected ArrayList<Group> groups;
    protected Context context;
    protected ListView listView;
    protected int index;

    public GroupAdapter(Context c, ListView list) {
        db = new SQLiteHandler(c);
        groups = db.getGroupsDetails();
        context = c;
        listView = list;
        index = -1;
    }

    @Override
    public int getCount() {
        return groups.size();
    }

    @Override
    public Object getItem(int position) {
        return groups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.group_list_element, parent, false);
        TextView name = (TextView) row.findViewById(R.id.groupName);
        Group tmp = groups.get(position);
        name.setText(tmp.getName());
        return row;
    }


}
