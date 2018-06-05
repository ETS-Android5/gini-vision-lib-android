package net.gini.android.vision.internal.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;

/**
 * Created by Alpar Szotyori on 05.06.2018.
 *
 * Copyright (c) 2018 Gini GmbH.
 */

/**
 * @exclude
 */
public class AlertDialogFragment extends DialogFragment {

    private static final String ARG_TITLE = "ARG_TITLE";
    private static final String ARG_MESSAGE = "ARG_MESSAGE";
    private static final String ARG_POSITIVE_BUTTON_TITLE = "ARG_POSITIVE_BUTTON_TITLE";
    private static final String ARG_NEGATIVE_BUTTON_TITLE = "ARG_NEGATIVE_BUTTON_TITLE";
    private static final String ARG_DIALOG_ID = "ARG_DIALOG_ID";

    @StringRes
    private int mTitle;
    private int mMessage;
    private int mPositiveButtonTitle;
    private int mNegativeButtonTitle;
    private int mDialogId;

    public static AlertDialogFragment createInstance(@StringRes final int title,
            @StringRes final int message, @StringRes final int positiveButtonTitle,
            @StringRes final int negativeButtonTitle, @StringRes final int dialogId) {
        final Bundle args = new Bundle();
        args.putInt(ARG_TITLE, title);
        args.putInt(ARG_MESSAGE, message);
        args.putInt(ARG_POSITIVE_BUTTON_TITLE, positiveButtonTitle);
        args.putInt(ARG_NEGATIVE_BUTTON_TITLE, negativeButtonTitle);
        args.putInt(ARG_DIALOG_ID, dialogId);
        final AlertDialogFragment fragment = new AlertDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readArguments();
    }

    private void readArguments() {
        final Bundle arguments = getArguments();
        if (arguments == null) {
            return;
        }
        mTitle = arguments.getInt(ARG_TITLE);
        mMessage = arguments.getInt(ARG_MESSAGE);
        mPositiveButtonTitle = arguments.getInt(ARG_POSITIVE_BUTTON_TITLE);
        mNegativeButtonTitle = arguments.getInt(ARG_NEGATIVE_BUTTON_TITLE);
        mDialogId = arguments.getInt(ARG_DIALOG_ID);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (mTitle > 0) {
            builder.setTitle(mTitle);
        }
        if (mMessage > 0) {
            builder.setMessage(mMessage);
        }
        if (mPositiveButtonTitle > 0) {
            builder.setPositiveButton(mPositiveButtonTitle, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    final FragmentActivity activity = getActivity();
                    if (activity instanceof AlertDialogFragmentListener) {
                        ((AlertDialogFragmentListener) activity).onPositiveButtonClicked(
                                dialog, mDialogId);
                    }
                }
            });
        }
        if (mNegativeButtonTitle > 0) {
            builder.setNegativeButton(mNegativeButtonTitle, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    final FragmentActivity activity = getActivity();
                    if (activity instanceof AlertDialogFragmentListener) {
                        ((AlertDialogFragmentListener) activity).onNegativeButtonClicked(dialog,
                                mDialogId);
                    }
                }
            });
        }
        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }
}
