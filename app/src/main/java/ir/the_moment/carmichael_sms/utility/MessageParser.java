package ir.the_moment.carmichael_sms.utility;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ir.the_moment.carmichael_sms.Message;

/**
 * Created by vaas on 3/26/2017.
 * parses the message received by the smsReceiver back to a Message object
 *
 */

public class MessageParser extends AsyncTask<Void,Void,Void> {
    private String message;
    private Message.Type type;
    private ArrayList<Message> messages = new ArrayList<>();
    private OnMessageParseFinished onMessageParseFinished = null;

    public static final String COMMAND = "0";
    public static final String RESPONSE = "1";
    public static final String REQUEST = "2";

    public void setOnMessageParseFinished(OnMessageParseFinished onMessageParseFinished) {
        this.onMessageParseFinished = onMessageParseFinished;
    }

    public MessageParser(String message) {
        this.message = message;
    }

    private void parse(){
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(message);

            JSONArray jsonMainNode = jsonObject.optJSONArray(COMMAND);
            if (jsonMainNode != null){
                type = Message.Type.command;
            }else if ((jsonMainNode = jsonObject.optJSONArray(RESPONSE)) != null){
                type = Message.Type.response;;
            }else {
                jsonMainNode = jsonObject.optJSONArray(REQUEST);
                type = Message.Type.request;;
            }

            int length = jsonMainNode.length();

            for(int i=0; i < length; i++)
            {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                Message message = new Message();
                String action = jsonChildNode.optString(MessageCreator.ACTION);
                String sender = jsonChildNode.optString(MessageCreator.SENDER);
                String requestResponse = jsonChildNode.optString(MessageCreator.REQUEST_RESPOND);
                message.type = type;
                message.requestRespond = requestResponse.equals("1");
                message.sender = sender;
                JSONArray flags   = jsonChildNode.optJSONArray(MessageCreator.FLAGS);
                JSONObject data = jsonChildNode.optJSONObject(MessageCreator.DATA);

                message.action = action;

                if (flags != null){
                    message.flags = getArray(flags);
                }


                if (data != null){
                    message.setData(getData(data));
                }
                messages.add(message);
            }
        } catch (JSONException e) {

            e.printStackTrace();
        }
    }

    private Map<String, String> getData(JSONObject data) {
        Map<String,String> dataMap = new HashMap<>();
        Iterator<String> keys = data.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                dataMap.put(key, data.getString(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return dataMap;
    }

    private String[] getArray(JSONArray array) {
        int length = array.length();
        String[] newData = new String[length];

        for (int i = 0; i < length; i++) {
            try {
                newData[i] = array.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return newData;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        parse();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (onMessageParseFinished != null){
            onMessageParseFinished.onParseFinished(messages,type);
        }
    }
}
