package com.ds.ftp.java;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import com.ds.ftp.R;
import com.ds.ftp.java.api.FTPViewModel;

/**
 * The main and only activity. Could be a fragment but there is really no need for that for one screen.
 */
public class FTPActivity extends AppCompatActivity {

    /**
     * Constant for file picking activity result.
     */
    private final static int PICK_FILE_RESULT_CODE = 12312;

    /**
     * Logging tag.
     */
    private final static String TAG = "FTPActivity";

    @Nullable
    private FTPViewModel model;

    @Nullable
    private EditText serverView;

    @Nullable
    private EditText portView;

    @Nullable
    private EditText usernameView;

    @Nullable
    private EditText passwordView;

    @Nullable
    private TextView messageView;

    @Nullable
    private View uploadButtonView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar(findViewById(R.id.toolbar));

        findViews();

        if (serverView == null || portView == null || usernameView == null || passwordView == null ||
                uploadButtonView == null) {
            Log.e(TAG, "Invalid layout.");
            return;
        }

        // create the view model
        model = ViewModelProviders.of(this).get(FTPViewModelImpl.class);
        // restore state from view model (this is not really needed as all states are also view states)
        serverView.setText(model.getServer().getValue());
        usernameView.setText(model.getUsername().getValue());
        passwordView.setText(model.getPassword().getValue());

        // add message and uploading observers
        model.getMessage().observe(this, this::setMessage);
        model.getIsUploading().observe(this, isUploading -> {
            uploadButtonView.setEnabled(!isUploading);
        });

        // button click
        uploadButtonView.setOnClickListener(this::onUploadButtonClicked);
    }

    /**
     * Acts when the upload button is clicked.
     *
     * @param button the upload button.
     */
    private void onUploadButtonClicked(@NonNull final View button) {
        if (model == null) {
            return;
        }
        final Editable serverEditable = serverView == null ? null : serverView.getText();
        model.getServer().setValue(serverEditable == null ? null : serverEditable.toString());

        final Editable portEditable = portView == null ? null : portView.getText();
        final String portString = portEditable == null ? null : portEditable.toString();
        try {
            model.getPort().setValue(portString == null || portString.isEmpty() ? null : Integer.parseInt(portString));
        } catch(final NumberFormatException e) {
            // should not happen because of the EditText inputType
            return;
        }

        final Editable usernameEditable = usernameView == null ? null : usernameView.getText();
        model.getUsername().setValue(usernameEditable == null ? null : usernameEditable.toString());

        final Editable passwordEditable = passwordView == null ? null : passwordView.getText();
        model.getPassword().setValue(passwordEditable == null ? null : passwordEditable.toString());
        // This seems complicated but required for real MVVM since startActivityForResult is not accessible from the
        // view model, but the action should come from it.
        model.getStartUploadProcessAction(this::startFileSelection).startUploadProcess();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (PICK_FILE_RESULT_CODE == requestCode && model != null) {
            model.getUploadFileAction().uploadFile(getContentResolver(), data == null ? null : data.getData(),
                    resultCode);
        }
    }

    /**
     * Find the views and store them.
     */
    private void findViews() {
        serverView = findViewById(R.id.server_view);
        portView = findViewById(R.id.port_view);
        usernameView = findViewById(R.id.username_view);
        passwordView = findViewById(R.id.password_view);
        messageView = findViewById(R.id.message_view);
        uploadButtonView = findViewById(R.id.upload_button_view);
    }

    /**
     * Stats the intent for file selection.
     */
    private void startFileSelection() {
        final Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType(getString(R.string.file_type));
        startActivityForResult(chooseFile, PICK_FILE_RESULT_CODE);
    }

    /**
     * Sets the message using string resource.
     *
     * @param messageRes the message resource.
     */
    private void setMessage(@StringRes Integer messageRes) {
        if (messageView != null) {
            messageView.setText(messageRes == null ? "" : getString(messageRes));
        }
    }
}
