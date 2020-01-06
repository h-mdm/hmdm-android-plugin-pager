package com.hmdm.pager;

public class Const {
    public static final String LOG_TAG = "com.hmdm.Pager";
    public static final int CONNECTION_TIMEOUT = 10;

    public static final String INTENT_PUSH_NOTIFICATION_TYPE = "com.hmdm.push.textMessage";
    public static final String INTENT_PUSH_NOTIFICATION_EXTRA = "com.hmdm.PUSH_DATA";

    public static final String STATUS_OK = "OK";

    public static final int TASK_SUCCESS = 0;
    public static final int TASK_ERROR = 1;
    public static final int TASK_NETWORK_ERROR = 2;
    public static final int TASK_IDLE = 3;

    public static final int HMDM_RECONNECT_DELAY_FIRST = 5000;
    public static final int HMDM_RECONNECT_DELAY_NEXT = 5000;

    public static final String ACTION_NEW_MESSAGE = "NEW_MESSAGE";
}

