package com.peprally.jeremy.peprally.network;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.db_models.DBUserFeedback;
import com.peprally.jeremy.peprally.messaging.ChatMessage;
import com.peprally.jeremy.peprally.messaging.Conversation;
import com.peprally.jeremy.peprally.db_models.DBUserConversation;
import com.peprally.jeremy.peprally.db_models.DBPlayerProfile;
import com.peprally.jeremy.peprally.db_models.DBUserComment;
import com.peprally.jeremy.peprally.db_models.DBUserNickname;
import com.peprally.jeremy.peprally.db_models.DBUserNotification;
import com.peprally.jeremy.peprally.db_models.DBUserPost;
import com.peprally.jeremy.peprally.db_models.DBUserProfile;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.enums.NotificationEnum;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamoDBHelper {

    // AWS Variables
    private AmazonDynamoDBClient ddbClient;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private DynamoDBMapper mapper;

    public DynamoDBHelper(Context callingContext) {
        // Set up AWS members
        refresh(callingContext);
    }

    public void refresh(Context callingContext) {
        credentialsProvider = new CognitoCachingCredentialsProvider(
                callingContext,                                         // Context
                AWSCredentialProvider.IDENTITY_POOL_ID,                 // Identity Pool ID
                AWSCredentialProvider.COGNITO_REGION                    // Region
        );
        ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);
    }

    public String getIdentityID() {
        return credentialsProvider.getIdentityId();
    }

    public DynamoDBMapper getMapper() {
        return mapper;
    }

    // For AsyncTasks that need to have a callback function back in the activity once it finishes
    public interface AsyncTaskCallback {
        void onTaskDone();
    }

    /***********************************************************************************************
     *************************************** DATABASE METHODS **************************************
     **********************************************************************************************/
    public void saveDBObject(Object object) {
        mapper.save(object);
    }

    public void saveDBObjectAsync(Object object) {
        new SaveDBObjectAsyncTask().execute(object);
    }

    public void deleteDBObject(Object object) {
        mapper.delete(object);
    }

    // Database load Methods

    public DBUserProfile loadDBUserProfile(String postNickname) {
        return mapper.load(DBUserProfile.class, postNickname);
    }

    public DBPlayerProfile loadDBPlayerProfile(String playerTeam, Integer playerIndex) {
        return mapper.load(DBPlayerProfile.class, playerTeam, playerIndex);
    }

    public DBUserPost loadDBUserPost(String postNickname, Long timeStampInSeconds) {
        return mapper.load(DBUserPost.class, postNickname, timeStampInSeconds);
    }

    public DBUserComment loadDBUserComment(String postID) {
        return mapper.load(DBUserComment.class, postID);
    }

    public DBUserNickname loadDBNickname(String nickname) {
        return mapper.load(DBUserNickname.class, nickname);
    }

    public DBUserConversation loadDBUserConversation(String conversationID) {
        return mapper.load(DBUserConversation.class, conversationID);
    }

    public void updateFirebaseInstanceID(String newInstanceID) {
        new UpdateFirebaseInstanceIDAsyncTask().execute(newInstanceID);
    }

    // Database save methods

    public void updateUserEmailPreferences(String nickname, String email) {
        new UpdateUserEmailAsyncTask().execute(nickname, email);
    }

    public void incrementUserSentFistbumpsCount(String nickname) {
        new IncrementUserSentFistbumpsCountAsyncTask().execute(nickname);
    }

    public void decrementUserSentFistbumpsCount(String nickname) {
        new DecrementUserSentFistbumpsCountAsyncTask().execute(nickname);
    }

    public void incrementUserReceivedFistbumpsCount(String nickname) {
        new IncrementUserReceivedFistbumpsCountAsyncTask().execute(nickname);
    }

    public void decrementUserReceivedFistbumpsCount(String nickname) {
        new DecrementUserReceivedFistbumpsCountAsyncTask().execute(nickname);
    }

    public void incrementPostCommentsCount(DBUserPost userPost) {
        new IncrementPostCommentsCountAsyncTask().execute(userPost);
    }

    public void decrementPostCommentsCount(DBUserPost userPost) {
        new DecrementPostCommentsCountAsyncTask().execute(userPost);
    }

    public void makeNewNotification(Bundle bundle) {
        new MakeNewDBUserNotificationAsyncTask().execute(bundle);
    }

    public void makeNewFeedback(Bundle bundle) {
        new MakeNewDBUserFeedbackAsyncTask().execute(bundle);
    }

    public void deleteCommentFistbumpNotification(NotificationEnum notificationEnum, String commentID, String senderNickname) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("NOTIFICATION_ENUM", notificationEnum);
        bundle.putString("COMMENT_ID", commentID);
        bundle.putString("SENDER_NICKNAME", senderNickname);
        new DeleteDBUserNotificationAsyncTask().execute(bundle);
    }

    public void deletePostFistbumpNotification(NotificationEnum notificationEnum, String postID, String senderNickname) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("NOTIFICATION_ENUM", notificationEnum);
        bundle.putString("POST_ID", postID);
        bundle.putString("SENDER_NICKNAME", senderNickname);
        new DeleteDBUserNotificationAsyncTask().execute(bundle);
    }

    public void deletePostCommentNotification(NotificationEnum notificationEnum, DBUserComment userComment) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("NOTIFICATION_ENUM", notificationEnum);
        bundle.putParcelable("USER_COMMENT", userComment);
        new DeleteDBUserNotificationAsyncTask().execute(bundle);
    }

    // Database create methods

    public void createNewConversation(String nickname1, String nickname2) {
        new CreateNewDBUserConversationAsyncTask().execute(nickname1, nickname2);
    }

    // Database delete methods

    public void deleteUserPost(DBUserPost userPost, AsyncTaskCallback taskCallback) {
        new DeleteDBUserPostAsyncTask(taskCallback).execute(userPost);
    }

    public void deleteUserAccount(UserProfileParcel userProfileParcel, AsyncTaskCallback taskCallback) {
        new DeleteDBUserProfileAsyncTask().execute(userProfileParcel);
        new DeleteDBUserNicknameAsyncTask().execute(userProfileParcel.getCurUserNickname());
        new BatchDeleteDBUserPostsAsyncTask(taskCallback).execute(userProfileParcel);
    }

    public void batchDeleteCommentNotifications(DBUserComment userComment) {
        new BatchDeleteCommentDBUserNotificationsAsyncTask().execute(userComment);
    }

    public void batchDeletePostNotifications(DBUserPost userPost) {
        new BatchDeletePostDBUserNotificationsAsyncTask().execute(userPost);
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class SaveDBObjectAsyncTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            mapper.save(params[0]);
            return null;
        }
    }

    // UserProfile Tasks

    private class UpdateUserEmailAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String nickname = strings[0];
            String email = strings[1];
            DBUserProfile userProfile = loadDBUserProfile(nickname);
            if (userProfile != null) {
                userProfile.setEmail(email);
                saveDBObject(userProfile);
            }
            return null;
        }
    }

    private class DeleteDBUserProfileAsyncTask extends AsyncTask<UserProfileParcel, Void, Void> {
        @Override
        protected Void doInBackground(UserProfileParcel... userProfileParcels) {
            UserProfileParcel userProfileParcel = userProfileParcels[0];
            // First delete user profile and username
            DBUserProfile userProfile = loadDBUserProfile(userProfileParcel.getCurUserNickname());
            DBUserNickname userNickname = loadDBNickname(userProfileParcel.getCurUserNickname());
            mapper.delete(userNickname);
            mapper.delete(userProfile);

            // delete all posts
            if (userProfileParcel.getPostsCount() != null && userProfileParcel.getPostsCount() > 0) {
                DBUserPost userPost = new DBUserPost();
                userPost.setNickname(userProfileParcel.getCurUserNickname());
                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression<DBUserPost>()
                        .withIndexName("Nickname-index")
                        .withHashKeyValues(userPost)
                        .withConsistentRead(false);

                PaginatedQueryList<DBUserPost> queryResults = mapper.query(DBUserPost.class, queryExpression);
                if (queryResults != null && queryResults.size() > 0) {
                    for (DBUserPost post : queryResults) {
                        mapper.delete(post);
                    }
                }
            }

            // delete all comments

            // delete all notification
            return null;
        }
    }

    private class DeleteDBUserNicknameAsyncTask extends AsyncTask<String,  Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String username = strings[0];
            DBUserNickname dbUsername = mapper.load(DBUserNickname.class, username);
            if (dbUsername != null)
                mapper.delete(dbUsername);
            return null;
        }
    }

    // Post/Comment Tasks
    private class IncrementUserSentFistbumpsCountAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... userNickname) {
            DBUserProfile userProfile = mapper.load(DBUserProfile.class, userNickname[0]);
            userProfile.setSentFistbumpsCount(userProfile.getSentFistbumpsCount() + 1);
            mapper.save(userProfile);
            return null;
        }
    }

    private class DecrementUserSentFistbumpsCountAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... userNickname) {
            DBUserProfile userProfile = mapper.load(DBUserProfile.class, userNickname[0]);
            userProfile.setSentFistbumpsCount(userProfile.getSentFistbumpsCount() - 1);
            mapper.save(userProfile);
            return null;
        }
    }

    private class IncrementUserReceivedFistbumpsCountAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... userNickname) {
            DBUserProfile userProfile = mapper.load(DBUserProfile.class, userNickname[0]);
            userProfile.setReceivedFistbumpsCount(userProfile.getReceivedFistbumpsCount() + 1);
            mapper.save(userProfile);
            return null;
        }
    }

    private class DecrementUserReceivedFistbumpsCountAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... userNickname) {
            DBUserProfile userProfile = mapper.load(DBUserProfile.class, userNickname[0]);
            userProfile.setReceivedFistbumpsCount(userProfile.getReceivedFistbumpsCount() - 1);
            mapper.save(userProfile);
            return null;
        }
    }

    private class IncrementPostCommentsCountAsyncTask extends AsyncTask<DBUserPost, Void, Void> {
        @Override
        protected Void doInBackground(DBUserPost... params) {
            DBUserPost userPost = params[0];
            if (userPost != null) {
                userPost.setCommentsCount(userPost.getCommentsCount() + 1);
                mapper.save(userPost);
            }
            return null;
        }
    }

    private class DecrementPostCommentsCountAsyncTask extends AsyncTask<DBUserPost, Void, Void> {
        @Override
        protected Void doInBackground(DBUserPost... params) {
            DBUserPost userPost = params[0];
            if (userPost != null) {
                userPost.setCommentsCount(userPost.getCommentsCount() - 1);
                mapper.save(userPost);
            }
            return null;
        }
    }

    private class DeleteDBUserPostAsyncTask extends AsyncTask<DBUserPost, Void, Void> {

        private AsyncTaskCallback taskCallback;

        private DeleteDBUserPostAsyncTask(AsyncTaskCallback taskCallback) {
            this.taskCallback = taskCallback;
        }

        @Override
        protected Void doInBackground(DBUserPost... dbUserPosts) {
            DBUserPost userPost = dbUserPosts[0];

            if (userPost != null && userPost.getCommentsCount() > 0) {
                // query for all the comments under this post
                DBUserComment userComment = new DBUserComment();
                userComment.setPostID(userPost.getPostId());
                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                        .withHashKeyValues(userComment)
                        .withConsistentRead(true);
                List<DBUserComment> results = mapper.query(DBUserComment.class, queryExpression);

                // delete the comments under the post
                for (DBUserComment comment : results) {
                    mapper.delete(comment);
                }
            }
            mapper.delete(userPost);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            taskCallback.onTaskDone();
        }
    }

    private class BatchDeleteDBUserPostsAsyncTask extends AsyncTask<UserProfileParcel, Void, Void> {

        private AsyncTaskCallback taskCallback;

        private BatchDeleteDBUserPostsAsyncTask(AsyncTaskCallback taskCallback) {
            this.taskCallback = taskCallback;
        }

        @Override
        protected Void doInBackground(UserProfileParcel... userProfileParcels) {
            UserProfileParcel userProfileParcel = userProfileParcels[0];
            if (userProfileParcel.getPostsCount() != null && userProfileParcel.getPostsCount() > 0) {
                String username = userProfileParcel.getCurUserNickname();
                DBUserPost userPost = new DBUserPost();
                userPost.setNickname(username);
                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression<DBUserPost>()
                        .withIndexName("Nickname-index")
                        .withHashKeyValues(userPost)
                        .withConsistentRead(false);

                PaginatedQueryList<DBUserPost> queryResults = mapper.query(DBUserPost.class, queryExpression);
                if (queryResults != null && queryResults.size() > 0) {
                    for (DBUserPost post : queryResults) {
                        mapper.delete(post);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            taskCallback.onTaskDone();
        }
    }

    private class BatchDeleteDBUserCommentsAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String username = strings[0];
            DBUserPost userPost = new DBUserPost();
            userPost.setNickname(username);
            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression<DBUserPost>()
                    .withIndexName("Nickname-index")
                    .withHashKeyValues(userPost)
                    .withConsistentRead(false);

            PaginatedQueryList<DBUserPost> queryResults = mapper.query(DBUserPost.class, queryExpression);
            if (queryResults != null && queryResults.size() > 0) {
                for (DBUserPost post : queryResults) {
                    mapper.delete(post);
                }
            }
            return null;
        }
    }

    // Notification Tasks
    private class MakeNewDBUserNotificationAsyncTask extends AsyncTask<Bundle, Void, Void> {
        @Override
        protected Void doInBackground(Bundle... params) {
            Bundle bundle = params[0];
            UserProfileParcel userProfileParcel = bundle.getParcelable("USER_PROFILE_PARCEL");
            DBUserNotification userNotification = new DBUserNotification();
            // getting time stamp
            userNotification.setTimeInSeconds(Helpers.getTimestampSeconds());
            userNotification.setTimeStamp(Helpers.getTimestampString());
            // setting up new user notification
            userNotification.setNickname(bundle.getString("RECEIVER_NICKNAME")); // who the notification is going to
            if (userProfileParcel != null) {
                userNotification.setNicknameSender(userProfileParcel.getCurUserNickname());
                userNotification.setFacebookIDSender(userProfileParcel.getFacebookID());
            }

            NotificationEnum notificationType = NotificationEnum.fromInt(bundle.getInt("NOTIFICATION_TYPE"));
            if (notificationType != null) {
                switch (notificationType) {
                    case DIRECT_FISTBUMP:
                        userNotification.setNotificationType(notificationType.toInt());
                        userNotification.setNicknameSender(bundle.getString("SENDER_NICKNAME"));
                        DBUserProfile senderProfile = loadDBUserProfile(bundle.getString("SENDER_NICKNAME"));
                        userNotification.setFacebookIDSender(senderProfile.getFacebookId());
                        break;
                    case POST_COMMENT:
                        userNotification.setNotificationType(notificationType.toInt());
                        userNotification.setPostID(bundle.getString("POST_ID"));
                        userNotification.setCommentID(bundle.getString("COMMENT_ID"));
                        userNotification.setComment(bundle.getString("COMMENT"));
                        break;
                    case POST_FISTBUMP:
                        userNotification.setNotificationType(notificationType.toInt());
                        userNotification.setPostID(bundle.getString("POST_ID"));
                        break;
                    case COMMENT_FISTBUMP:
                        userNotification.setNotificationType(notificationType.toInt());
                        userNotification.setPostID(bundle.getString("POST_ID"));
                        userNotification.setCommentID(bundle.getString("COMMENT_ID"));
                        break;
                    default:
                        userNotification.setNotificationType(-1);   // invalid notification type
                        break;
                }
            }

            // set receiver profile's newNotification flag to true
            DBUserProfile receiverUserProfile = loadDBUserProfile(bundle.getString("RECEIVER_NICKNAME"));
            if (receiverUserProfile != null) {
                receiverUserProfile.setHasNewNotification(true);
                saveDBObject(receiverUserProfile);
            }

            saveDBObject(userNotification);
            return null;
        }
    }

    private class DeleteDBUserNotificationAsyncTask extends AsyncTask<Bundle, Void, Void> {
        @Override
        protected Void doInBackground(Bundle... params) {
            Bundle bundle = params[0];
            NotificationEnum notificationType = (NotificationEnum) bundle.get("NOTIFICATION_ENUM");

            if (notificationType != null) {
                DBUserNotification userNotification = new DBUserNotification();
                DynamoDBQueryExpression<DBUserNotification> queryExpression = null;
                Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
                switch (notificationType) {
                    case POST_COMMENT:
                        DBUserComment postComment = bundle.getParcelable("USER_COMMENT");
                        expressionAttributeValues.put(":type", new AttributeValue().withN("1"));
                        if (postComment != null) {
                            userNotification.setPostID(postComment.getPostID());
                            queryExpression = new DynamoDBQueryExpression<DBUserNotification>()
                                    .withIndexName("PostID-CommentID-index")
                                    .withHashKeyValues(userNotification)
                                    .withRangeKeyCondition("CommentID", new Condition()
                                            .withComparisonOperator(ComparisonOperator.EQ)
                                            .withAttributeValueList(new AttributeValue().withS(postComment.getCommentId())))
                                    .withFilterExpression("NotificationType = :type")
                                    .withExpressionAttributeValues(expressionAttributeValues)
                                    .withConsistentRead(false);
                        }
                        break;
                    case POST_FISTBUMP:
                        userNotification.setPostID(bundle.getString("POST_ID"));
                        expressionAttributeValues.put(":type", new AttributeValue().withN("2"));
                        queryExpression = new DynamoDBQueryExpression<DBUserNotification>()
                                .withIndexName("PostID-SenderNickname-index")
                                .withHashKeyValues(userNotification)
                                .withRangeKeyCondition("SenderNickname", new Condition()
                                        .withComparisonOperator(ComparisonOperator.EQ)
                                        .withAttributeValueList(new AttributeValue().withS(bundle.getString("SENDER_NICKNAME"))))
                                .withFilterExpression("NotificationType = :type")
                                .withExpressionAttributeValues(expressionAttributeValues)
                                .withConsistentRead(false);
                        break;
                    case COMMENT_FISTBUMP:
                        userNotification.setCommentID(bundle.getString("COMMENT_ID"));
                        queryExpression = new DynamoDBQueryExpression<DBUserNotification>()
                                .withIndexName("CommentID-SenderNickname-index")
                                .withHashKeyValues(userNotification)
                                .withRangeKeyCondition("SenderNickname", new Condition()
                                        .withComparisonOperator(ComparisonOperator.EQ)
                                        .withAttributeValueList(new AttributeValue().withS(bundle.getString("SENDER_NICKNAME"))))
                                .withConsistentRead(false);
                        break;
                }

                if (queryExpression != null) {
                    List<DBUserNotification> queryResults = mapper.query(DBUserNotification.class, queryExpression);
                    // make sure only 1 entry is found
                    if (queryResults != null && queryResults.size() == 1) {
                        mapper.delete(queryResults.get(0));
                    }
                }
            }
            return null;
        }
    }

    private class BatchDeletePostDBUserNotificationsAsyncTask extends AsyncTask<DBUserPost, Void, Void> {
        @Override
        protected Void doInBackground(DBUserPost... params) {
            DBUserPost post = params[0];
            if (post != null) {
                DBUserNotification userNotification = new DBUserNotification();
                userNotification.setPostID(post.getPostId());
                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression<DBUserNotification>()
                        .withIndexName("PostID-index")
                        .withHashKeyValues(userNotification)
                        .withConsistentRead(false);

                PaginatedQueryList<DBUserNotification> queryResults = mapper.query(DBUserNotification.class, queryExpression);
                if (queryResults != null && queryResults.size() > 0) {
                    for (DBUserNotification notification : queryResults) {
                        mapper.delete(notification);
                    }
                }
            }
            return null;
        }
    }

    private class BatchDeleteCommentDBUserNotificationsAsyncTask extends AsyncTask<DBUserComment, Void, Void> {
        @Override
        protected Void doInBackground(DBUserComment... params) {
            DBUserComment comment = params[0];
            if (comment != null) {
                DBUserNotification userNotification = new DBUserNotification();
                userNotification.setCommentID(comment.getCommentId());
                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression<DBUserNotification>()
                        .withIndexName("CommentID-index")
                        .withHashKeyValues(userNotification)
                        .withConsistentRead(false);

                PaginatedQueryList<DBUserNotification> queryResults = mapper.query(DBUserNotification.class, queryExpression);
                if (queryResults != null) {
                    for (DBUserNotification notification : queryResults) {
                        mapper.delete(notification);
                    }
                }
            }
            return null;
        }
    }

    // Messaging Tasks
    private class CreateNewDBUserConversationAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... nicknames) {
            DBUserProfile fistbumpedUserProfile1 = loadDBUserProfile(nicknames[0]);
            DBUserProfile fistbumpedUserProfile2 = loadDBUserProfile(nicknames[1]);
            if (fistbumpedUserProfile1 != null && fistbumpedUserProfile2 != null) {
                DBUserConversation newConversation = new DBUserConversation();
                String conversation_id = fistbumpedUserProfile1.getFacebookId() + "_" + fistbumpedUserProfile2.getFacebookId();
                newConversation.setConversationID(conversation_id);
                Long timeInSeconds = Helpers.getTimestampSeconds();
                newConversation.setTimeStampCreated(timeInSeconds);
                newConversation.setTimeStampLatest(timeInSeconds);
                Map<String, String> nicknameFacebookIDMap = new HashMap<>();
                nicknameFacebookIDMap.put(fistbumpedUserProfile1.getNickname(), fistbumpedUserProfile1.getFacebookId());
                nicknameFacebookIDMap.put(fistbumpedUserProfile2.getNickname(), fistbumpedUserProfile2.getFacebookId());
                newConversation.setConversation(new Conversation(conversation_id, new ArrayList<ChatMessage>(), nicknameFacebookIDMap));

                // append conversation_id to each user
                mapper.save(newConversation);
                fistbumpedUserProfile1.addConversationId(conversation_id);
                fistbumpedUserProfile2.addConversationId(conversation_id);
                mapper.save(fistbumpedUserProfile1);
                mapper.save(fistbumpedUserProfile2);
            }
            return null;
        }
    }

    private class MakeNewDBUserFeedbackAsyncTask extends AsyncTask<Bundle, Void, Void> {
        @Override
        protected Void doInBackground(Bundle... bundles) {
            Bundle bundle = bundles[0];
            DBUserFeedback userFeedback = new DBUserFeedback(bundle.getString("USERNAME"), bundle.getLong("TIMESTAMP"));
            userFeedback.setFeedbackType(bundle.getInt("FEEDBACK_TYPE"));
            userFeedback.setFeedback(bundle.getString("FEEDBACK"));
            userFeedback.setPlatform("Android");
            mapper.save(userFeedback);
            return null;
        }
    }

    // Other Tasks
    private class UpdateFirebaseInstanceIDAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String newInstanceID = strings[0];
            // Query for userProfile using cognitoID
            DBUserProfile userProfile = new DBUserProfile();
            userProfile.setCognitoId(credentialsProvider.getIdentityId());
            DynamoDBQueryExpression<DBUserProfile> queryExpression = new DynamoDBQueryExpression<DBUserProfile>()
                    .withIndexName("CognitoId-index")
                    .withHashKeyValues(userProfile)
                    .withConsistentRead(false);
            List<DBUserProfile> results = mapper.query(DBUserProfile.class, queryExpression);
            if (results != null && results.size() == 1) {
                userProfile = results.get(0);
                userProfile.setFCMInstanceId(newInstanceID);
            }
            return null;
        }
    }
}