package com.peprally.jeremy.peprally.adapters;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.peprally.jeremy.peprally.R;
import com.peprally.jeremy.peprally.db_models.DBUserConversation;
import com.peprally.jeremy.peprally.db_models.DBUserNotification;
import com.peprally.jeremy.peprally.messaging.ChatMessage;
import com.peprally.jeremy.peprally.utils.DynamoDBHelper;
import com.peprally.jeremy.peprally.utils.Helpers;
import com.peprally.jeremy.peprally.utils.UserProfileParcel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageArrayAdapter extends ArrayAdapter<ChatMessage> {

    /***********************************************************************************************
     *************************************** CLASS VARIABLES ***************************************
     **********************************************************************************************/
    // AWS Variables
    private DynamoDBHelper dynamoDBHelper;

    // General Variables
    private Context callingContext;
    private List<ChatMessage> messageHistoryList;
    private UserProfileParcel userProfileParcel;

    public MessageArrayAdapter(Context callingContext,
                               ArrayList<ChatMessage> messageHistoryList,
                               UserProfileParcel userProfileParcel) {
        super(callingContext, R.layout.message_right);  // default text view resource id
        this.callingContext = callingContext;
        this.userProfileParcel = userProfileParcel;
        if (messageHistoryList == null)
            this.messageHistoryList = new ArrayList<>();
        else
            this.messageHistoryList = messageHistoryList;

        dynamoDBHelper = new DynamoDBHelper(callingContext);
    }

    @Override
    public void add(ChatMessage message) {
        messageHistoryList.add(message);
        new PushChatMessageToDBAsyncTask().execute(message);
        super.add(message);
    }

    @Override
    public int getCount() {
        return messageHistoryList.size();
    }

    public ChatMessage getItem(int index) {
        return messageHistoryList.get(index);
    }

    public View getView(int position, View messageView, ViewGroup parent) {
        ChatMessage chatMessage = getItem(position);
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (chatMessage.getNickname().equals(userProfileParcel.getCurUserNickname())) {
            messageView = inflater.inflate(R.layout.message_right, parent, false);
        }
        else {
            messageView = inflater.inflate(R.layout.message_left, parent, false);
            ImageView leftImageView = (ImageView) messageView.findViewById(R.id.id_image_view_message_left);
            if (leftImageView != null)
                Helpers.setFacebookProfileImage(callingContext, leftImageView, chatMessage.getFacebookID(), 3);
        }
        TextView textViewMessageText = (TextView) messageView.findViewById(R.id.id_text_view_message_content);
        textViewMessageText.setText(chatMessage.getMessageContent());
        return messageView;
    }

    /***********************************************************************************************
     ****************************************** ASYNC TASKS ****************************************
     **********************************************************************************************/
    private class PushChatMessageToDBAsyncTask extends AsyncTask<ChatMessage, Void, Void> {
        @Override
        protected Void doInBackground(ChatMessage... chatMessages) {
            ChatMessage chatMessage = chatMessages[0];
            DBUserConversation userConversation = dynamoDBHelper.loadDBUserConversation(chatMessage.getConversationID());

            userConversation.setTimeStampLatest(Helpers.getTimestampMiliseconds());
            userConversation.addConversationChatMessage(chatMessage);
            dynamoDBHelper.saveDBObject(userConversation);
            return null;
        }
    }
}
