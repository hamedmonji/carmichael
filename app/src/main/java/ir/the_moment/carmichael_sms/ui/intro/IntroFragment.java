package ir.the_moment.carmichael_sms.ui.intro;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ir.the_moment.carmichael_sms.R;

/**
 * Created by vaas on 8/9/17.
 */

public class IntroFragment extends Fragment {
    private static final String KEY_POSITION = "position";
    int position;
    public static IntroFragment newInstance(int position) {

        Bundle args = new Bundle();
        args.putInt(KEY_POSITION,position);
        IntroFragment fragment = new IntroFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(KEY_POSITION);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_intro,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        ImageView image = view.findViewById(R.id.image);
        TextView title = view.findViewById(R.id.title);
        TextView description = view.findViewById(R.id.description);

        title.setText(getTitle());
        image.setImageResource(getImage());
        description.setText(getDescription());

    }

    private int getTitle() {
        switch (position) {
            case 0:
                return R.string.app_name;
            case 1://full featured
                return R.string.full_featured;
            case 2://secure
                return R.string.secure;
            default:
                return R.string.app_name;
        }
    }

    private int getDescription() {
        switch (position) {
            case 0:
                return R.string.app_description;
            case 1://full featured
                return R.string.description_full_featured;
            case 2://secure
                return R.string.description_secure;
            default:
                return 0;
        }
    }

    private int getImage() {
        switch (position) {
            case 0:
                return R.drawable.app_logo;
            case 1://full featured
                return R.drawable.feature;
            case 2://secure
                return R.drawable.secure;
            default:
                return R.drawable.app_logo;
        }
    }
}
