package ir.the_moment.carmichael_sms;

/**
 * Created by vaas on 7/9/17.
 */

public class User {
    public String name;
    public String email;
    public String profile_image;
    public Tasks Tasks = new Tasks();

    public User(String name, String email, String profile_image) {
        this.name = name;
        this.email = email;
        this.profile_image = profile_image;
    }

    public User(){
    }

    public class Tasks{
        public String placeHolder = "placeHolder";
    }
}
