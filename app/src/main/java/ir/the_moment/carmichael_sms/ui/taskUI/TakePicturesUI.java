package ir.the_moment.carmichael_sms.ui.taskUI;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import ir.the_moment.carmichael_sms.tasks.takePictures.TakePictures;
import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.ui.taskUI.base.TaskUI;

/**
 * Created by mac on 6/14/17.
 */

public class TakePicturesUI extends TaskUI {
    @Override
    protected View constructView( ) {
        View view  = LayoutInflater.from(getContext()).inflate(R.layout.take_pictures_options,null);

        final Spinner snapFrequency = view.findViewById(R.id.take_pic_interval);
        snapFrequency.setAdapter(getAdapter(R.array.take_pictures_interval));

        final Spinner interval = view.findViewById(R.id.upload_pic_interval);
        interval.setAdapter(getAdapter(R.array.upload_pictures_interval));

        CheckBox disable = view.findViewById(R.id.disable);
        disable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                snapFrequency.setEnabled(!isChecked);
                interval.setEnabled(!isChecked);
            }
        });

        return view;
    }

    @Override
    protected Message constructMessage(View view) {
        Message message = new Message();
        message.action = TakePictures.action;
        message.requestRespond = true;

        CheckBox disable = view.findViewById(R.id.disable);
        if (disable.isChecked()) {
            message.addFlag(TakePictures.FLAG_DISABLE);
            return message;
        }else {

            Spinner takeInterval = view.findViewById(R.id.take_pic_interval);
            Spinner uploadInterval = view.findViewById(R.id.upload_pic_interval);


            if (takeInterval.getSelectedItemPosition() != 0) {
                message.addFlag(TakePictures.FLAG_TAKE_PIC_PERIODICALLY);
                message.putInt(TakePictures.DATA_KEY_TAKE_PIC_INTERVAL, takeInterval.getSelectedItemPosition());
            }

            message.addFlag(TakePictures.FLAG_AUTO_UPLOAD_PICTURES);
            message.putInt(TakePictures.DATA_KEY_UPLOAD_INTERVAL, uploadInterval.getSelectedItemPosition());

            return message;
        }
    }

    private ArrayAdapter<String> getAdapter(int id) {
        return new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item,
                getContext().getResources().getStringArray(id));
    }
}
