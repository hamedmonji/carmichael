package ir.the_moment.carmichael_sms.utility;

import java.util.List;

import ir.the_moment.carmichael_sms.Message;

/**
 * Created by vaas on 3/26/2017.
 * callback for when parsing a message ends.
 */

public interface OnMessageParseFinished {
    /**
     *
     * @param messages list of messages within the message that was parsed.
     * @param type type of the messages can be one of the two values of {@link Message.Type#command,Message.Type#response,Message.Type#request}
     *
     */
    void onParseFinished(List<Message> messages, Message.Type type);
}
