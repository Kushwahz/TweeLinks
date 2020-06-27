package com.helpmevishal.tweelinks;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Callback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class TweetsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String API_KEY = "";
    private static final String API_SECRET = "";
    private static final String CALLBACKURL = "https://helpmevishal.wordpress.com/";
    private DatabaseReference mDatabase;
    private final OAuthService s = new ServiceBuilder()
            .provider(TwitterApi.SSL.class)
            .apiKey(API_KEY)
            .apiSecret(API_SECRET)
            .callback(CALLBACKURL)
            .build();

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mDatabaseReferenceFav;
    private DatabaseReference mDatabaseTweetProps;
    private RecyclerView tweetListView;
    public Boolean mDeleteClicked = false, mLikeClicked =false, mViewClicked = false, mFavClicked = false;
    public static String username;
    private ImageView filter;
    private int mYear, mMonth, mDay;
    private LinearLayout tweetHome, tweetSearch, tweetDate, tweetFav;
    private SharedPreferences userProfile;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressDialog progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweets);

        progress = new ProgressDialog(this);
        progress.setMessage("Loading TweeLinks...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);
        progress.show();

        userProfile = getApplicationContext().getSharedPreferences("userData", 0);
        username = userProfile.getString("screenName", "");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(username).child("Tweets");
        mDatabase.keepSynced(true);
        mDatabaseTweetProps = FirebaseDatabase.getInstance().getReference().child("Users").child(username).child("TweetProps");
        mDatabaseTweetProps.keepSynced(true);
        FirebaseDatabase.getInstance().getReference().child("Users").child(username).child("CustomTweets").removeValue();

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.tweet_refresh);
        swipeRefresh.setOnRefreshListener(this);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(username).child("Tweets");
        mDatabaseReference.keepSynced(true);

        mDatabaseReferenceFav = FirebaseDatabase.getInstance().getReference().child("Users").child(username).child("FavTweets");
        mDatabaseReference.keepSynced(true);

        tweetHome = (LinearLayout) findViewById(R.id.tweet_home_back);
        tweetSearch = (LinearLayout) findViewById(R.id.tweet_search_back);
        tweetDate = (LinearLayout) findViewById(R.id.tweet_date_back);
        tweetFav = (LinearLayout) findViewById(R.id.tweet_fav_back);

        filter = (ImageView) findViewById(R.id.tweet_date);
        try{
            filter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Calendar c = Calendar.getInstance();
                    mYear = c.get(Calendar.YEAR);
                    mMonth = c.get(Calendar.MONTH);
                    mDay = c.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(TweetsActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    final Bundle date = new Bundle();
                                    date.putInt("DATE", dayOfMonth);
                                    date.putInt("MONTH",monthOfYear);
                                    date.putInt("YEAR",year);
                                    date.putString("username", username);

                                    Intent intent = new Intent(getApplicationContext(), CustomTweet.class);
                                    intent.putExtras(date);
                                    startActivity(intent);
                                    finishAffinity();
                                }
                            }, mYear, mMonth, mDay);
                    datePickerDialog.show();
                }
            });

        } catch(Exception e){
            Toast.makeText(getApplicationContext(),"Please restart TweeLinks!", Toast.LENGTH_SHORT).show();
        }

        tweetListView = (RecyclerView) findViewById(R.id.tweets_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        tweetListView.setHasFixedSize(true);
        tweetListView.setLayoutManager(layoutManager);

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
    protected void onStart() {
        super.onStart();

        final FirebaseRecyclerAdapter<Tweet, TweetViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Tweet, TweetViewHolder>(
                        Tweet.class,
                        R.layout.tweet_row,
                        TweetViewHolder.class,
                        mDatabaseReference
                ) {
                    @Override
                    protected void populateViewHolder(TweetViewHolder viewHolder, final Tweet model, int position) {
                        final String ID = model.getID();

                        viewHolder.viewTweetLL.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mViewClicked = true;
                                mDatabaseTweetProps.child(ID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(mViewClicked){
                                            if (dataSnapshot.hasChild("Viewed")) {
                                                mDatabaseTweetProps.child(ID).child("Viewed").removeValue();
                                                mViewClicked = false;
                                                notifyDataSetChanged();
                                            } else {
                                                mDatabaseTweetProps.child(ID).child("Viewed").setValue("TRUE");
                                                mViewClicked = false;
                                                notifyDataSetChanged();
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                            }
                        });

                        viewHolder.setViewed(ID);
                        viewHolder.setLike(ID);
                        viewHolder.setFav(ID);
                        viewHolder.setUserName(model.getUserName());
                        viewHolder.setScreenName(model.getScreenName());
                        viewHolder.setDate(model.getCreatedAt());
                        viewHolder.setContent(model.getContent());
                        viewHolder.setProfileImage(getApplicationContext(), model.getProfileImage());


                        viewHolder.favTweetLL.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mFavClicked = true;
                                mDatabaseTweetProps.child(ID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(mFavClicked){
                                            if (dataSnapshot.hasChild("Favorite")) {
                                                mDatabaseTweetProps.child(ID).child("Favorite").removeValue();
                                                mDatabaseReferenceFav.child(ID).removeValue();
                                                mFavClicked = false;
                                                notifyDataSetChanged();
                                            } else {
                                                mDatabaseTweetProps.child(ID).child("Favorite").setValue("TRUE");
                                                mDatabaseReferenceFav.child(ID).child("ID").setValue(model.getID());
                                                mDatabaseReferenceFav.child(ID).child("UserName").setValue(model.getUserName());
                                                mDatabaseReferenceFav.child(ID).child("ScreenName").setValue(model.getScreenName());
                                                mDatabaseReferenceFav.child(ID).child("CreatedAt").setValue(model.getCreatedAt());
                                                mDatabaseReferenceFav.child(ID).child("Content").setValue(model.getContent());
                                                mDatabaseReferenceFav.child(ID).child("ProfileImage").setValue(model.getProfileImage());
                                                mFavClicked = false;
                                                notifyDataSetChanged();
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                            }
                        });

                        viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(TweetsActivity.this);
                                builder.setCancelable(false);
                                builder.setMessage("Delete this TweeLink?");
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mDeleteClicked = true;
                                        mDatabaseReference.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(mDeleteClicked){
                                                    if(dataSnapshot.exists()){
                                                        mDatabaseReference.child(ID).removeValue();
                                                        mDeleteClicked = false;
                                                    }
                                                }
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                                builder.show();
                            }
                        });

                        viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(TweetsActivity.this);
                                builder.setCancelable(false);
                                builder.setMessage("Delete this TweeLink?");
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mDeleteClicked = true;
                                        mDatabaseReference.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(mDeleteClicked){
                                                    if(dataSnapshot.exists()){
                                                        mDatabaseReference.child(ID).removeValue();
                                                        mDeleteClicked = false;
                                                    }
                                                }
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                                builder.show();
                                return false;
                            }
                        });

                        viewHolder.likeTweetLL.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mLikeClicked = true;
                                mDatabaseTweetProps.child(ID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(mLikeClicked){
                                            if (dataSnapshot.hasChild("Liked")) {
                                                mDatabaseTweetProps.child(ID).child("Liked").removeValue();
                                                mLikeClicked = false;
                                                notifyDataSetChanged();
                                            } else {
                                                mDatabaseTweetProps.child(ID).child("Liked").setValue("TRUE");
                                                mLikeClicked = false;
                                                notifyDataSetChanged();
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                            }
                        });
                    }
                };
        try{
            tweetListView.setAdapter(firebaseRecyclerAdapter);
            tweetListView.scrollToPosition(firebaseRecyclerAdapter.getItemCount() - 1);
            firebaseRecyclerAdapter.notifyDataSetChanged();
            progress.hide();
        } catch (Exception e){
        }
    }

    @Override
    public void onRefresh() {
        try {
            if (!DetectConnection.checkInternetConnection(this)) {
                Snackbar.make(tweetListView, "Can't Refresh TweeLinks. No Internet!", Snackbar.LENGTH_LONG).show();
                swipeRefresh.setRefreshing(false);
            } else {
                swipeRefresh.setRefreshing(true);
                new TweetsActivity.getTweets().execute().get();
                swipeRefresh.setRefreshing(false);
            }
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        }
    }

    public static class TweetViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageView profileImage;
        LinearLayout deleteButton;
        LinearLayout likeTweetLL;
        LinearLayout viewTweetLL;
        LinearLayout favTweetLL;
        ImageView likeTweet;
        ImageView viewTweet;
        ImageView favTweet;
        DatabaseReference mDatabaseReferenceDelete;
        DatabaseReference mDatabaseReferenceTweetProps;

        public TweetViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            likeTweet = (ImageView) mView.findViewById(R.id.tweet_like_im);
            viewTweet = (ImageView) mView.findViewById(R.id.tweet_view_im);
            favTweet = (ImageView) mView.findViewById(R.id.tweet_fav_im);

            likeTweetLL = (LinearLayout) mView.findViewById(R.id.tweet_like);
            viewTweetLL = (LinearLayout) mView.findViewById(R.id.tweet_view);
            favTweetLL = (LinearLayout) mView.findViewById(R.id.tweet_fav);

            profileImage = (ImageView) mView.findViewById(R.id.im_Author);
            deleteButton = (LinearLayout) mView.findViewById(R.id.tweet_delete);
            mDatabaseReferenceDelete = FirebaseDatabase.getInstance().getReference().child("Users").child(username).child("Tweets");
            mDatabaseReferenceDelete.keepSynced(true);
            mDatabaseReferenceTweetProps = FirebaseDatabase.getInstance().getReference().child("Users").child(username).child("TweetProps");
            mDatabaseReferenceTweetProps.keepSynced(true);
        }

        public void setUserName(String name) {
            TextView userName = (TextView) mView.findViewById(R.id.text_UserName);
            userName.setText(name);
        }

        public void setScreenName(String name) {
            TextView screenName = (TextView) mView.findViewById(R.id.text_ScreenName);
            screenName.setText("@" + name);
        }

        public void setDate(String date) {
            TextView tweetDate = (TextView) mView.findViewById(R.id.tweet_date);
            tweetDate.setText(date);
        }

        public void setContent(String text) {
            TextView tweetContent = (TextView) mView.findViewById(R.id.tweet_content);
            tweetContent.setText(text);
            Linkify.addLinks(tweetContent, Linkify.ALL);
            tweetContent.setLinkTextColor(Color.parseColor("#1DA1F3"));
        }

        public void setProfileImage(final Context context, final String image) {
            try {
                final String imageURL = image.replaceAll("\\\\", "");
                Picasso.with(context).load(imageURL).placeholder(R.drawable.ic_twitter_icon)
                        .error(R.drawable.ic_twitter_icon).networkPolicy(NetworkPolicy.OFFLINE).into(profileImage, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        Picasso.with(context).load(imageURL).into(profileImage);
                    }
                });
            } catch (Exception e) {

            }
        }

        public void setViewed(String ID) {
            mDatabaseReferenceTweetProps.child(ID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("Viewed")) {
                        viewTweet.setImageResource(R.drawable.ic_viewed);
                    } else {
                        viewTweet.setImageResource(R.drawable.ic_not_viewed);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        public void setLike(String ID) {
            mDatabaseReferenceTweetProps.child(ID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("Liked")) {
                        likeTweet.setImageResource(R.drawable.ic_liked);
                    } else {
                        likeTweet.setImageResource(R.drawable.ic_like);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        public void setFav(String ID) {
            mDatabaseReferenceTweetProps.child(ID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("Favorite")) {
                        favTweet.setImageResource(R.drawable.ic_favo);
                    } else {
                        favTweet.setImageResource(R.drawable.ic_add_fav);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    private class getTweets extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Token newAccessToken = new Token(userProfile.getString("token", null), userProfile.getString("secret", null));
            final OAuthRequest request = new OAuthRequest(Verb.GET,"https://api.twitter.com/1.1/statuses/home_timeline.json");
            s.signRequest(newAccessToken, request);
            Response response = request.send();
            String body = response.getBody();

            try {
                JSONArray data_list = new JSONArray(body);
                int len = data_list.length();
                for (int i = 0; i < len; i++) {
                    JSONObject item = data_list.getJSONObject(i);
                    try {
                        String pContent = item.get("text").toString();

                        if(pContent.contains("https://")){
                            String id = item.get("id").toString();
                            String user = item.get("user").toString();
                            String pUserName = getName(user);
                            String pScreenName = getScreenName(user);
                            String pCreatedAt = getDate(item.get("created_at").toString());
                            String profileImage = getProfileImage(user);

                            mDatabase.child(id).child("ID").setValue(id);
                            mDatabase.child(id).child("UserName").setValue(pUserName);
                            mDatabase.child(id).child("ScreenName").setValue(pScreenName);
                            mDatabase.child(id).child("CreatedAt").setValue(pCreatedAt);
                            mDatabase.child(id).child("Content").setValue(pContent);
                            mDatabase.child(id).child("ProfileImage").setValue(profileImage);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            startActivity(new Intent(getApplicationContext(), TweetsActivity.class));
            finishAffinity();
            return body;
        }
    }

    private String getName(String user){
        String Name = user;
        String TEST ="\"name\":\"";
        int start = Name.indexOf(TEST);
        Name = Name.substring(start+8);
        int end = Name.indexOf("\"");
        Name = Name.substring(0,end);
        return Name;
    }

    private String getScreenName(String user){
        String screenName = user;
        String TEST ="\"screen_name\":\"";
        int start = screenName.indexOf(TEST);
        screenName = screenName.substring(start+15);
        int end = screenName.indexOf("\"");
        screenName = screenName.substring(0,end);
        return screenName;
    }

    private String getProfileImage(String data){
        String image = data;
        String TEST ="\"profile_image_url_https\":\"";
        int start = image.indexOf(TEST);
        image = image.substring(start+27);
        int end = image.indexOf("\"");
        image = image.substring(0,end);
        return image;
    }
    private String getDate(String createdAt){
        String DATE = createdAt;
        String day = DATE.substring(0,3);
        String date = DATE.substring(8, 10);
        String month = DATE.substring(4, 7);
        String year = DATE.substring(26);
        String GMT = DATE.substring(20, 25);
        String time = DATE.substring(11, 19);
        String inputTime = time+GMT;
        DateFormat df = new SimpleDateFormat("hh:mm:ssZ");
        Date result;
        try {
            result = df.parse(inputTime);
            time = String.valueOf(result).substring(11, 19);
        } catch (Exception e){
        }

        DATE = day+", "+date+" "+month+" "+year+", "+time;
        return DATE;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Exit TweeLinks?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finishAffinity();
                        TweetsActivity.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
