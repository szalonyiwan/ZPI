package pl.mf.zpi.matefinder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pl.mf.zpi.matefinder.app.AppConfig;
import pl.mf.zpi.matefinder.app.AppController;
import pl.mf.zpi.matefinder.helper.SQLiteHandler;

/**
 * Adapter listy znajomych wykorzystywany przy dodawaniu do grup. Znajomi sa filtrowani tak zeby nie bylo mozliwosci dodania znajomego do grupy kilkakrotnie
 */
public class FriendCheckAdapter extends BaseAdapter implements View.OnClickListener {

    private static final String TAG = "addGroup";
    private Context context;
    private ArrayList<Friend> friends;
    private int id;
    private ListView listView;
    private SQLiteHandler db;
    private ProgressDialog pDialog;

    public FriendCheckAdapter(Context context, ListView list, int id){
        this.context=context;
        db = new SQLiteHandler(context);
        this.listView=list;
        this.id=id;

        friends =db.getFriends(id);
        ArrayList <Friend> f = db.getMembersDetails(id);
        friends.removeAll(f);

        pDialog = new ProgressDialog(context);
        pDialog.setCancelable(false);

        if(friends.size()==0){
            Toast.makeText(context, "Nie masz wiecej znajomych, których możesz dodać do tej grupy.", Toast.LENGTH_SHORT).show();
            backToMain();
        }
    }


    @Override
    public int getCount() {
        return friends.size();
    }

    @Override
    public Object getItem(int position) {
        return friends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return friends.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.list_check_item, parent, false);
        TextView name = (TextView) row.findViewById(R.id.name);
        Friend tmp = friends.get(position);
        name.setText(tmp.getLogin()+" "+tmp.getName()+" "+tmp.getSurname());
        return row;
    }

    /**
     * Zapisywanie wybranych znajomych do wybranej grupy.
     * @param v
     */
    @Override
    public void onClick(View v) {
        pDialog.setMessage("Zapisywanie...");
        showDialog();
        int count = countChecked();
        int added = 0;
        for(int i=0; i<friends.size(); i++){
            View row = listView.getChildAt(i);
            if(((CheckBox)row.findViewById(R.id.check)).isChecked()){
                Friend f = friends.get(i);
                added++;
                addToGroup(id, f.getId(), added==count);
            }
        }
    }

    private int countChecked(){
        int count = 0;
        for(int i=0; i<friends.size(); i++){
            View row = listView.getChildAt(i);
            if(((CheckBox)row.findViewById(R.id.check)).isChecked()){
                count++;
            }
        }
        return count;
    }

    /**
     * Dodawanie znajomych do wybranej grupy.
     * @param gid id grupy
     * @param mid id dodawanego znajomego
     * @param last czy to ostatni dodawany znajomy
     */
    private void addToGroup(final int gid, final int mid, final boolean last){
        db = new SQLiteHandler(context);
        String tag_string_req = "addMember_req";


        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Add Member Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        db.addMember(gid, mid);
                        if(last)
                            backToMain();
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(context,
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Adding member Error: " + error.getMessage());
                Toast.makeText(context,
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                SQLiteHandler db = new SQLiteHandler(context);
                HashMap<String, String> user = db.getUserDetails();
                // Posting params to register url
                Group g = db.getGroupDetails(gid);
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "addFriendToGroup");
                params.put("userID", db.getUserDetails().get("userID"));
                params.put("groupID", ""+gid);
                params.put("groupName", g.getName());
                params.put("memberID", ""+mid);

                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void backToMain() {
        ((Activity)context).setResult(1);
        ((Activity) context).finish();
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

}
