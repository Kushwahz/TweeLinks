package com.helpmevishal.tweelinks;

/**
 * Created by Kushwahz on 22-08-2017.
 */

public class Tweet {
    private String ID;
    private String UserName;
    private String ScreenName;
    private String CreatedAt;
    private String Content;
    private String ProfileImage;

    public Tweet() {
    }

    public Tweet(String ID, String userName, String screenName, String createdAt, String content, String profileImage) {
        this.ID = ID;
        UserName = userName;
        ScreenName = screenName;
        CreatedAt = createdAt;
        Content = content;
        ProfileImage = profileImage;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getScreenName() {
        return ScreenName;
    }

    public void setScreenName(String screenName) {
        ScreenName = screenName;
    }

    public String getCreatedAt() {
        return CreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        CreatedAt = createdAt;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public String getProfileImage() {
        return ProfileImage;
    }

    public void setProfileImage(String profileImage) {
        ProfileImage = profileImage;
    }
}
