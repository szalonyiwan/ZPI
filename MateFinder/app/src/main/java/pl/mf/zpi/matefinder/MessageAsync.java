package pl.mf.zpi.matefinder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import pl.mf.zpi.matefinder.app.AppConfig;
import pl.mf.zpi.matefinder.app.AppController;
import pl.mf.zpi.matefinder.helper.SQLiteHandler;
import pl.mf.zpi.matefinder.helper.SessionManager;

/**
 * Klasa umozliwiająca asynchroniczne pobieranie wiadomosci z serwera. Laczenie z serwerem odbywa się co 1 minute.
 */
public class MessageAsync extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = MessageAsync.class.getSimpleName();
    private Context context;

    private SQLiteHandler db;
    private SessionManager session;

    private boolean[] notif_settings;

    /**
     * Konstruktor pobierajacy dane zalogowanego uzytkownika oraz ustawienia dotyczące sposobu otrzymywania powiadomien
     *
     * @param context        kontekst intencji
     * @param notif_settings tablica parametrow, określajacych ustawienia otrzymywania powiadomien (dzwiek, wibracja, cichy)
     */
    public MessageAsync(Context context, boolean[] notif_settings) {
        Log.d(TAG, "Uruchomiono ASYNC");
        this.context = context;
        db = new SQLiteHandler(context);
        session = new SessionManager(context);
        this.notif_settings = notif_settings;
    }

    /**
     * Metoda odpowiedzialna za pobieranie z serwera nowych wiadomosci, zaproszen do znajomych oraz sprawdzanie, czy dane zaproszenie zostalo akceptowane przez innego uzytkownika. Jest wykonywana w tle.
     *
     * @param params parametry zadania asynchronicznego; argument konieczny w danej metodzie nadpisywanej po nadklasie
     * @return w kazdym wypadku, po wykonaniu wszystkich operacji, zwraca wartosc TRUE
     */
    @Override
    protected Boolean doInBackground(Void... params) {
        getMessages();
        getAcceptedRequests();
        return true;
    }

    /**
     * Metoda odpowiedzialna za ustanowienie polaczenia z serwerem oraz pobranie wiadomosci z bazy danych i zapis ich na urzadzeniu. W przypadku pobrania nowych wiadomosci zostaje utworzone powiadomienie, zgodne z preferencjami uzytkownika.
     */
    private void getMessages() {
        Log.d(TAG, "Sprawdzanie wiadomości...");
        if (session.isLoggedIn()) {
            String tag_string_req = "req_getMessages";
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_REGISTER, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Getting messages Response: " + response.toString());

                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");
                        if (!error) {
                            JSONArray user = jObj.getJSONArray("messages");
                            for (int i = 0; i < user.length(); i++) {
                                JSONObject u = user.getJSONObject(i);
                                final String requestID = u.getString(("messageID"));
                                final String authorLogin = u.getString(("authorLogin"));
                                final String content = u.getString("content");

                                db.addMessage(requestID, authorLogin, content);
                                makeNotification();
                            }
                            MainActivity.refreshMenuIcon(db.allMessagesRead());
                        }
                    } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Message ERROR: " + error.getMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    HashMap<String, String> user = db.getUserDetails();
                    String userID = user.get("userID");
                    params.put("tag", "getMessages");
                    params.put("userID", userID);
                    params.put("type", "1");
                    return params;
                }
            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
    }

    /**
     * Metoda odpowiedzialna za ustanowienie polaczenia z serwerem oraz sprawdzenie, czy inny uzytkownik akceptowal zaproszenie do znajomych, wyslane przez danego. W przypadku, gdy tamten akceptowal zaproszenie, lista znajomych zostaje odswiezona.
     */
    private void getAcceptedRequests() {
        Log.d(TAG, "Sprawdzanie zaproszeń...");
        if (session.isLoggedIn()) {
            String tag_string_req = "req_getAcceptedRequests";
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_REGISTER, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Getting accepted requests Response: " + response.toString());

                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");
                        if (!error) {
                            ZakladkaZnajomi.new_accepted_req = true;
                        }
                    } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Message ERROR: " + error.getMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    HashMap<String, String> user = db.getUserDetails();
                    String userID = user.get("userID");
                    params.put("tag", "getMessages");
                    params.put("userID", userID);
                    params.put("type", "2");
                    return params;
                }
            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
    }

    /**
     * Metoda odpowiedzialna za utworzenie powiadomienia o nowej wiadomosci. W zalezności od preferencji uzytkownika, powiadomieniu zostaje przypisany, dzwiek, wibracja lub tez jest ono tworzone w trybie cichym.
     */
    private void makeNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.drawable.ic_app);
        mBuilder.setContentTitle("MateFinder");
        mBuilder.setContentText("Masz nowe powiadomienia.");
        mBuilder.setAutoCancel(true);

        if (!notif_settings[0] && notif_settings[1]) {
            mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        }

        if (!notif_settings[0] && notif_settings[2]) {
            mBuilder.setVibrate(new long[]{200, 200, 200, 200, 200});
        }

        Intent resultIntent = new Intent(context, MessageActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        int mNotificationId = 001;
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
