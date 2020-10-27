package ir.the_moment.carmichael_sms.ui.securityReport;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ir.the_moment.carmichael_sms.TaskExecutorService;
import ir.the_moment.carmichael_sms.database.DeviceInfoDbHelper;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.tasks.Task;
import ir.the_moment.carmichael_sms.tasks.TaskFactory;
import de.hdodenhof.circleimageview.CircleImageView;
import ir.the_moment.carmichael_sms.DeviceModel;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.SecurityAlert;
import ir.the_moment.carmichael_sms.tasks.UserActivatedTask;
import ir.the_moment.carmichael_sms.utility.Utility;

/**
 * Created by vaas on 5/5/17.
 * adapter to show all security alerts in a recycler view.
 */
class SecurityAlertsAdapter extends RecyclerView.Adapter<SecurityAlertsAdapter.ViewHolder> {

    private ArrayList<SecurityAlert> lowPriorityAlerts = new ArrayList<>();
    private ArrayList<SecurityAlert> mediumPriorityAlerts = new ArrayList<>();
    private ArrayList<SecurityAlert> highPriorityAlerts = new ArrayList<>();
    private Context context;
    SecurityAlertsAdapter(ArrayList<SecurityAlert> lowAlerts, ArrayList<SecurityAlert> mediumAlerts,
                          ArrayList<SecurityAlert> highPriorityAlerts, Context context) {
        this.lowPriorityAlerts = lowAlerts;
        this.mediumPriorityAlerts = mediumAlerts;
        this.highPriorityAlerts = highPriorityAlerts;
        this.context = context;
    }

