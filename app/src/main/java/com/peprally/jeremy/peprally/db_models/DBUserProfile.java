package com.peprally.jeremy.peprally.db_models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

@DynamoDBTable(tableName = "UserProfiles")
public class DBUserProfile {
    private String cognitoID;
    private String facebookID;
    private String facebookLink;
    private String email;
    private String firstName;
    private String lastName;
    private String gender;
    private String birthday;
    private String nickname;
    private String favoriteTeam;
    private String favoritePlayer;
    private String pepTalk;
    private String trashTalk;
    private String dateJoined;
    private String team;
    private String FMSInstanceID;
    private int age;
    private int followersCount;
    private int followingCount;
    private int sentFistbumpsCount;
    private int receivedFistbumpsCount;
    private int postsCount;
    private int playerIndex;
    private boolean newUser;
    private boolean isVarsityPlayer;

    @DynamoDBHashKey(attributeName = "Nickname")
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "CognitoID-index", attributeName = "CognitoID")
    public String getCognitoId() {
        return cognitoID;
    }

    public void setCognitoId(String cognitoId) {
        this.cognitoID = cognitoId;
    }

    @DynamoDBAttribute(attributeName = "FacebookID")
    public String getFacebookID() {
        return facebookID;
    }

    public void setFacebookID(String facebookID) {
        this.facebookID = facebookID;
    }

    @DynamoDBAttribute(attributeName = "FacebookLink")
    public String getFacebookLink() {
        return facebookLink;
    }

    public void setFacebookLink(String facebookLink) {
        this.facebookLink = facebookLink;
    }

    @DynamoDBAttribute(attributeName = "Email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @DynamoDBAttribute(attributeName = "NewUser")
    public boolean getNewUser() {
        return newUser;
    }

    public void setNewUser(boolean newUser) {
        this.newUser = newUser;
    }

    @DynamoDBAttribute(attributeName = "FirstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @DynamoDBAttribute(attributeName = "LastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @DynamoDBAttribute(attributeName = "Age")
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @DynamoDBAttribute(attributeName = "Gender")
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @DynamoDBAttribute(attributeName = "Birthday")
    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    @DynamoDBAttribute(attributeName = "FollowersCount")
    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    @DynamoDBAttribute(attributeName = "FollowingCount")
    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    @DynamoDBAttribute(attributeName = "SentFistbumpsCount")
    public int getSentFistbumpsCount() {
        return sentFistbumpsCount;
    }

    public void setSentFistbumpsCount(int sentFistbumpsCount) {
        this.sentFistbumpsCount = sentFistbumpsCount;
    }

    @DynamoDBAttribute(attributeName = "ReceivedFistbumpsCount")
    public int getReceivedFistbumpsCount() {
        return receivedFistbumpsCount;
    }

    public void setReceivedFistbumpsCount(int receivedFistbumpsCount) {
        this.receivedFistbumpsCount = receivedFistbumpsCount;
    }

    @DynamoDBAttribute(attributeName = "PostsCount")
    public int getPostsCount() {
        return postsCount;
    }

    public void setPostsCount(int postsCount) {
        this.postsCount = postsCount;
    }

    @DynamoDBAttribute(attributeName = "FavoriteTeam")
    public String getFavoriteTeam() {
        return favoriteTeam;
    }

    public void setFavoriteTeam(String favoriteTeam) {
        this.favoriteTeam = favoriteTeam;
    }

    @DynamoDBAttribute(attributeName = "FavoritePlayer")
    public String getFavoritePlayer() {
        return favoritePlayer;
    }

    public void setFavoritePlayer(String favoritePlayer) {
        this.favoritePlayer = favoritePlayer;
    }

    @DynamoDBAttribute(attributeName = "PepTalk")
    public String getPepTalk() {
        return pepTalk;
    }

    public void setPepTalk(String pepTalk) {
        this.pepTalk = pepTalk;
    }

    @DynamoDBAttribute(attributeName = "TrashTalk")
    public String getTrashTalk() {
        return trashTalk;
    }

    public void setTrashTalk(String trashTalk) {
        this.trashTalk = trashTalk;
    }

    @DynamoDBAttribute(attributeName = "DateJoined")
    public String getDateJoined() {
        return dateJoined;
    }

    public void setDateJoined(String dateJoined) {
        this.dateJoined = dateJoined;
    }

    @DynamoDBAttribute(attributeName = "IsVarsityPlayer")
    public boolean getIsVarsityPlayer() {
        return isVarsityPlayer;
    }

    public void setIsVarsityPlayer(boolean isVarsityPlayer) {
        this.isVarsityPlayer = isVarsityPlayer;
    }

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "Team-index", attributeName = "PlayerTeam")
    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    @DynamoDBAttribute(attributeName = "PlayerIndex")
    public int getPlayerIndex() {
        return playerIndex;
    }

    public void setPlayerIndex(int index) {
        this.playerIndex = index;
    }

    @DynamoDBAttribute(attributeName = "FMSInstanceID")
    public String getFMSInstanceID() {
        return FMSInstanceID;
    }

    public void setFMSInstanceID(String FMSInstanceID) {
        this.FMSInstanceID = FMSInstanceID;
    }
}
