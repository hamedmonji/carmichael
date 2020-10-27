package ir.the_moment.carmichael_sms.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ir.the_moment.carmichael_sms.Message;

/**
 * Created by vaas on 3/26/2017.
 * creates an string in the form of json from message objects
 */

public class MessageCreator {
    public static final String MESSAGE_TYPE_COMMAND = "\"0\":";
    public static final String MESSAGE_TYPE_RESPONSE = "\"1\":";
    public static final String MESSAGE_TYPE_REQUEST = "\"2\":";
    static final String ACTION = "2";
    static final String FLAGS = "3";
    static final String DATA = "4";
    static final String REQUEST_RESPOND = "5";
    static final String SENDER = "6";
    private List<Message> messages;


    public MessageCreator(List<Message> messages) {
        this.messages = new ArrayList<>();
        this.messages.addAll(messages);
    }
    public MessageCreator(Message message){
        this.messages = new ArrayList<>();
        messages.add(message);
    }

    /**
     *
     * @return returns a string in json format from the the commands list that was requested
     * an example would be like this
     * {
     *     commands[
     *          {
     *              "action": "secure_data",
     *              "flags": ["3000"],
     *              "request_response" : "true"
     *              "data" : {
     *                  "file_list": "profile_image.jpeg;,movie.mp4"
     *              }
     *          },
     *          {
     *              "action": "get_location",
     *              "flags": ["1000"],
     *              "data" :{
     *                  "interval": "60"
     *              }
     *          }
     *     ]
     * }
     */
    public String getInJsonFormat(){
        String typeString = getTypeAsString();
        if (messages != null && messages.size() > 0 && typeString != null ) {
            StringBuilder builder = new StringBuilder();
            builder.append(beginObject());
            builder.append(typeString);
            builder.append(beginArray());
            for (int i = 0; i < messages.size(); i++) {
                builder.append(writeInJsonFormat(messages.get(i)));
                if (i != messages.size() - 1) {
                    builder.append(",");
                }
            }
            builder.append(endArray());
            builder.append(endObject());
            return builder.toString();
        }
        return null;
    }

    private String writeInJsonFormat(Message message){
        String s = beginObject() +
                writeAction(ACTION, message.action) + writeSeparator();
        String requestResponse = message.requestRespond ? "1" : "0";
        s += writeAction(REQUEST_RESPOND, String.valueOf(requestResponse)) + writeSeparator();

//        s += writeAction(SENDER, message.sender) + writeSeparator();

        if (message.flags != null){
            s += writeArray(FLAGS, message.flags) + writeSeparator();
        }
        if (message.getData() != null){
            s += writeData(DATA, message.getData());
        }
        s += endObject();
        return s;
    }


    private String beginArray(){
        return "[";
    }
    private String endArray(){
        return "]";
    }
    private String beginObject(){
        return "{";
    }
    private String endObject(){
        return "}";
    }
    private String writeAction(String name,String action){
        //example "action": "get_location",
        return "\"" +
                name +
                "\":" +
                " " +
                "\"" +
                action +
                "\"";
    }
    private String writeSeparator(){
        return ",";
    }
    private String writeArray(String name, String[] data){
        StringBuilder builder = new StringBuilder();
        builder.append("\"");
        builder.append(name);
        builder.append("\":");
        builder.append(" ");
        if (data.length != 0) {
            builder.append("[");
            for (int i=0 ;i< data.length;i++) {
                builder.append("\"");
                builder.append(data[i]);
                builder.append("\"");
                if (i != (data.length-1)) {
                    builder.append(",");
                }
            }
            builder.append("]");
        }
        return builder.toString();
    }
    private String writeData(String name, Map<String,String> data){
        StringBuilder builder  = new StringBuilder();
        builder.append("\"");
        builder.append(name);
        builder.append("\":");
        builder.append(beginObject());
        int i =0;
        for (String key :
                data.keySet()) {
            builder.append(writeAction(key,data.get(key)));
            if (i != data.size()-1){
                builder.append(",");
            }
            i++;
        }
        builder.append(endObject());
        return builder.toString();
    }


    private String getTypeAsString(){
        if (messages != null && messages.size() > 0) {
            if (messages.get(0).type == null){
                throw new IllegalStateException("type not supported");
            }
            switch (messages.get(0).type) {
                case command:
                    return MESSAGE_TYPE_COMMAND;
                case response:
                    return MESSAGE_TYPE_RESPONSE;
                case request:
                    return MESSAGE_TYPE_REQUEST;
                default:
                    throw new IllegalStateException("type not supported");
            }
        }else return null;
    }
}
