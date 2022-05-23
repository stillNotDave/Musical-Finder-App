package ru.sfedu.zenin;

public class Cards {
    private String userID;
    private String userName;

    public Cards(String userID, String userName){
        this.userID = userID;
        this.userName = userName;
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
}
