package ir.the_moment.carmichael_sms.ui.intro;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.utility.Utility;

public class IntroActivity extends AppCompatActivity {

    private FrameLayout root;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        final String skipText = getString(R.string.skip);
        final String start = getString(R.string.start);
        root = findViewById(R.id.intro_root);

        final Button skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        ViewPager introPager = findViewById(R.id.intro_pager);
        introPager.setAdapter(new IntroFragmentPagerAdapter(getSupportFragmentManager()));

        final LinearLayout indicatorContainer = findViewById(R.id.current_fragment_indicator_container);

        introPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }


            @Override
            public void onPageSelected(int position) {
                if (position == 2) {
                    skip.setText(start);
                }else {
                    skip.setText(skipText);
                }
                setCurrentFragmentColor(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

            private void setCurrentFragmentColor(int position) {
                root.setBackgroundColor(getColor(position));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    if (position == 0) {
                        indicatorContainer.getChildAt(0).setBackground(Utility.getBackgroundDrawable(IntroActivity.this,R.drawable.red));
                        indicatorContainer.getChildAt(1).setBackground(Utility.getBackgroundDrawable(IntroActivity.this,R.drawable.white));
                        indicatorContainer.getChildAt(2).setBackground(Utility.getBackgroundDrawable(IntroActivity.this,R.drawable.white));
                        return;
                    }
                    if (position != 2) {
                        indicatorContainer.getChildAt(position+1).setBackground(Utility.getBackgroundDrawable(IntroActivity.this,R.drawable.white));
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        indicatorContainer.getChildAt(position-1).setBackground(Utility.getBackgroundDrawable(IntroActivity.this,R.drawable.white));
                    }
                    indicatorContainer.getChildAt(position).setBackground(Utility.getBackgroundDrawable(IntroActivity.this,R.drawable.red));
                }else {
                    if (position == 0) {
                        indicatorContainer.getChildAt(0).setBackgroundDrawable(Utility.getBackgroundDrawable(IntroActivity.this,R.drawable.red));
                        indicatorContainer.getChildAt(1).setBackgroundDrawable(Utility.getBackgroundDrawable(IntroActivity.this,R.drawable.white));
                        indicatorContainer.getChildAt(2).setBackgroundDrawable(Utility.getBackgroundDrawable(IntroActivity.this,R.drawable.white));
                        return;
                    }
                    if (position != 2) {
                        indicatorContainer.getChildAt(position+1).setBackgroundDrawable(Utility.getBackgroundDrawable(IntroActivity.this,R.drawable.white));
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        indicatorContainer.getChildAt(position-1).setBackgroundDrawable(Utility.getBackgroundDrawable(IntroActivity.this,R.drawable.white));
                    }
                    indicatorContainer.getChildAt(position).setBackgroundDrawable(Utility.getBackgroundDrawable(IntroActivity.this,R.drawable.red));
                }

            }

            private int getColor(int position) {
                switch (position) {
                    case 0:
                        return getCorrectColor(R.color.colorPrimary);
                    case 1://full featured
                        return getCorrectColor(R.color.yellow_lemon);
                    case 2://secure
                        return getCorrectColor(R.color.green);

                    default:
                        return getCorrectColor(R.color.colorPrimary);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private int getCorrectColor(@ColorRes int id) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return getResources().getColor(id,null);

        }else {
            return getResources().getColor(id);
        }
    }
}