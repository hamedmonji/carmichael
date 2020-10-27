package ir.the_moment.carmichael_sms.utility;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;

/**
 * Created by vaas on 4/30/17.
 * gets user's dropbox account.
 */

public class GetDropBoxUserAccount  extends AsyncTask<Void,Void,FullAccount>{
    private DbxClientV2 dbxClient;
    private OnAccountReady OnAccountReady;
    private Exception error;

    public interface OnAccountReady {
        void onAccountReceived(FullAccount account);
        void onError(Exception error);
    }

    public GetDropBoxUserAccount(DbxClientV2 dbxClient, OnAccountReady delegate){
        this.dbxClient =dbxClient;
        this.OnAccountReady = delegate;
    }

    @Override
    protected FullAccount doInBackground(Void... params) {
        try {
            //get the users FullAccount
            return dbxClient.users().getCurrentAccount();
        } catch (DbxException e) {
            e.printStackTrace();
            error = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(FullAccount account) {
        super.onPostExecute(account);

        if (account != null && error == null){
            //User Account received successfully
            OnAccountReady.onAccountReceived(account);
        }
        else {
            // Something went wrong
            OnAccountReady.onError(error);
        }
    }
}
