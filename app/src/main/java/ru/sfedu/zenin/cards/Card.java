package ru.sfedu.zenin.cards;

public class Card {
    private String userID;
    private String userName;
    private String profileImageUrl;

    public Card(String userID, String userName, String profileImageUrl){
        this.userID = userID;
        this.userName = userName;
        this.profileImageUrl = profileImageUrl;
    }

    public String getUserID(){
        return userID;
    }
    public void setUserID(String userID){
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String userImageUrl) {
        this.profileImageUrl = userImageUrl;
    }
}
