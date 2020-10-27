package ir.the_moment.carmichael_sms.tasks.requests.addRequest;

/**
 * Created by vaas on 5/12/17.
 * holds all the keys for a add request
 */

public final class AddRequest {

    public static final String DATA_KEY_USER_ID = "ui";

    private AddRequest(){}

    public static final String action = "AddRequest";
    public static final String DATA_KEY_MESSAGE = "m";
    public static final String DATA_KEY_ACCEPTED = "ac";
    public static final String DATA_KEY_USERNAME = "u";
}
