package com.helpmevishal.tweelinks;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;
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
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity {
    private static final String API_KEY = "";
    private static final String API_SECRET = "";
    private static final String CALLBACKURL = "https://helpmevishal.wordpress.com/";
    private DatabaseReference mUserDatabase;
    private final OAuthService s = new ServiceBuilder()
            .provider(TwitterApi.SSL.class)
            .apiKey(API_KEY)
            .apiSecret(API_SECRET)
            .callback(CALLBACKURL)
            .build();

    private String token, secret;
    private TwitterLoginButton loginButton;
    private SharedPreferences userProfile;
    private DatabaseReference mDatabase;
    private ProgressDialog progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Twitter.initialize(this);
        setContentView(R.layout.activity_login);

        loginButton = (TwitterLoginButton) findViewById(R.id.login_button);

        userProfile = getApplicationContext().getSharedPreferences("userData", 0);
        if(userProfile.getString("isLoggedIn","FALSE").equals("TRUE")){
            startActivity(new Intent(getApplicationContext(), TweetsActivity.class));
            finishAffinity();
        }

        final SharedPreferences.Editor editor = userProfile.edit();

        progress = new ProgressDialog(this);
        progress.setMessage("Logging in...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        try{
            loginButton.setCallback(new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {
                    loginButton.setVisibility(View.GONE);
                    progress.show();
                    TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                    TwitterAuthToken authToken = session.getAuthToken();
                    token = authToken.token;
                    secret = authToken.secret;
                    editor.putString("token",token);
                    editor.putString("secret", secret);
                    String username = result.data.getUserName();
                    editor.putString("isLoggedIn","TRUE");

                    TwitterCore.getInstance().getApiClient().getAccountService().verifyCredentials(true, true, false).enqueue(new Callback<User>() {
                        @Override
                        public void success(Result<User> result) {
                            String name = result.data.name;
                            final String screenName = result.data.screenName;
                            String description = result.data.description;
                            final String profileImage = result.data.profileImageUrl.replace("_normal","");
                            int friendsCount = result.data.friendsCount;
                            int followersCount = result.data.followersCount;

                            editor.putString("name", name);
                            editor.putString("screenName", screenName);
                            editor.putString("description", description);
                            editor.putString("profileImage", profileImage);
                            editor.putInt("friendsCount", friendsCount);
                            editor.putInt("followersCount", followersCount);
                            editor.commit();

                            mDatabase.child("Users").child(screenName).child("UserName").setValue(screenName);
                            mDatabase.child("Users").child(screenName).child("ProfileImage").setValue(profileImage);

                            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(screenName).child("Tweets");

                            try {
                                new LoginActivity.getTweets().execute().get();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void failure(TwitterException exception) {

                        }
                    });

                }

                @Override
                public void failure(TwitterException exception) {
                    if (!DetectConnection.checkInternetConnection(getApplicationContext())) {
                        Snackbar.make(loginButton, "Can't Login. No Internet Connection!", Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(loginButton, "Login failed. Try again! ", Snackbar.LENGTH_LONG).show();
                    }
                }
            });

        } catch(Exception e){
            Toast.makeText(getApplicationContext(), "Restart TweeLinks!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onRestart() {
        Twitter.initialize(this);
        super.onRestart();
    }

    @Override
    protected void onResume() {
        Twitter.initialize(this);
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    private class getTweets extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Token newAccessToken = new Token(token, secret);
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

                            mUserDatabase.child(id).child("ID").setValue(id);
                            mUserDatabase.child(id).child("UserName").setValue(pUserName);
                            mUserDatabase.child(id).child("ScreenName").setValue(pScreenName);
                            mUserDatabase.child(id).child("CreatedAt").setValue(pCreatedAt);
                            mUserDatabase.child(id).child("Content").setValue(pContent);
                            mUserDatabase.child(id).child("ProfileImage").setValue(profileImage);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(getApplicationContext(), Profile.class);
            startActivity(intent);
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
}
