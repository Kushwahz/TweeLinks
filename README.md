![](/TweeLinks/Banner.png)

# [TweeLinks App](https://play.google.com/store/apps/details?id=com.helpmevishal.tweelinks) 
[!["TweeLinks" App](/TweeLinks/google-play-store.png)](https://play.google.com/store/apps/details?id=com.helpmevishal.tweelinks "TweeLinks")

###### Timeline | 2017-18 (Engineering - Final Year)

People on twitter follow hundreds of people and all those people post everyday many interesting links in their tweets, which you receive in your twitter feed. Sometimes we don’t check our twitter feed regularly and hence miss on all those interesting links shared by others. So to solve this problem, **TweeLinks** application has been built **(TweeLinks = Tweet + Links)**. This application bookmarks all those links in your twitter feed and store them in the application’s database. Now you have all those links saved and you just have to login TweeLinks and check all those links shared by others on a particular day or time.

#### This project was assigned to me by Tapzo (now acquired by Amazon Pay) as part of my college placement process where I had to develop this application in 48 hours. I was able to finish the app and publish it on Google Play Store too within 40 hours.

## Introduction
**TweeLinks** is an android application which has been built using Android Studio 2.3.3. This application allows users to login their Twitter account and automatically save all recent tweets having URLs from Twitter Feed. Then user can view all those saved links later and can also filter those tweet links by particular time.

##### Available on **[Google Play Store](https://play.google.com/store/apps/details?id=com.wordpress.helpmevishal.helpmevishalapp)** 

## Demonstration

![](/TweeLinks/TweeLinks.gif)

## Technology Stack
- Languages	|	Java and XML
- IDE	| Android Studio 2.3.3
- Database	|	Google Firebase
- Graphics Design	|	Adobe Photoshop CS6

## Implemented Concepts
- Android Components/Features/UI Elements:
  - [RecyclerView](https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView)
  - [Google Firebase Database](https://firebase.google.com/docs/database)
  - [Async Task](https://developer.android.com/reference/android/os/AsyncTask)
  - [Twitter API](https://developer.twitter.com/en/docs/api-reference-index)
  - [OAuth Login](https://developers.google.com/identity/protocols/oauth2/native-app)
  - [Picasso OkHttp Downloader](https://github.com/square/picasso)
  - [Linkify](https://developer.android.com/reference/android/text/util/Linkify)
  - [Tab Layout](https://material.io/develop/android/components/tab-layout/)
  - [Shared Preferences](https://developer.android.com/reference/android/content/SharedPreferences)
  - [Progress Dialog](https://developer.android.com/reference/android/app/ProgressDialog)
  - Bean/Model Class, JSONArray and Objects
  - DatePicker, SnackBar, Toast and Contextual Menu
- Graphics Editing | Icons

## Backend Functioning
- This application uses Twitter Core feature of Twitter Kit Android API and hence enables users to authenticate with Twitter.
- To access Twitter account and fetch its data, this app has to be first authenticated with “Keys and Access Tokens” provided by Twitter app dashboard after registering this app by its SHA-1 Fingerprint.
- After getting Token and Secret Key, using Twitter Core’s feature of single sign-on in webview, OAuth process is completed.
- Now this app is authorised to request the data from the logged in account.
- Using the one time created token and secret key, requests can be made to access the Twitter Feeds using GET statuses/home_timeline.
- Once authorised, this app can make 15 requests per 15 minutes window and can fetch up to 800 Tweets.

## Working & Features
- Users have to login only once through their Twitter account credentials, hence one time account setup is required.
- This app has TweeLinks feed/timeline where user will get every tweet having URLs in them. 
- There is a custom tweet search by date, which helps to filter TweeLinks feed by specific date.
- Each TweeLinks feed post has options like mark as viewed, add to favourite and like.
- The “Mark as viewed” feature helps to identify unread TweeLinks feed.
- The “Add to favourites” feature helps to save any feed in a separate featured list to view them later.
- The “Search TweeLinks” tab helps to search keywords in all tweets.
- The “Favourite TweeLinks” tab has all saved tweet URLs for future use.
- “Mobile Offline Capability” allows users to use this app without Internet.
- The “Sign Out” feature allows removing your current account and login other account.

## Application Activities
Below is the functioning and properties of all the activities in the application.

### Twitter Login Activity

![](/TweeLinks/Login_Activity.png)

- Provides Twitter login button.
- Has Internet Connection check functionality.
- Gives warning when not connected to Internet.
- This activity is one time required, because after login, this activity is skipped thereafter.
- This activity is followed by a webview for OAuth login process.

### Twitter Profile Activity

![](/TweeLinks/Profile_Activity.png)

- This activity has got its input from SharedPreferences, saved after Login Activity finished.
- All user profile information is set to their respective views.
- This activity contains profile information like Profile Image, Username, Screen Name, following count, followers count and Bio description.
- This activity has offline capabilities, which means Profile image and other information, will be keep synched, even after not having Internet Connection.
- At the bottom it has bottom navigation controls to navigate through various activities.

### TweeLinks Feeds Activity

![](/TweeLinks/Feeds_Activity.png)

- This is the main tab where all tweets having URLs are requested through Twitter API and saved as a list.
- This list contains only those tweets which have URLs.
- Each post in this feed tab has three features.
- “Mark as Viewed” is used to know which feed is read and which one is unread.
- “Add to Favourite” helps to save any favourite tweet to be read later in a separate list.
- The tweet from this main tab can be deleted if not required by just clicking on the cross image or by long pressing that feed to be deleted.
- List can be updated by Swipe down refresh.

### Favourite TweeLinks Activity

![](/TweeLinks/Fav_Activity.png)

- “The Favourite TweeLinks Tab” is a place where all favourite feeds are saved for future reference in a separate list.
- After clicking on the star icon having plus sign in the centre, the tweet is added to the Favourite TweeLinks list. 
- The favourite tweets are not deleted even if user has deleted it from the main home feed Tab.
- This tab is very useful when user only wants to save some tweets to be read later.
- This tab has offline capabilities and hence can be accessed without Internet.

### Custom Search Activity

![](/TweeLinks/Search.png)

- This is a custom tweet search by date, which helps to filter tweets from the main TweeLinks home list by particular date.
- Selecting any date will show a list of tweets which have been saved on that particular day.
- If no tweets have been saved on the searched date, it will simply notify that you don’t have any tweet on that day.

### Search Result Activity

![](/TweeLinks/Search_Result.png)

- This is the custom feed list searched by date.
- It contains all the tweets saved on the input filtered date.
- User can delete tweets from this custom list, which will eventually remove the same from the main TweeLinks feed list also.
- It helps in finding particular tweets.
- It also has offline capability of filtering tweets from the saved TweeLinks Home feed.

### Keyword Search Activity

![](/TweeLinks/Keyword-Search.png)

- This is a custom tweet search by keyword, which helps to filter tweets from the main TweeLinks home list.
- Entering any keyword will show a list of tweets which have that particular keyword in them. 
- If no tweets have that keyword, it will simply notify that no search result found. 
