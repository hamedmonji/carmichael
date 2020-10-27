package ir.the_moment.carmichael_sms.ui.taskUI.base;

/**
 * Created by mac on 6/12/17.
 */

public class TaskUIFactory {
    private TaskUIFactory(){}

    public static TaskUI createTaskUI(String taskUI){
        try {
            return (TaskUI) Class.forName(taskUI).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return new NullTaskUI();
        }
    }

}
