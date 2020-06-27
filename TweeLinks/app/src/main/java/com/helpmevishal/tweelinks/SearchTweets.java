package com.helpmevishal.tweelinks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class SearchTweets extends AppCompatActivity {
    private EditText searchKeyword;
    private ImageView searchIcon, searchCancel;
    private LinearLayout searchLL;
    private LinearLayout tweetHome, tweetFeed, tweetDate, tweetFav;
    private ProgressDialog progress;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mFilterDatabaseReference;
    private DatabaseReference mDatabaseReferenceFav;
    private DatabaseReference mDatabaseTweetProps;
    private RecyclerView tweetListView;
    public static String username;
    private SharedPreferences userProfile;
    private String KEYWORD;
    private TextView noTweet;
    private boolean isClicked;
    public Boolean mDeleteClicked = false, mLikeClicked =false, mViewClicked = false, mFavClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_tweets);

        progress = new ProgressDialog(this);
        progress.setMessage("Loading TweeLinks...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);

        noTweet = (TextView) findViewById(R.id.no_tweets);

        searchKeyword = (EditText) findViewById(R.id.search_tweets);
        searchIcon = (ImageView) findViewById(R.id.search_icon);
        searchCancel = (ImageView) findViewById(R.id.cancel_search_icon);
        searchLL = (LinearLayout) findViewById(R.id.tweet_search);

        tweetListView = (RecyclerView) findViewById(R.id.tweets_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        tweetListView.setHasFixedSize(true);
        tweetListView.setLayoutManager(layoutManager);

        tweetHome = (LinearLayout) findViewById(R.id.tweet_home_back);
        tweetFeed = (LinearLayout) findViewById(R.id.tweet_feed_back);
        tweetDate = (LinearLayout) findViewById(R.id.tweet_date_back);
        tweetFav = (LinearLayout) findViewById(R.id.tweet_fav_back);

        userProfile = getApplicationContext().getSharedPreferences("userData", 0);
        username = userProfile.getString("screenName", "@username");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(username).child("Tweets");
        mFilterDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(username).child("CustomTweets");
        mDatabaseTweetProps = FirebaseDatabase.getInstance().getReference().child("Users").child(username).child("TweetProps");
        mDatabaseTweetProps.keepSynced(true);
        mDatabaseReferenceFav = FirebaseDatabase.getInstance().getReference().child("Users").child(username).child("FavTweets");

        mFilterDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    noTweet.setVisibility(View.VISIBLE);
                    if(isClicked)
                        noTweet.setText("No Search Results for \""+KEYWORD+"\"");
                }

                else
                    noTweet.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        searchKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals("")){
                    searchIcon.setVisibility(View.GONE);
                }
                else{
                    searchIcon.setVisibility(View.VISIBLE);
                    searchCancel.setVisibility(View.GONE);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        searchCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClicked = false;
                searchKeyword.setText("");
                searchCancel.setVisibility(View.GONE);
                mFilterDatabaseReference.removeValue();
            }
        });

        try{
            searchIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isClicked = true;
                    KEYWORD = searchKeyword.getText().toString().trim();
                    if(!KEYWORD.equals(null)) {
                        hideSoftKeyboard(SearchTweets.this, searchKeyword);
                        searchIcon.setVisibility(View.GONE);
                        searchCancel.setVisibility(View.VISIBLE);
                        progress.show();

                        mDatabaseReference.addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        getCustomTweets((Map<String,Object>) dataSnapshot.getValue());
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Toast.makeText(getApplicationContext(), "Restart TweeLinks", Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                }
            });
        } catch(Exception e){

        }

        try{
            tweetHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFilterDatabaseReference.removeValue();
                    startActivity(new Intent(getApplicationContext(), Profile.class));
                    finishAffinity();
                }
            });
            tweetFeed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFilterDatabaseReference.removeValue();
                    startActivity(new Intent(getApplicationContext(), TweetsActivity.class));
                    finishAffinity();
                }
            });
            tweetDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFilterDatabaseReference.removeValue();
                    startActivity(new Intent(getApplicationContext(), CustomTweet.class));
                    finishAffinity();
                }
            });
            tweetFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFilterDatabaseReference.removeValue();
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
        final FirebaseRecyclerAdapter<Tweet, SearchTweets.TweetViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Tweet, SearchTweets.TweetViewHolder>(
                        Tweet.class,
                        R.layout.tweet_row,
                        SearchTweets.TweetViewHolder.class,
                        mFilterDatabaseReference
                ) {
                    @Override
                    protected void populateViewHolder(SearchTweets.TweetViewHolder viewHolder, final Tweet model, int position) {
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
                                AlertDialog.Builder builder = new AlertDialog.Builder(SearchTweets.this);
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
                                                        mFilterDatabaseReference.child(ID).removeValue();
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
                                AlertDialog.Builder builder = new AlertDialog.Builder(SearchTweets.this);
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
                                                        mFilterDatabaseReference.child(ID).removeValue();
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
        } catch (Exception e){

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

        public void setUserName(String name){
            TextView userName = (TextView) mView.findViewById(R.id.text_UserName);
            userName.setText(name);
        }

        public void setScreenName(String name){
            TextView screenName = (TextView) mView.findViewById(R.id.text_ScreenName);
            screenName.setText("@"+name);
        }

        public void setDate(String date){
            TextView tweetDate = (TextView) mView.findViewById(R.id.tweet_date);
            tweetDate.setText(date);
        }

        public void setContent(String text){
            TextView tweetContent = (TextView) mView.findViewById(R.id.tweet_content);
            tweetContent.setText(text);
            Linkify.addLinks(tweetContent, Linkify.ALL);
            tweetContent.setLinkTextColor(Color.parseColor("#1DA1F3"));
        }

        public void setProfileImage(final Context context, final String image){
            try{
                final String imageURL = image.replaceAll("\\\\","");
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

        public void setViewed(String ID){
            mDatabaseReferenceTweetProps.child(ID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild("Viewed")) {
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

        public void setLike(String ID){
            mDatabaseReferenceTweetProps.child(ID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild("Liked")) {
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

        public void setFav(String ID){
            mDatabaseReferenceTweetProps.child(ID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild("Favorite")) {
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

    private void getCustomTweets(Map<String,Object> users) {
        for (Map.Entry<String, Object> entry : users.entrySet()){
            final Map singleUser = (Map) entry.getValue();
            String tweetUser = String.valueOf(singleUser.get("UserName"));
            String tweetScreen = String.valueOf(singleUser.get("ScreenName"));

            if(tweetUser.contains(KEYWORD) || tweetScreen.contains(KEYWORD)){
                final String ID  = String.valueOf(singleUser.get("ID"));
                mFilterDatabaseReference.child(ID).child("ID").setValue(ID);
                mFilterDatabaseReference.child(ID).child("UserName").setValue(singleUser.get("UserName"));
                mFilterDatabaseReference.child(ID).child("ScreenName").setValue(singleUser.get("ScreenName"));
                mFilterDatabaseReference.child(ID).child("CreatedAt").setValue(singleUser.get("CreatedAt"));
                mFilterDatabaseReference.child(ID).child("Content").setValue(singleUser.get("Content"));
                mFilterDatabaseReference.child(ID).child("ProfileImage").setValue(singleUser.get("ProfileImage"));
            }
        }
        progress.hide();
    }

    public static void hideSoftKeyboard (Activity activity, View view) {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Exit TweeLinks?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseDatabase.getInstance().getReference().child("Users").child(username).child("CustomTweets").removeValue();
                        finishAffinity();
                        SearchTweets.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
