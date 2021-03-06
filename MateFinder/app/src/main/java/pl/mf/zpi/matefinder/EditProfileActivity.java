package pl.mf.zpi.matefinder;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pl.mf.zpi.matefinder.app.AppConfig;
import pl.mf.zpi.matefinder.app.AppController;
import pl.mf.zpi.matefinder.helper.SQLiteHandler;
import pl.mf.zpi.matefinder.helper.SessionManager;

/**
 * Aktywnosc odpowiadajaca za edycje profilu uzytkownika zalogowanego w aplikacji.
 */
public class EditProfileActivity extends ActionBarActivity {

    private static final String TAG = EditProfileActivity.class.getSimpleName();
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout Drawer;
    ActionBarDrawerToggle mDrawerToggle;
    private ProgressDialog pDialog;
    private Button btn_update;
    private TextView login;
    private TextView email;
    private TextView phone_number;
    private TextView name;
    private TextView surname;
    private ImageView profile_photo;
    private String image_data;
    private SQLiteHandler db;
    private SessionManager session;
    private boolean drawerOpened;

    /**
     * Metoda odpowiedzialna za przeksztalcenie zdjecia w postaci bitmapy na jego bitowa reprezentacje.
     *
     * @param image zdjecie w postaci bitmapy
     * @return zdjecie w postaci ciągu 64 bitow
     */
    private static String encodeToBase64(Bitmap image) {
        System.gc();
        if (image == null)
            return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    /**
     * Metoda wywolywana przy tworzeniu aktywnosci, zawiera inicjalizacje wszelkich potrzebnych parametrow, widokow, bocznego menu.
     *
     * @param savedInstanceState parametr przechowujacy poprzedni stan, w ktorym znajdowala się aktywnosc przed jej zakonczeniem; na jego podstawie odtwarzana jest poprzednia konfiguracja, np. orientacja ekranu
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        login = (TextView) findViewById(R.id.editProfile_login_content);
        email = (TextView) findViewById(R.id.editProfile_email_content);
        phone_number = (TextView) findViewById(R.id.editProfile_phone_content);
        name = (TextView) findViewById(R.id.editProfile_name_content);
        surname = (TextView) findViewById(R.id.editProfile_surname_content);

        profile_photo = (ImageView) findViewById(R.id.editProfile_photo);
        // Change photo button Click Event
        profile_photo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Wybieranie zdjęcia z galerii"), 1);
            }
        });

        TextView change_pass = (TextView) findViewById(R.id.editProfile_changePass_link);
        change_pass.setMovementMethod(LinkMovementMethod.getInstance());
        change_pass.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(EditProfileActivity.this, EditPasswordActivity.class);
                startActivity(intent);
            }
        });

        btn_update = (Button) findViewById(R.id.editProfile_btn_accept);
        // Update button Click Event
        btn_update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                actionUpdate();
            }
        });

        try {
            updateUserInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //wyloguj
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }


        //Boczne menu
        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new MenuAdapter(this, db);
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        Drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);
        mDrawerToggle = new ActionBarDrawerToggle(this, Drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // code here will execute once the drawer is opened( As I dont want anything happened whe drawer is
                // open I am not going to put anything here)
                drawerOpened = true;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Code here will execute once drawer is closed
                drawerOpened = false;
            }
        };
        Drawer.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    /**
     * Metoda odpowiedzialna za przypisanie odpowiedniego, wyspecjalizowanego widoku menu do danej aktywnosci.
     *
     * @param menu parametr, do ktorego przypisywany jest odpowiedni widok
     * @return po dokonaniu przypisania zawsze zwraca wartosc TRUE
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    /**
     * Metoda odpowiedzialna za przypisanie funkcjonalnosci, odpowiednich zachowan aplikacji do poszczegolnych pozycji w menu.
     *
     * @param item wybrana pozycja, do której przypisywana jest okreslona funkcjonalnosc
     * @return zwraca wartosc TRUE po przypisaniu funkcjonalnosci do danej pozycji menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_logout:
                logoutUser();
                Toast.makeText(this, "Wylogowano!", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_change_pass:
                startActivity(new Intent(EditProfileActivity.this, EditPasswordActivity.class));
                return true;
            case R.id.action_change_photo:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Wybieranie zdjęcia z galerii"), 1);
                return true;
            case android.R.id.home:
                //backToMain();
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Metoda odpowiedzialna za pobranie danych zalogowanego uzytkownika z lokalnej bazy danych na urzadzeniu oraz wypelnienie nimi poszczegolnych pol w widoku je wyswietajacym.
     *
     * @throws IOException wyjatek zwracany przy bledzie napotkanym podczas odczytu danych z lokalnej bazy lub podczas przypisywania ich do okreslonych komponentow widoku
     */
    private void updateUserInfo() throws IOException {
        HashMap<String, String> user = db.getUserDetails();

        login.setText(user.get("login"));
        email.setText(user.get("email"));
        phone_number.setText(user.get("phone"));
        name.setText(user.get("name"));
        surname.setText(user.get("surname"));

        loadImageFromStorage();
    }

    /**
     * Metoda odpowiedzialna za pobranie zdjecia uzytkownika, zapisanego w pamięci wewnetrznej urzadzenia oraz wyswietlenie go w komponencie na to przeznaczonym.
     */
    private void loadImageFromStorage() {
        try {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File f = new File(directory, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            profile_photo.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metoda odpowiedzialna za zapisanie zmian dokonanych przez uzytkownika w panelu edycji profilu, po zatwierdzeniu ich przez niego.
     */
    private void actionUpdate() {
        String up_login = login.getText().toString();
        String up_email = email.getText().toString();
        String up_phone = phone_number.getText().toString();
        String up_name = name.getText().toString();
        String up_surname = surname.getText().toString();

        savePhotoToGallery();

        updateUserDB(up_login, up_email, up_phone, up_name, up_surname);
    }

    /**
     * Metoda umozliwiajaca przyciecie zdjecia do rozmiaru ramki, do której zostanie ono przypisane.
     *
     * @param u lokalizacja, adres przycinanego zdjecia
     */
    private void performCrop(Uri u) {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(u, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", 80);
            cropIntent.putExtra("outputY", 80);
            cropIntent.putExtra("return-data", true);
            startActivityForResult(cropIntent, 2);
        } catch (ActivityNotFoundException anfe) {
            Toast toast = Toast
                    .makeText(this, "To urządzenie nie obsługuje przycinania zdjęć!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Metoda odpowiedzialna za pobranie z pamieci urzadzenia wybranego zdjecia uzytkownika.
     *
     * @param reqCode parametr okreslający, jakie dalsze czynnosci powinny zostac wykonane po pobraniu zdjecia; przyjmuje wartosci '1' lub '2'
     * @param resCode parametr okreslajacy, czy pobranie zdjecia z galerii przebieglo pomyslnie; po pomyslnym zakonczeniu operacji, zostaja wykonywane dalsze czynnosci
     * @param data    intencja wywolywana na rzecz danej czynnosci - tutaj: przejscie do galerii, skad wybierane jest zdjecie
     */
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        if (resCode == RESULT_OK) {
            if (reqCode == 2) {
                Bundle extras = data.getExtras();
                Bitmap bitmap = extras.getParcelable("data");
                setImage(bitmap);
            } else if (reqCode == 1) {
                Uri selected_image_uri = data.getData();
                performCrop(selected_image_uri);
            }
        }
    }

    /**
     * Metoda odpowiedzialna za przypisanie wybranego zdjecia do komponentu bedacego odpowiedzialnym za jego przechowywanie i wyswietlanie oraz okreslenie jego bitowej reprezentacji.
     *
     * @param image zdjecie w postaci bitmapy
     */
    private void setImage(Bitmap image) {
        image = Bitmap.createScaledBitmap(image, 80, 80, false);
        profile_photo.setImageBitmap(image);
        //upload
        image_data = encodeToBase64(image);
    }

    /**
     * Metoda odpowiedzialna za zapisanie wybranego, przycietego do okreslonych rozmiarow zdjecia w pamieci urzadzenia.
     */
    private void savePhotoToGallery() {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File my_path = new File(directory, "profile.jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(my_path);
            Bitmap bitmap = ((BitmapDrawable) profile_photo.getDrawable()).getBitmap();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            image_data = encodeToBase64(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Metoda odpowiedzialna za zapis zmodyfikowanych danych uzytkownika oraz jego zdjecia w lokalnej bazie danych urzadzenia, oraz w bazie danch na serwerze.
     *
     * @param login   login uzytkownika
     * @param email   adres e-mail uzytkownika
     * @param phone   numer telefonu uzytkownika
     * @param name    imie uzytkownika
     * @param surname nazwisko uzytkownika
     */
    private void updateUserDB(final String login, final String email, final String phone,
                              final String name, final String surname) {
        String tag_string_req = "update_req";

        pDialog.setMessage("Aktualizowanie informacji...");
        showDialog();

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
                        JSONObject user = jObj.getJSONObject("user");
                        String userID = user.getString("userID");
                        String login = user.getString("login");
                        String email = user.getString("email");
                        String phone = user.getString("phone_number");
                        String name = user.getString("name");
                        String surname = user.getString("surname");
                        String photo = user.getString("photo");
                        String location = user.getString("location");
                        // Inserting row in users table
                        db.deleteUsers();
                        db.addUser(userID, login, email, phone, name, surname, photo, location);
                        mAdapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(), "Zmiany zostały zapisane.", Toast.LENGTH_LONG).show();
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
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "update");
                params.put("login", login);
                params.put("email", email);
                params.put("phone_number", phone);
                params.put("name", name);
                params.put("surname", surname);
                params.put("image", image_data);

                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * Metoda odpowiedzialna za wyswietlanie komunikatu "Aktualizowanie informacji..." po zatwierdzeniu zmian przez uzytkownika.
     */
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    /**
     * Metoda odpowiedzialna za ukrycie komunikatu "Aktualizowanie informacji...", gdy proces aktualizacji zakonczyl sie, to znaczy, gdy dane pomyslnie zostały przekazane na serwer.
     */
    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    /**
     * Metoda odpowiedzialna za przypisanie określonego zachowania dla przycisku 'Wstecz'.
     */
    @Override
    public void onBackPressed() {
        if (!drawerOpened)
            backToMain();
        else
            hideMenu();
    }

    /**
     * Metoda odpowiedzialna za przejscie z danej aktywnosci do aktywnosci glownej - tutaj ekranu znajomych.
     */
    private void backToMain() {
        Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Metoda odpowiedzialna za proces wylogowania uzytkownika z aplikacji.
     */
    private void logoutUser() {
        session.setLogin(false);

        if (MainActivity.doAsynchronousTask != null)
            MainActivity.doAsynchronousTask.cancel();
        MainActivity.doAsynchronousTask = null;

        db.deleteFriends();
        db.deleteGroups();
        db.deleteUsers();

        Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void hideMenu() {
        Drawer.closeDrawers();
    }
}
