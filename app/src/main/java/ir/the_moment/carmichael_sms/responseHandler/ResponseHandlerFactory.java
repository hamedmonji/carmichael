package ir.the_moment.carmichael_sms.responseHandler;

import android.util.Log;

/**
 * Created by vaas on 6/30/17.
 */

public class ResponseHandlerFactory {

    private ResponseHandlerFactory(){
    }

    public static ResponseHandler createRespond(String respondAction){
        try {
            return  (ResponseHandler) Class.forName(respondAction).newInstance();
        } catch (Exception e){
            Log.i("createResponse","exception response factory",e);
            return new NullResponseHandler();
        }
    }
}
