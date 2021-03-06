package pl.mf.zpi.matefinder;

import android.graphics.Bitmap;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

import pl.mf.zpi.matefinder.app.AppController;

/**
 * Przechowywanie informacji o znajomych
 */
public class Friend {
    private int id;
    private String login, name, surname, email, phone, photoS;
    private Bitmap photo;

    public Friend(int id, String friendLogin, String friendPhoto){
        this.login = friendLogin;
        this.id = id;
        photoS = friendPhoto;
    }
    public void setPhoto(Bitmap photo){
        this.photo = photo;
    }
    public void getFriendPhoto(){
        String url = "http://156.17.130.212/android_login_api/images/" + photoS;

        ImageRequest ir = new ImageRequest(url, new Response.Listener<Bitmap>(){
            @Override
            public void onResponse(Bitmap bitmap) {
                Log.d("Bitmap", "Bitmap Response: " + bitmap.toString());
                setPhoto(bitmap);
            }
        }, 0, 0, null, null);
        AppController.getInstance().addToRequestQueue(ir, "image_request");
    }

    public String getName(){
        return name!=null?name:"";
    }

    public void setName(String name){
        this.name=name;
    }

    public String getSurname(){
        return surname!=null?surname:"";
    }

    public void setSurname(String surname){
        this.surname=surname;
    }

    public String getPhone(){
        return phone;
    }

    public  void setPhone(String phone){
        this.phone=phone;
    }

    public int getId(){
        return id;
    }

    public String getLogin(){
        return login;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email=email;
    }

    @Override
    public boolean equals(Object f){
        Friend fr = (Friend) f;
        return login.equals(fr.login);
    }
}
