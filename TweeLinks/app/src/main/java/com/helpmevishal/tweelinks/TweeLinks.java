package com.helpmevishal.tweelinks;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;


/**
 * Created by Kushwahz on 23-08-2017.
 */

public class TweeLinks extends Application {
    private static FirebaseDatabase mDatabase;
    @Override
    public void onCreate() {
        super.onCreate();
        if(mDatabase==null){
            mDatabase = FirebaseDatabase.getInstance();
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } else {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
    }
}