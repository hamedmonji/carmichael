package ir.the_moment.carmichael_sms;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by vaas on 7/9/17.
 */

public class HandlerModel{
    public String name;
    public String email;
    public String profile_image;
    public String number;
    public boolean is_authorized = false;

    public HandlerModel(@NonNull String name, @NonNull String user_email,
                        @Nullable String number , String profile_image) {
        this.name = name;
        this.email = user_email;
        this.number = number;
        this.profile_image = profile_image;
    }

    public HandlerModel(){
    }
}