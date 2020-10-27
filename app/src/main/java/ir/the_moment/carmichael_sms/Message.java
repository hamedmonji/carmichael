package ir.the_moment.carmichael_sms;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

import ir.the_moment.carmichael_sms.messageHandler.MessageHandler;
import ir.the_moment.carmichael_sms.utility.MessageCreator;

/**
 * Created by vaas on 3/25/2017.
 * a message holds a task with a set of flags.
 * an instance of this class will be used to trigger
 * a task or respond by sending the json representation of this class to
 * the client device.
 */

public class Message implements Parcelable {

    /**
     * holds the password for the asset this message is sent to
     */
    public static String DATA_KEY_PASSWORD = "psw";
    /**
     * each task has an action which is a string containing the name of the task class.
     * this field is used to create the task using the {@link ir.the_moment.carmichael_sms.tasks.TaskFactory} class
     */
    public String action;

    /**
     * the sender of the message
     */
    public String sender;


    /**
     * whether to request a respond after sending the message
     */
    public boolean requestRespond = false;

    /**
     * set of flags to change the default behavior of the action.
     */
    public String[] flags;

    /**
     *     specifying the type of this message the value can be on of two
     *     {@link MessageCreator#MESSAGE_TYPE_COMMAND,MessageCreator#MESSAGE_TYPE_RESPONSE}
     */
    public Type type;

    /**
     * map for storing extras
     */
    private Map<String, String> data = new HashMap<>();

    public Map<String, String> getData() {
        return data;
    }

    /**
     * adds a new key/value pair to data map
     * @param key key of the extra
     * @param value value of the extra
     */
    public void putExtra(String key, String value){
        data.put(key,value);
    }

    /**
     adds a new key/value  pair of type double to data map
     * @param key key of the extra
     * @param value value of the extra
     */
    public void putDouble(String key,double value){
        data.put(key,String.valueOf(value));
    }

    /**
     adds a new key/value  pair of type int to data map
     * @param key key of the extra
     * @param value value of the extra
     */
    public void putInt(String key,int value){
        data.put(key, String.valueOf(value));
    }

    /**
     * @param key key for requested value.
     * @return value corresponding to the requested key if exists, -1 otherwise.
     */
    public int getInt(String key) {
        String i;
        if ((i = getExtra(key) )!= null) {
            return Integer.parseInt(i);
        }
        return -1;
    }

    /**
     * @param key key for requested value.
     * @return value of type double corresponding to the requested key if exists, -1 otherwise.
     */
    public double getDouble(String key) {
        String i;
        if ((i = getExtra(key) )!= null) {
            return Double.parseDouble(i);
        }
        return -1;
    }

    /**
     * @param key key for requested value.
     * @return value of type float corresponding to the requested key if exists, -1 otherwise.
     */
    public float getFloat(String key) {
        String i;
        if ((i = getExtra(key) )!= null) {
            return Float.parseFloat(i);
        }
        return -1;
    }

    /**
     * adds a new flags to the message
     * @param flag flag to be added the message
     */
    public void addFlag(String flag){

        if (flags == null){
            flags = new String[1];
            flags[0] = flag;
            return;
        }

        if (!flag.equals("")){
            int length = flags.length;
            String[] newFlags = new String[length+ 1];
            System.arraycopy(flags,0,newFlags,0,length);
            newFlags[length] = flag;
            this.flags = newFlags;
        }
    }

    /**
     *
     * @param key key for requested value.
     * @return value corresponding to the requested key if exists, null otherwise.
     */
    public String getExtra(String key){
        if (data != null && data.containsKey(key)){
            return data.get(key);
        }
        return null;
    }

    /**
     *
     * @param key key for requested value.
     * @return value corresponding to the requested key if exists, false otherwise.
     */
    public boolean getBoolean(String key) {
        return getData() != null && getData().containsKey(key) && Boolean.parseBoolean(getData().get(key));
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    /**
     * check to see if a flag has been set
     * @param flag to check its existence.
     * @return true if the flag exists in {@link #flags} false otherwise.
     */
    public boolean hasFlag(String flag){
        if (flags == null )
            return false;
        for (String f :
                flags) {
            if (f.equals(flag))
                return true;
        }
        return false;
    }


    public boolean succeed() {
        return getBoolean(MessageHandler.KEY_SUCCESS);
    }
    /**
     *
     * @param obj object to compare with
     * @return true if the action for both of the objects are the same.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Message){
            Message message = (Message) obj;
            if (message.action.equals(action)){
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return this message in json format
     */
    @Override
    public String toString() {
        MessageCreator creator = new MessageCreator(this);
        if (type == null){
            type = Type.command;
        }
        return creator.getInJsonFormat();
    }

    public enum Type{
        command,
        response,
        request
    }


    public Message() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.action);
        dest.writeString(this.sender);
        dest.writeByte(this.requestRespond ? (byte) 1 : (byte) 0);
        dest.writeStringArray(this.flags);
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
        dest.writeInt(this.data.size());
        for (Map.Entry<String, String> entry : this.data.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
    }

    protected Message(Parcel in) {
        this.action = in.readString();
        this.sender = in.readString();
        this.requestRespond = in.readByte() != 0;
        this.flags = in.createStringArray();
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : Type.values()[tmpType];
        int dataSize = in.readInt();
        this.data = new HashMap<String, String>(dataSize);
        for (int i = 0; i < dataSize; i++) {
            String key = in.readString();
            String value = in.readString();
            this.data.put(key, value);
        }
    }

    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
}
