package ir.the_moment.carmichael_sms.ui.intro;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by vaas on 8/9/17.
 */

public class IntroFragmentPagerAdapter extends FragmentPagerAdapter {

    public IntroFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return IntroFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return 3;
    }
}
