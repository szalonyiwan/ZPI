package pl.mf.zpi.matefinder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.identity.intents.AddressConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import pl.mf.zpi.matefinder.app.AppConfig;
import pl.mf.zpi.matefinder.app.AppController;
import pl.mf.zpi.matefinder.helper.SQLiteHandler;

/**
 * Created by root on 17.05.15.
 */
public class CheckGroupAdapter extends  GroupAdapter implements View.OnClickListener{

    private int id;
    private static final String TAG = "addToGroup";
    private ProgressDialog pDialog;

    public CheckGroupAdapter(Context c, ListView list, int id) {
        super(c, list);
        this.id=id;

        pDialog = new ProgressDialog(context);
        pDialog.setCancelable(false);

        db = new SQLiteHandler(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.list_check_item, parent, false);
        TextView name = (TextView) row.findViewById(R.id.name);
        Group tmp = groups.get(position);
        name.setText(tmp.getName());
        return row;
    }

    @Override
    public void onClick(View v) {
        pDialog.setMessage("Zapisywanie...");
        showDialog();
        for(int i=0; i<groups.size(); i++){
            View row = listView.getChildAt(i);
            if(((CheckBox)row.findViewById(R.id.check)).isChecked()){
                Group g = groups.get(i);
                addToGroup(g.getID(), id);
            }
        }
        hideDialog();
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
        ((Activity)context).finish();
    }

    private void addToGroup(final int gid, final int mid){
        //TODO dodać json'a (odkomentować i wywalić dodawanie do sqlite)
        db.addMember(gid, mid);
        // Tag used to cancel the request
        //db = new SQLiteHandler(getApplicationContext());
//        String tag_string_req = "addMember_req";
//
//
//        StringRequest strReq = new StringRequest(Request.Method.POST,
//                AppConfig.URL_REGISTER, new Response.Listener<String>() {
//
//            @Override
//            public void onResponse(String response) {
//                Log.d(TAG, "Add Member Response: " + response.toString());
//                hideDialog();
//
//                try {
//                    JSONObject jObj = new JSONObject(response);
//                    boolean error = jObj.getBoolean("error");
//                    if (!error) {
//                        int gid = jObj.getInt("groupID");
//                        db.addMember(gid, mid);
//                        // Launch login activity
//                        //   backToMain();
//                    } else {
//
//                        // Error occurred in registration. Get the error
//                        // message
//                        String errorMsg = jObj.getString("error_msg");
//                        Toast.makeText(context,
//                                errorMsg, Toast.LENGTH_LONG).show();
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }, new Response.ErrorListener() {
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e(TAG, "Adding member Error: " + error.getMessage());
//                Toast.makeText(context,
//                        error.getMessage(), Toast.LENGTH_LONG).show();
//                hideDialog();
//            }
//        }) {
//
//            @Override
//            protected Map<String, String> getParams() {
//                SQLiteHandler db = new SQLiteHandler(context);
//                HashMap<String, String> user = db.getUserDetails();
//                // Posting params to register url
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("tag", "addMember");
//                params.put("groupID", ""+gid);
//                params.put("friendID", ""+mid);
//
//                return params;
//            }
//        };
//
//        // Adding request to request queue
//        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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