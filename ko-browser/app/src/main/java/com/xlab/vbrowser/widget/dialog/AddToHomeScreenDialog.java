package com.xlab.vbrowser.widget.dialog;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.xlab.vbrowser.R;
import com.xlab.vbrowser.shortcut.HomeScreen;
import com.xlab.vbrowser.shortcut.IconGenerator;

public class AddToHomeScreenDialog extends DialogFragment {
    public static final String FRAGMENT_TAG = "add-to-homescreen-prompt-dialog";
    private static final String URL = "url";
    private static final String TITLE = "title";
    private static final String BLOCKING_ENABLED = "blocking_enabled";

    public static AddToHomeScreenDialog newInstance(final String url, final String title, final boolean blockingEnabled) {
        AddToHomeScreenDialog frag = new AddToHomeScreenDialog();
        final Bundle args = new Bundle();
        args.putString(URL, url);
        args.putString(TITLE, title);
        args.putBoolean(BLOCKING_ENABLED, blockingEnabled);
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public AlertDialog onCreateDialog(Bundle bundle) {
        final String url = getArguments().getString(URL);
        final String title = getArguments().getString(TITLE);
        final boolean blockingEnabled = getArguments().getBoolean(BLOCKING_ENABLED);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogStyle);
        builder.setCancelable(true);
        builder.setTitle(getActivity().getString(R.string.menu_add_to_home_screen));

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.add_to_homescreen, null);
        builder.setView(dialogView);

        // For the dialog we display the Pre Oreo version of the icon because the Oreo+
        // adaptive launcher icon does not have a mask applied until we create the shortcut
        final Bitmap iconBitmap = IconGenerator.generateLauncherIconPreOreo(getContext(), url);
        final ImageView iconView = (ImageView) dialogView.findViewById(R.id.homescreen_icon);
        iconView.setImageBitmap(iconBitmap);

        final ImageView blockIcon = (ImageView) dialogView.findViewById(R.id.homescreen_dialog_block_icon);
        blockIcon.setImageResource(R.drawable.ic_tracking_protection_16_disabled);

        final Button addToHomescreenDialogCancelButton = (Button) dialogView.findViewById(R.id.addtohomescreen_dialog_cancel);
        final Button addToHomescreenDialogConfirmButton = (Button) dialogView.findViewById(R.id.addtohomescreen_dialog_add);
        addToHomescreenDialogCancelButton.setText(getString(R.string.dialog_addtohomescreen_action_cancel));
        addToHomescreenDialogConfirmButton.setText(getString(R.string.dialog_addtohomescreen_action_add));

        final LinearLayout warning = (LinearLayout) dialogView.findViewById(R.id.homescreen_dialog_warning_layout);
        warning.setVisibility(blockingEnabled ? View.GONE : View.VISIBLE);

        final EditText editableTitle = (EditText) dialogView.findViewById(R.id.edit_title);

        if (!TextUtils.isEmpty(title)) {
            editableTitle.setText(title);
            editableTitle.setSelection(title.length());
        }

        addToHomescreenDialogCancelButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }});

        addToHomescreenDialogConfirmButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeScreen.installShortCut(getContext(), IconGenerator.generateLauncherIcon(getContext(), url), url,
                        editableTitle.getText().toString().trim(), blockingEnabled);
                dismiss();
            }});

        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Dialog dialog = getDialog();
        if (dialog != null) {
            final Window window = dialog.getWindow();
            if (window != null) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }
        }
    }
}
