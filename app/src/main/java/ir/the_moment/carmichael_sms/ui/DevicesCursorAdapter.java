package ir.the_moment.carmichael_sms.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;
import ir.the_moment.carmichael_sms.DeviceModel;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.database.DeviceInfoContract;
import ir.the_moment.carmichael_sms.ui.manageDevices.DevicesFragment;
import ir.the_moment.carmichael_sms.utility.Security;
import ir.the_moment.carmichael_sms.utility.Utility;

import static ir.the_moment.carmichael_sms.utility.Security.getDecryptedPreferenceEntry;

/**
 * Created by vaas on 4/9/2017.
 * cursor adapter for list view in devices fragment
 */

public class DevicesCursorAdapter extends CursorAdapter implements View.OnClickListener {
    private DevicesFragment devicesFragment;
    private String decryptionPassword;
    private int totalColors = 9;
    private int idColumnIndex;


    public DevicesCursorAdapter(Context context, Cursor c,DevicesFragment devicesFragment) {
        super(context, c, true);
        this.devicesFragment = devicesFragment;
        idColumnIndex = c.getColumnIndexOrThrow(DeviceInfoContract.DeviceInfo._ID);
        decryptionPassword = getDecryptedPreferenceEntry(context, context.getString(R.string.key_pref_user_password_encrypted));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view =  LayoutInflater.from(context).inflate(R.layout.item_devices_list, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        DeviceModel device = Security.getDecryptedDeviceModel(cursor,decryptionPassword);
        if (device != null) {
            LinearLayout root = view.findViewById(R.id.device_root);

            final TextView name = view.findViewById(R.id.device_name);
            final TextView number = view.findViewById(R.id.device_number);
            CircleImageView rankImage = view.findViewById(R.id.handler_rank);
            CircleImageView userPhoto = view.findViewById(R.id.device_image);
            TextView userImageText = view.findViewById(R.id.device_image_text);
            if (device.pic != null){
                userPhoto.setImageURI(Uri.parse(device.pic));
                userImageText.setVisibility(View.GONE);
            }else {
                Utility.setBackgroundDrawable(context,userPhoto, (int) cursor.getLong(idColumnIndex));
                userImageText.setText(device.name.substring(0,1).toUpperCase());
                userImageText.setVisibility(View.VISIBLE);
            }

            name.setText(device.name);
            number.setText(Utility.getFormattedNumber(device.number));
            if (device.status == DeviceModel.STATUS_OK) {
                root.setBackgroundColor(Utility.getCorrectColor(context,R.color.tw__solid_white));
                name.setTextColor(Utility.getCorrectColor(context,R.color.dark_grey_color));
                number.setTextColor(Utility.getCorrectColor(context,R.color.dark_gray));
            } else if (device.status == DeviceModel.STATUS_LOST) {
                root.setBackgroundColor(Utility.getCorrectColor(context,R.color.dark_grey_color));
                name.setTextColor(Utility.getCorrectColor(context,R.color.blue_aqua));
                number.setTextColor(Utility.getCorrectColor(context,R.color.blue_electric));
            }

            if (device.rank == 0) {
                rankImage.setImageDrawable(null);
            }else if (device.rank >= 1 && device.rank < 4) {
                rankImage.setImageResource(R.drawable.military_1);
            } else if (device.rank >= 4 && device.rank < 7) {
                rankImage.setImageResource(R.drawable.military_2);
            } else if (device.rank >= 7 && device.rank < 10) {
                rankImage.setImageResource(R.drawable.military_3);
            } else if (device.rank >= 10) {
                rankImage.setImageResource(R.drawable.military_4);
            }
            userPhoto.setTag(getCursor().getPosition());
            userPhoto.setOnClickListener(this);
        }
    }

    private int getBackgroundImage(int pos){
        pos -= (int)Math.floor(pos/totalColors) * totalColors;
        switch (pos){
            case 0:
                return R.drawable.blue_aqua;
            case 1:
                return R.drawable.blue_electric;
            case 2:
                return R.drawable.deep_aqua;
            case 3:
                return R.drawable.salt_water;
            case 4:
                return R.drawable.green_cliantro;
            case 5:
                return R.drawable.gray_pewter;
            case 6:
                return R.drawable.yellow_citrus;
            case 7:
                return R.drawable.yellow_lemon;
            case 8:
                return R.drawable.red_cherry;
            default:
                return R.drawable.blue_electric;
        }
    }

    @Override
    public void onClick(View view) {
        int position = (int) view.getTag();
        getCursor().moveToPosition(position);
        DeviceModel deviceModel = Security.getDecryptedDeviceModel(getCursor(),decryptionPassword);
        devicesFragment.changeDeviceImage(deviceModel);
    }
}