package com.helpmevishal.tweelinks;

import android.app.DatePickerDialog;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

public class CustomTweet extends AppCompatActivity {
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mFilterDatabaseReference;
    private DatabaseReference mDatabaseReferenceFav;
    private DatabaseReference mDatabaseTweetProps;
    private RecyclerView tweetListView;
    public static String username;
    private ImageView cancel;
    private TextView dateOfTweet, noTweet;
    private int day, month, yearS;
    private String mon, DATE, dayOfmon;
    private int mYear, mMonth, mDay;
    private int tweetCount;
    private LinearLayout tweetHome, tweetSearch, tweetFeed, tweetFav;
    public Boolean mDeleteClicked = false, mLikeClicked =false, mViewClicked = false, mFavClicked = false;
    private SharedPreferences userProfile;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_tweet);

        progress = new ProgressDialog(this);
        progress.setMessage("Loading TweeLinks...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);

        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        dateOfTweet = (TextView) findViewById(R.id.tweet_ondate);

        tweetHome = (LinearLayout) findViewById(R.id.tweet_home_back);
        tweetSearch = (LinearLayout) findViewById(R.id.tweet_search_back);
        tweetFeed = (LinearLayout) findViewById(R.id.tweet_feed_back);
        tweetFav = (LinearLayout) findViewById(R.id.tweet_fav_back);

        noTweet = (TextView) findViewById(R.id.no_tweets);

        userProfile = getApplicationContext().getSharedPreferences("userData", 0);
        username = userProfile.getString("screenName", "@username");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(username).child("Tweets");
        mFilterDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(username).child("CustomTweets");
        mDatabaseTweetProps = FirebaseDatabase.getInstance().getReference().child("Users").child(username).child("TweetProps");
        mDatabaseTweetProps.keepSynced(true);

        mDatabaseReferenceFav = FirebaseDatabase.getInstance().getReference().child("Users").child(username).child("FavTweets");

        cancel = (ImageView) findViewById(R.id.tweet_cancel);

        try{
            DatePickerDialog datePickerDialog = new DatePickerDialog(CustomTweet.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                            final Bundle date = new Bundle();
                            date.putInt("DATE", dayOfMonth);
                            date.putInt("MONTH",monthOfYear);
                            date.putInt("YEAR",year);
                            date.putString("username", username);

                            day = dayOfMonth;
                            month = monthOfYear;
                            yearS = year;

                            mon = getMonthName(month);
                            dayOfmon = getDay(day,month,yearS);
                            DATE = day+" "+mon+" "+yearS;
                            dateOfTweet.setText(dayOfmon+", "+DATE);
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
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();

        } catch (Exception e){

        }


       /* Intent in = getIntent();
        Bundle date = in.getExtras();
        day = date.getInt("DATE",mDay);
        month = date.getInt("MONTH",mMonth);
        yearS = date.getInt("YEAR", mYear);*/

       mFilterDatabaseReference.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               if(!dataSnapshot.exists()){
                   noTweet.setText("Please select Date");
                   noTweet.setVisibility(View.VISIBLE);
               }
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });

        try{
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFilterDatabaseReference.removeValue();
                    Intent intent = new Intent(CustomTweet.this, CustomTweet.class);
                    startActivity(intent);
                    finishAffinity();
                }
            });
        } catch (Exception e){
            Toast.makeText(getApplicationContext(),"Restart TweeLinks", Toast.LENGTH_SHORT).show();
        }

        tweetListView = (RecyclerView) findViewById(R.id.tweets_list_custom);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        tweetListView.setHasFixedSize(true);
        tweetListView.setLayoutManager(layoutManager);

        try{
            tweetHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFilterDatabaseReference.removeValue();
                    startActivity(new Intent(getApplicationContext(), Profile.class));
                    finishAffinity();
                }
            });
            tweetSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFilterDatabaseReference.removeValue();
                    startActivity(new Intent(getApplicationContext(), SearchTweets.class));
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
        final FirebaseRecyclerAdapter<Tweet, TweetViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Tweet, TweetViewHolder>(
                        Tweet.class,
                        R.layout.tweet_row,
                        TweetViewHolder.class,
                        mFilterDatabaseReference
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
                                AlertDialog.Builder builder = new AlertDialog.Builder(CustomTweet.this);
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
                                AlertDialog.Builder builder = new AlertDialog.Builder(CustomTweet.this);
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
            String time = String.valueOf(singleUser.get("CreatedAt"));
            if(time.contains(DATE) && time.contains(dayOfmon+", ")){
                tweetCount++;
                final String ID  = String.valueOf(singleUser.get("ID"));
                mFilterDatabaseReference.child(ID).child("ID").setValue(ID);
                mFilterDatabaseReference.child(ID).child("UserName").setValue(singleUser.get("UserName"));
                mFilterDatabaseReference.child(ID).child("ScreenName").setValue(singleUser.get("ScreenName"));
                mFilterDatabaseReference.child(ID).child("CreatedAt").setValue(singleUser.get("CreatedAt"));
                mFilterDatabaseReference.child(ID).child("Content").setValue(singleUser.get("Content"));
                mFilterDatabaseReference.child(ID).child("ProfileImage").setValue(singleUser.get("ProfileImage"));
            }
        }

        if(tweetCount == 0){
            noTweet.setText("No TweeLinks saved on "+dayOfmon+", "+DATE);
            noTweet.setVisibility(View.VISIBLE);
        } else noTweet.setVisibility(View.GONE);

        progress.hide();
    }

    public String getMonthName(int month){
        String mon;
        switch (month){
            case 0 : mon = "Jan";
                break;
            case 1 : mon = "Feb";
                break;
            case 2 : mon = "Mar";
                break;
            case 3 : mon = "Apr";
                break;
            case 4 : mon = "May";
                break;
            case 5 : mon = "Jun";
                break;
            case 6 : mon = "Jul";
                break;
            case 7 : mon = "Aug";
                break;
            case 8 : mon = "Sep";
                break;
            case 9 : mon = "Oct";
                break;
            case 10 : mon = "Nov";
                break;
            case 11 : mon = "Dec";
                break;
            default: mon = "Aug";
                break;
        }
        return mon;
    }

    public String getDay(int date, int mon, int year){
        String DAY;

        Calendar calendar = new GregorianCalendar(year, mon, date);
        int result = calendar.get(Calendar.DAY_OF_WEEK);
        switch (result) {
            case Calendar.MONDAY:
                DAY = "Mon";
                break;
            case Calendar.TUESDAY:
                DAY = "Tue";
                break;
            case Calendar.WEDNESDAY:
                DAY = "Wed";
                break;
            case Calendar.THURSDAY:
                DAY = "Thu";
                break;
            case Calendar.FRIDAY:
                DAY = "Fri";
                break;
            case Calendar.SATURDAY:
                DAY = "Sat";
                break;
            case Calendar.SUNDAY:
                DAY = "Sun";
                break;
            default: DAY = "Sun";
                break;
        }
        return DAY;
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
                        CustomTweet.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
