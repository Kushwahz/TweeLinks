package com.helpmevishal.tweelinks;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class Profile extends AppCompatActivity {
    private SharedPreferences userProfile;
    private LinearLayout tweetHome, tweetSearch, tweetFeed, tweetDate, tweetFav;
    private LinearLayout signOut;
    private DatabaseReference mDatabaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userProfile = getApplicationContext().getSharedPreferences("userData", 0);
        final SharedPreferences.Editor editor = userProfile.edit();

        final ImageView userProfileImage = (ImageView) findViewById(R.id.im_myPic);
        TextView userName = (TextView) findViewById(R.id.name);
        TextView userScreenName = (TextView) findViewById(R.id.screen_name);
        TextView userDescription = (TextView) findViewById(R.id.description);
        TextView userFriends = (TextView) findViewById(R.id.friends);
        TextView userFollowers = (TextView) findViewById(R.id.followers);

        tweetHome = (LinearLayout) findViewById(R.id.tweet_home_back);
        tweetSearch = (LinearLayout) findViewById(R.id.tweet_search_back);
        tweetFeed = (LinearLayout) findViewById(R.id.tweet_feed_back);
        tweetDate = (LinearLayout) findViewById(R.id.tweet_date_back);
        tweetFav = (LinearLayout) findViewById(R.id.tweet_fav_back);

        String name = userProfile.getString("name", "Name");
        final String screenName = userProfile.getString("screenName", "@username");
        String description = userProfile.getString("description", "No Bio/Description");
        final String profileImage = userProfile.getString("profileImage", "");
        int friendsCount = userProfile.getInt("friendsCount", 0);
        int followersCount = userProfile.getInt("followersCount", 0);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseReference.keepSynced(true);
        mDatabaseReference.child("ProfileImage").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    final String profile = dataSnapshot.getValue().toString();
                    Picasso.with(getApplicationContext()).load(profile).placeholder(R.drawable.ic_default_user)
                            .error(R.drawable.ic_default_user).networkPolicy(NetworkPolicy.OFFLINE)
                            .into(userProfileImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                }
                                @Override
                                public void onError() {
                                    Picasso.with(getApplicationContext()).load(profile).into(userProfileImage);
                                }
                            });

                } else {
                    Picasso.with(getApplicationContext()).load(profileImage).into(userProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        userName.setText(name);
        userScreenName.setText("@"+screenName);
        if(description.isEmpty()){
            userDescription.setText("No Bio Description...");
        } else
            userDescription.setText(description);

        userFriends.setText(friendsCount+" Following");
        userFollowers.setText(followersCount+ " Followers");

        FirebaseDatabase.getInstance().getReference().child("Users").child(screenName).child("CustomTweets").removeValue();

        signOut = (LinearLayout) findViewById(R.id.sign_out);
        try{
            signOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(Profile.this)
                            .setMessage("Sign Out TweeLinks?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    editor.clear();
                                    editor.apply();
                                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                    finishAffinity();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            });
        } catch (Exception e){
        }

        try{
            tweetHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), Profile.class));
                    finishAffinity();
                }
            });
            tweetSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), SearchTweets.class));
                    finishAffinity();
                }
            });
            tweetFeed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), TweetsActivity.class));
                    finishAffinity();
                }
            });
            tweetDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), CustomTweet.class));
                    finishAffinity();
                }
            });
            tweetFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), FavoriteTweets.class));
                    finishAffinity();
                }
            });
        } catch (Exception e){

        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Exit TweeLinks?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finishAffinity();
                        Profile.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
