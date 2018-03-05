package com.nspu.songofspotify.views.dialog;

import android.app.Activity;
import android.app.ProgressDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nspu on 04/03/18.
 */

/**
 * Display a dialog when something is in load
 */
public class WaitingDialog {
    private static WaitingDialog ourInstance;

    private final List<String> mTag = new ArrayList<>();
    private ProgressDialog mDialog;
    private final Activity mActivity;

    public static WaitingDialog getInstance() {
        return ourInstance;
    }

    public static void init(Activity activity) {
        ourInstance = new WaitingDialog(activity);
    }

    private WaitingDialog(Activity activity) {
        mActivity = activity;
    }

    /**
     * Add a tag. If there was no tag, display the dialog.
     * @param tag represent a request for display the dialog
     */
    public void display(String tag) {
        mTag.add(tag);
        if (mTag.size() == 1) {
            mDialog = ProgressDialog.show(mActivity, "",
                    "Loading", true);
        }
    }

    /**
     * Add the tag. If there was 0 tag, hide the dialog.
     * @param tag
     */
    public void hide(String tag) {
        if (mTag.remove(tag)) {
            if (mTag.size() == 0) {
                mDialog.cancel();
            }
        }
    }
}
