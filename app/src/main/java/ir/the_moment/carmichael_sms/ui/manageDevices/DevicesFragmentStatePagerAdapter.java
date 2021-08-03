package ir.the_moment.carmichael_sms.ui.manageDevices;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.util.SparseArray;

import ir.the_moment.carmichael_sms.R;

/**
 * Created by vaas on 4/17/17.
 */

public class DevicesFragmentStatePagerAdapter extends FragmentStatePagerAdapter {
    private SparseArray<DevicesFragment> fragments = new SparseArray<>();
    private Context context;
    public DevicesFragment getFragmentAt(int position){
        return fragments.get(position);
    }

    public DevicesFragmentStatePagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        DevicesFragment fragment = DevicesFragment.newInstance(position);
        fragments.append(position,fragment);
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return context.getString(R.string.handlers);
            case 1:
                return context.getString(R.string.assets);
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
