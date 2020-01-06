package com.hmdm.pager.task;

import android.content.Context;
import android.os.AsyncTask;

import com.hmdm.pager.Const;
import com.hmdm.pager.SettingsHelper;
import com.hmdm.pager.http.ServerService;
import com.hmdm.pager.http.ServerServiceKeeper;
import com.hmdm.pager.http.json.Message;
import com.hmdm.pager.http.json.ServerResponse;

import retrofit2.Response;

public class UpdateStatusTask extends AsyncTask<Message, Integer, Integer> {

    private Context context;

    public UpdateStatusTask( Context context ) {
        this.context = context;
    }

    @Override
    protected Integer doInBackground(Message... message) {
        return updateStatus(message[0], context);
    }

    public static Integer updateStatus(Message message, Context context) {
        SettingsHelper settingsHelper = SettingsHelper.getInstance(context);
        ServerService serverService = ServerServiceKeeper.getServerServiceInstance(context);
        ServerService secondaryServerService = ServerServiceKeeper.getSecondaryServerServiceInstance(context);
        Response<ServerResponse> response = null;

        try {
            response = serverService.updateMessageStatus(settingsHelper.getString(SettingsHelper.KEY_SERVER_PATH),
                    message.getServerId(), message.getStatus()).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (response == null) {
                response = secondaryServerService.updateMessageStatus(settingsHelper.getString(SettingsHelper.KEY_SERVER_PATH),
                        message.getServerId(), message.getStatus()).execute();
            }
            if ( response.isSuccessful() ) {
                return Const.TASK_SUCCESS;
            }
        }
        catch ( Exception e ) { e.printStackTrace(); }

        return Const.TASK_ERROR;
    }
}
