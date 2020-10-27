package ir.the_moment.carmichael_sms.fileManager.base.callbacks;

import java.io.File;

/**
 * Created by vaas on 8/16/17.
 */

public interface FileManagerAdapterCallbacks {
    void onFileClicked(File clickedFile);

    /**
     * called when the user clicks on the back button
     * @return true if we are at the root and no parent file exists.
     */
    boolean isAtRoot();
    void onBackPressed();
}
