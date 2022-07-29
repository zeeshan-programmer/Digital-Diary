package jbox.skillz.digidiary;

import android.app.Application;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.droidnet.DroidNet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class DigiDiary extends Application {


    public Uri imgUri;
    private DatabaseReference mUserDatabase;
    private FirebaseUser currentUser;

    @Override
    public void onCreate() {
        super.onCreate();

        DroidNet.init(this);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        //--------Picasso---------

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

    }

//    public String get(){
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//        String mImageUri = preferences.getString("image", null);
//            return mImageUri;
//    }

    public void change(Uri uri)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("image", String.valueOf(uri));
        editor.commit();
        this.imgUri = uri;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        DroidNet.getInstance().removeAllInternetConnectivityChangeListeners();
    }

}