    @Override
    public SecurityAlertsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_security_alert,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SecurityAlertsAdapter.ViewHolder holder, int position) {
        SecurityAlert alert = getAlertForPosition(position);
        DeviceModel device = DeviceInfoDbHelper.getDeviceByNumber(context,alert.number, mR.TYPE_HANDLER);
        if (device != null) {
            holder.number.setText(device.number);
            holder.name.setText(device.name);
            if (device.pic == null) {
                Utility.setBackgroundDrawable(context,holder.image,position);
                holder.imageText.setVisibility(View.VISIBLE);
                holder.imageText.setText(device.name.substring(0,1).toUpperCase());
            }else {
                holder.imageText.setVisibility(View.VISIBLE);
                Glide.with(context).load(device.pic).into(holder.image);
            }

//            holder.rank.setImageResource((device.rank));
        }else {
            holder.name.setText(R.string.device_was_removed);
            holder.number.setText(R.string.device_was_removed);
            Utility.setBackgroundDrawable(context,holder.image,position);

        }

        holder.name.setTextColor(Utility.getCorrectColor(context, R.color.black));
        holder.number.setTextColor(Utility.getCorrectColor(context, R.color.black));
        UserActivatedTask task = TaskFactory.createTask(TaskExecutorService.tasksPackage + alert.task);
        task.setContext(context);
        if (task.getPriority() == Task.priority.low) {
            holder.rank.setImageDrawable(getCorrectDrawable(R.drawable.green_cliantro));
        }else if (task.getPriority() == Task.priority.medium) {
            holder.rank.setImageDrawable(getCorrectDrawable(R.drawable.yellow_citrus));
        }else {
            holder.rank.setImageDrawable(getCorrectDrawable(R.drawable.red_cherry));
        }

        CardView root = holder.itemView.findViewById(R.id.security_report_root);
//        setPriority(root, task);

        holder.task.setText(task.getTaskName());
        holder.time.setText(getTime(alert.time));
    }

    private Drawable getCorrectDrawable(int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(id);
        }else return context.getResources().getDrawable(id);
    }



    private void setPriority(CardView root, UserActivatedTask task) {
        if (task.getPriority() == Task.priority.low) {
            root.setCardBackgroundColor(Utility.getCorrectColor(context, R.color.green));
        }else if (task.getPriority() == Task.priority.medium) {
            root.setCardBackgroundColor(Utility.getCorrectColor(context,R.color.yellow_citrus));
        }else {
            root.setCardBackgroundColor(Utility.getCorrectColor(context,R.color.red));
        }
    }

    private int getRankImage(int rank) {
        if (rank >= 1 && rank < 4) {
            return R.drawable.military_1;
        } else if (rank >= 4 && rank < 7) {
            return R.drawable.military_2;
        } else if (rank >= 7 && rank < 10) {
            return R.drawable.military_3;
        } else if (rank >= 10) {
            return R.drawable.military_4;
        }
        return 0;
    }

    private String getTime(String timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timeInMillis));
        String time = calendar.getTime().toString();
        return time.substring(0,20).trim();
    }

    @Override
    public int getItemCount() {
        return lowPriorityAlerts.size() + mediumPriorityAlerts.size() + highPriorityAlerts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        TextView number;
        TextView task;
        TextView time;
        TextView imageText;
        CircleImageView image;
        CircleImageView rank;
        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.device_image);
            name = itemView.findViewById(R.id.device_name);
            number = itemView.findViewById(R.id.device_number);
            rank = itemView.findViewById(R.id.handler_rank);
            task = itemView.findViewById(R.id.task);
            time = itemView.findViewById(R.id.time);
            imageText = itemView.findViewById(R.id.device_image_text);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(final View v) {
            final int position = getAdapterPosition();
            final SecurityAlert selectedAlert = getAlertForPosition(position);
            final CharSequence[] items = {context.getString(R.string.retire_handler),
                    context.getString(R.string.delete_alert),
                    context.getString(R.string.promote_handler)};

            new AlertDialog.Builder(v.getContext())
                    .setTitle(R.string.manage_handler)
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            switch (which) {
                                case 0 :
                                    ProgressDialog progress = showWaitDialog(v);
                                    final DeviceInfoDbHelper dbHelper = new DeviceInfoDbHelper(v.getContext());
                                    dbHelper.deleteDeviceByNumber(context,selectedAlert.number,mR.TYPE_HANDLER);
                                    removeAlert(position);
                                    progress.dismiss();
                                    break;
                                case 1:
                                    showDeleteAlertDialog(v);
                                    break;
                                case 2:
                                    ProgressDialog waitDialog = showWaitDialog(v);
                                    waitDialog.show();
                                    DeviceModel device =
                                            DeviceInfoDbHelper.getDeviceByNumber(v.getContext(),selectedAlert.number,mR.TYPE_HANDLER);
                                    Task task = TaskFactory.createTask(TaskExecutorService.tasksPackage + selectedAlert.task);
                                    task.setContext(context);
                                    if (!task.isEnabled()) {
                                        Toast.makeText(v.getContext(), R.string.activate_task_in_settings, Toast.LENGTH_SHORT).show();
                                        waitDialog.dismiss();
                                        return;
                                    }
                                    if (device != null) {
                                        if (!mR.Permissions.hasPermission(device.permissions,task.getPermission())) {
                                            device.permissions = mR.Permissions.togglePermission(device.permissions, task.getPermission());
                                            device.rank = mR.Permissions.getAvailableTasksForDevice(device,v.getContext()).length;
                                            DeviceInfoDbHelper deviceDbHelper = new DeviceInfoDbHelper(context);
                                            deviceDbHelper.updateDeviceByNumber(v.getContext(),device, device.number, mR.TYPE_HANDLER);
                                        }
                                    }else {
                                        Toast.makeText(context, R.string.device_was_removed, Toast.LENGTH_SHORT).show();
                                    }
                                    removeAlert(position);
                                    waitDialog.dismiss();
                                    break;
                            }
                        }
                    }).show();
        }

        private void showDeleteAlertDialog(final View v) {
            new AlertDialog.Builder(v.getContext())
                    .setTitle(R.string.delete_alert)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ProgressDialog progress = showWaitDialog(v);
                            removeAlert(getAdapterPosition());
                            progress.dismiss();
                        }
                    }).setNegativeButton(R.string.cancel, null).show();
        }

        @NonNull
        private ProgressDialog showWaitDialog(View v) {
            ProgressDialog progress = new ProgressDialog(v.getContext());
            progress.setIndeterminate(true);
            progress.setMessage(context.getString(R.string.please_wait));
            progress.setCancelable(false);
            progress.show();
            return progress;
        }

        private void removeAlert(int position) {
            if (position < highPriorityAlerts.size()) {
                highPriorityAlerts.remove(position);
                saveAlerts(highPriorityAlerts,mR.KEY_PREF_SECURITY_ALERTS_HIGH);
            }else if ( position < mediumPriorityAlerts.size()) {
                mediumPriorityAlerts.remove(position - lowPriorityAlerts.size());
                saveAlerts(mediumPriorityAlerts,mR.KEY_PREF_SECURITY_ALERTS_MEDIUM);
            }else {
                lowPriorityAlerts.remove(position - mediumPriorityAlerts.size() - highPriorityAlerts.size() );
                saveAlerts(lowPriorityAlerts,mR.KEY_PREF_SECURITY_ALERTS_LOW);
            }
            notifyItemRemoved(position);
        }

        private void saveAlerts(List<SecurityAlert> alerts,String prefKey) {
            SharedPreferences prefs =
                    PreferenceManager.getDefaultSharedPreferences(context);
            if (alerts.size() == 0) {
                prefs.edit().putString(prefKey,null).commit();
            }else {
                StringBuilder rawAlerts = new StringBuilder();
                for (int i = 0; i < alerts.size(); i++) {
                    rawAlerts.append(alerts.get(i).toString());
                    if (i != alerts.size() -1)
                        rawAlerts.append(mR.DATA_SEPARATOR);
                }
                prefs.edit().putString(prefKey,rawAlerts.toString()).commit();
            }
        }

    }

    void clearAll() {
        if (getItemCount() == 0) {
            Toast.makeText(context, R.string.no_security_report_to_delete, Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(context)
                .setTitle(R.string.delete_all_alerts)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences prefs =
                                PreferenceManager.getDefaultSharedPreferences(context);
                        prefs.edit().putString(mR.KEY_PREF_SECURITY_ALERTS_LOW,null).commit();
                        prefs.edit().putString(mR.KEY_PREF_SECURITY_ALERTS_MEDIUM,null).commit();
                        prefs.edit().putString(mR.KEY_PREF_SECURITY_ALERTS_HIGH,null).commit();
                        lowPriorityAlerts.clear();
                        mediumPriorityAlerts.clear();
                        highPriorityAlerts.clear();
                        notifyDataSetChanged();
                    }
                }).setNegativeButton(R.string.cancel, null).show();
    }

    private SecurityAlert getAlertForPosition(int position) {
        if (position < highPriorityAlerts.size()) {
            return highPriorityAlerts.get(position);
        }else if ( position < mediumPriorityAlerts.size() + highPriorityAlerts.size()) {
            return mediumPriorityAlerts.get(position - highPriorityAlerts.size());
        }else {
            return lowPriorityAlerts.get(Math.abs(position - mediumPriorityAlerts.size() - highPriorityAlerts.size()));
        }
    }
}
