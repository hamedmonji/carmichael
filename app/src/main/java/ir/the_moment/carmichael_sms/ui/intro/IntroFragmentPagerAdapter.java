package ir.the_moment.carmichael_sms.ui.intro;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

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
