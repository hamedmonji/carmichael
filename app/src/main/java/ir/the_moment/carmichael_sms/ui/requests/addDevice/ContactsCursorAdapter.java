package ir.the_moment.carmichael_sms.ui.requests.addDevice;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.utility.Utility;

/**
 * Created by vaas on 5/8/17.
 * adapter for the list of contacts in the device.
 */

public class ContactsCursorAdapter extends CursorAdapter{
    private SparseArray<Character> headers = new SparseArray<>();
    private int CONTACT_ID_INDEX;
    public ContactsCursorAdapter(Context context, Cursor c) {
        super(context, c, true);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        CONTACT_ID_INDEX = cursor.getColumnIndex(ContactsContract.Contacts._ID);
        return LayoutInflater.from(context).inflate(R.layout.item_contact,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int position = cursor.getPosition();
        String contactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

        char firstChar = contactName.charAt(0);
        if (!containsHeader(firstChar)) {
            headers.append(position,firstChar);
            view.findViewById(R.id.contact_sort_header_root).setVisibility(View.VISIBLE);
            TextView letter = view.findViewById(R.id.letter);
            letter.setText(String.valueOf(firstChar));
        }else if (headers.get(position) != null){
            view.findViewById(R.id.contact_sort_header_root).setVisibility(View.VISIBLE);
            TextView letter = view.findViewById(R.id.letter);
            letter.setText(String.valueOf(firstChar));
        }else {
            view.findViewById(R.id.contact_sort_header_root).setVisibility(View.GONE);
        }

        String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
        TextView name  = view.findViewById(R.id.contact_name);

        String photoUri = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
        CircleImageView imageView = view.findViewById(R.id.image);

        if (photoUri == null){
            imageView.setImageDrawable(null);
            Utility.setBackgroundDrawable(context,imageView, (int) cursor.getLong(CONTACT_ID_INDEX));
            TextView imageText = view.findViewById(R.id.image_text);
            imageText.setVisibility(View.VISIBLE);
            imageText.setText(String.valueOf(firstChar).toUpperCase());
        }else {
            imageView.setImageURI(Uri.parse(photoUri));
            TextView imageText = view.findViewById(R.id.image_text);
            imageText.setVisibility(View.GONE);
        }

        TextView number = view.findViewById(R.id.contact_number);
        if (phoneNumber != null){
            number.setText(Utility.getFormattedNumber(phoneNumber));
        }
        name.setText(contactName);
    }

    private boolean containsHeader(char firstChar) {
        for (int i = 0; i < headers.size(); i++) {
            if (headers.valueAt(i).equals(firstChar))
                return true;
        }
        return false;
    }
}