package ir.the_moment.carmichael_sms.fileManager.base;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.fileManager.base.callbacks.FileManagerAdapterCallbacks;

/**
 * Created by vaas on 4/24/17.
 * base recycler adapter class for file manager.
 */

public abstract class FileManagerAdapter<VH extends FileManagerAdapter.ViewHolder> extends RecyclerView.Adapter<VH>
        implements FileManagerAdapterCallbacks {
    public static final String TAG = "filePreset";
    protected List<File> currentTaskFileList = new ArrayList<>();
    protected ArrayList<File> files = new ArrayList<>();
    protected ArrayList<File> folders = new ArrayList<>();
    public final void setCurrentTaskFileList(List<File> currentTaskFileList) {
        this.currentTaskFileList = currentTaskFileList;
    }

    public FileManagerAdapter(List<File> currentTaskFileList) {
        this.currentTaskFileList = currentTaskFileList;
    }

    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(VH holder, int position) {
        if (position < folders.size()){
            File folder = folders.get(position);
            holder.file.setText(folder.getName());
            holder.icon.setImageResource(R.drawable.folder);
        }else {
            position -= folders.size();
            File file = files.get(position);
            holder.file.setText(file.getName());
            holder.icon.setImageResource(R.drawable.file);
        }
    }

    @Override
    public final int getItemCount() {
        return files.size() + folders.size();
    }

    protected File getFileAtPosition(int position) {
        if (position < folders.size()) {
            return folders.get(position);
        }else {
            return files.get(position - folders.size());
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener{
        TextView file;
        ImageView icon;
        public ViewHolder(View itemView) {
            super(itemView);
            file = (TextView) itemView.findViewById(R.id.file);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            file.setOnClickListener(this);
            icon.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (getAdapterPosition() >= folders.size()) {
                Toast.makeText(view.getContext(), R.string.item_is_a_file, Toast.LENGTH_SHORT).show();
                return;
            }
            onFileClicked(getFileAtPosition(getAdapterPosition()));
        }
    }
}