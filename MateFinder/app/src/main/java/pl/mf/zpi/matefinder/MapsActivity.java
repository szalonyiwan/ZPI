package pl.mf.zpi.matefinder;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.*;import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pl.mf.zpi.matefinder.app.AppConfig;
import pl.mf.zpi.matefinder.app.AppController;
import pl.mf.zpi.matefinder.helper.SQLiteHandler;


public class MapsActivity extends ActionBarActivity implements LocationListener{


    GoogleMap googleMap;
    StreetViewPanorama myStreetView;
    boolean isStreetView=false;
    String provider;
    Location location;
    LocationManager locationManager;
    Criteria criteria;

    private SQLiteHandler db;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private ProgressDialog pDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        createMapView();

        addMarker();
        if(googleMap!=null)
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                createStreetView(latLng);
            }
        });



        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setMyLocalization();
//        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
       // googleMap.getUiSettings().setZoomControlsEnabled(true);
        //googleMap.getUiSettings().setCompassEnabled(true);

         //actionbar
         setSupportActionBar(toolbar);
         getSupportActionBar().setHomeButtonEnabled(true);
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //baza danych
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        db = new SQLiteHandler(getApplicationContext());


    }
    private void updateLocationDB(final String lat,final String lng)
    {
        HashMap<String, String> user = db.getUserDetails();
        final String userId = user.get("userID");
        final String login = user.get("login");
        String tag_string_req = "update_req";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Update Response: " + response.toString());
                hideDialog();

                try {
                   JSONObject jObj = new JSONObject(response);
                   boolean error = jObj.getBoolean("error");
                   if (!error) {
                        // User successfully updated in MySQL
                        // Now store the user in sqlite
                        JSONObject user = jObj.getJSONObject("user");
                      //String userID = user.getString("userID");
                     //  String login = user.getString("login");
                     /*  String email = user.getString("email");
                       String phone = user.getString("phone_number");
                       String name = user.getString("name");
                       String surname = user.getString("surname");
                       String photo = user.getString("photo"); */
                       String location = user.getString("location");


                        // Inserting row in users table
                      //  db.deleteUsers();
                    //    db.addUser(userID, login, email, phone, name, surname, photo,location);
                        Toast.makeText(getApplicationContext(),"Zmiany zostały zapisane.", Toast.LENGTH_LONG).show();
                    } else {
                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Update Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }

        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "updateLocation");
                params.put("userID", userId);
                params.put("lat", lat);
                params.put("lng", lng);

              return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void createStreetView(final LatLng latLng) {


       if(myStreetView == null)
        myStreetView =((StreetViewPanoramaFragment)
                getFragmentManager().findFragmentById(R.id.streetView))
                .getStreetViewPanorama();
        myStreetView.setPosition(latLng);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {


                if (myStreetView.getLocation() != null) {


                    isStreetView = true;
                    Fragment mapView = getFragmentManager().findFragmentById(R.id.mapView);
                    getFragmentManager().beginTransaction().hide(mapView).commit();
                    Fragment street = getFragmentManager().findFragmentById(R.id.streetView);
                    getFragmentManager().beginTransaction().show(street).commit();
                } else
                    Toast.makeText(getApplicationContext(),
                            "StreetView niedostępne w tym miejscu!", Toast.LENGTH_SHORT).show();
            }},1000);






    }
    public LatLng setMyLocalization() {
        LatLng coordinate=null;
        criteria = new Criteria();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        refresh();
        locationManager.requestLocationUpdates(provider,5000,10,this); // odswiezanie co 5 sek lub 10 metrow

        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
        googleMap.animateCamera(zoom);
        if (location != null)
        {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            coordinate = new LatLng(lat, lng);
            CameraUpdate center = CameraUpdateFactory.newLatLng(coordinate);
            googleMap.moveCamera(center);

        }
        else Toast.makeText(getApplicationContext(),"Problem z lokalizacją!", Toast.LENGTH_SHORT).show();


        return coordinate;
    }

    public void refresh()
    {
        provider = locationManager.getBestProvider(criteria, false);
        location = locationManager.getLastKnownLocation(provider);
        if(location!=null)
        updateLocationDB(Double.toString(location.getLatitude()),Double.toString(location.getLongitude()));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            return true;
        }
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createMapView() {
        /**
         * Catch the null pointer exception that
         * may be thrown when initialising the map
         */
        try {
            if (null == googleMap) {
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();


                /**
                 * If the map is still null after attempted initialisation,
                 * show an error to the user
                 */
                if (null == googleMap) {
                    Toast.makeText(getApplicationContext(),
                            "Error creating map", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NullPointerException exception) {
            Log.e("mapApp", exception.toString());
        }
    }

    private void addMarker() {

        /** Make sure that the map has been initialised **/
        if (null != googleMap) {
            LatLng wroclaw = new LatLng(51.107885, 17.038538);
            googleMap.addMarker(new MarkerOptions().position(wroclaw).title("Centrum Wrocławia")
                    .draggable(true));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(wroclaw, 10));

        }

    }
    private String getMyLastLocation() throws IOException {
        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
       String lastLocation;
       return lastLocation = (user.get("location"));
    }

    private void backToMain() {
        // Launching the login activity
        if(isStreetView)
        {

            Fragment street = getFragmentManager().findFragmentById(R.id.streetView);
            getFragmentManager().beginTransaction().hide(street).commit();
            Fragment map = getFragmentManager().findFragmentById(R.id.mapView);
            getFragmentManager().beginTransaction().show(map).commit();
            isStreetView=false;
            myStreetView.setPosition((LatLng)null);
        }
        else
        {
            Intent intent = new Intent(MapsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }
    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
    @Override
    public void onBackPressed()
    {
        backToMain();
    }

    @Override
    public void onLocationChanged(Location location) {
        refresh();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
