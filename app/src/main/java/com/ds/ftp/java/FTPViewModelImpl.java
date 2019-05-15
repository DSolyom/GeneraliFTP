package com.ds.ftp.java;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ds.ftp.R;
import com.ds.ftp.java.api.FTPViewModel;
import com.ds.ftp.java.api.StartUploadProcessAction;
import com.ds.ftp.java.api.UploadFileAction;
import com.ds.ftp.java.api.FileSelectStarter;
import com.ds.ftp.java.ftp.FTPClientUser;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Implementation of {@link FTPViewModel}.
 */
public class FTPViewModelImpl extends ViewModel implements FTPViewModel {

    // Some default values for testing
    private static final String SERVER = "ftp.drivehq.com";
    private static final String USER = "dsdsdsds";
    private static final String PASSWORD = "0Almacska0";

    /**
     * The message {@link LiveData}.
     */
    private final MutableLiveData<Integer> message = new MutableLiveData<>();

    /**
     * The uploading flag {@link LiveData}.
     */
    private final MutableLiveData<Boolean> isUploading = new MutableLiveData<>();

    /**
     * The server {@link LiveData}.
     */
    private final MutableLiveData<String> server = new MutableLiveData<>();

    /**
     * The port {@link LiveData}.
     */
    private final MutableLiveData<Integer> port = new MutableLiveData<>();

    /**
     * The username {@link LiveData}.
     */
    private final MutableLiveData<String> username = new MutableLiveData<>();

    /**
     * The password {@link LiveData}.
     */
    private final MutableLiveData<String> password = new MutableLiveData<>();
    /**
     * The [FTPClientUser] to use [FTPClient] functionality.
     */
    @NonNull
    private FTPClientUser ftpClientUser = new FTPClientUser();

    /**
     * The disposable background job.
     */
    @Nullable
    private Disposable job;

    /**
     * Constructor - settings default (test) data.
     */
    public FTPViewModelImpl() {
        server.setValue(SERVER);
        username.setValue(USER);
        password.setValue(PASSWORD);
    }

    @Override
    public MutableLiveData<Integer> getMessage() {
        return message;
    }

    @Override
    public MutableLiveData<Boolean> getIsUploading() {
        return isUploading;
    }

    @Override
    public MutableLiveData<String> getServer() {
        return server;
    }

    @Override
    public MutableLiveData<Integer> getPort() {
        return port;
    }

    @Override
    public MutableLiveData<String> getUsername() {
        return username;
    }

    @Override
    public MutableLiveData<String> getPassword() {
        return password;
    }

    @Override
    public StartUploadProcessAction getStartUploadProcessAction(@NonNull final FileSelectStarter fileSelectStarter) {
        return () -> FTPViewModelImpl.this.startUploadProcess(fileSelectStarter);
    }

    @Override
    public UploadFileAction getUploadFileAction() {
        return this::startUpload;
    }

    /**
     * Stats the upload process.
     *
     * @param fileSelectStarter the method to call to start the file selecting.
     */
    private void startUploadProcess(@NonNull final FileSelectStarter fileSelectStarter) {
        if (!checkInput()) {
            message.postValue(R.string.warning_fill_fields);
            return;
        }
        setupFtpClientUser();
        isUploading.postValue(true);

        message.postValue(R.string.trying_to_connect);
        job = Observable.fromCallable(this::checkServer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(serverOk -> {
                    if (!serverOk) {
                        message.postValue(R.string.error_connection);
                        isUploading.postValue(false);
                        return;
                    }
                    message.postValue(R.string.selecting_file);
                    if (job != null && !job.isDisposed()) {
                        fileSelectStarter.startFileSelection();
                    }
                    job = null;
                });
    }

    /**
     * Checks the inputs.
     *
     * @return true if everything is ok, false otherwise.
     */
    private boolean checkInput() {
        final String serverValue = server.getValue();
        final String usernameValue = username.getValue();
        final String passwordValue = password.getValue();
        return !(serverValue == null || serverValue.isEmpty() || usernameValue == null || usernameValue.isEmpty()
                || passwordValue == null || passwordValue.isEmpty());
    }

    /**
     * Setup the [ftpClientUser]. Sets the server and user related data.
     */
    private void setupFtpClientUser() {
        ftpClientUser.setServer(server.getValue());
        ftpClientUser.setPort(port.getValue());
        ftpClientUser.setUsername(username.getValue());
        ftpClientUser.setPassword(password.getValue());
    }

    /**
     * Starts the uploading in [scope].
     *
     * @param contentResolver the content resolver to resolve the target and it's name from given uri.
     * @param uri             the uri pointing the target file.
     */
    private void startUpload(@NonNull final ContentResolver contentResolver, @Nullable final Uri uri,
                             final int resultCode) {
        if (resultCode != Activity.RESULT_OK) {
            isUploading.postValue(false);
            message.postValue(null);
            return;
        }
        if (uri == null) {
            message.postValue(R.string.error_file_not_found);
            return;
        }
        message.postValue(R.string.uploading);
        job = Observable.fromCallable(() -> uploadFile(contentResolver, uri))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    if (success) {
                        message.postValue(R.string.success_upload);
                    } else {
                        message.postValue(R.string.error_upload);
                    }
                    isUploading.postValue(false);
                    job = null;
                });
    }

    /**
     * Uploads the file the [ftpClientUser].
     *
     * @param contentResolver the content resolver to resolve the target and it's name from given uri.
     * @param uri             the uri pointing the target file.
     */
    private boolean uploadFile(@NonNull final ContentResolver contentResolver, @NonNull final Uri uri) {
        final String fileName = getFileName(contentResolver, uri);
        if (fileName == null) {
            return false;
        }
        InputStream inputStream = null;
        try {
            inputStream = contentResolver.openInputStream(uri);
            return ftpClientUser.uploadFile(fileName, inputStream);
        } catch (FileNotFoundException e) {
            return false;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch(Throwable e2) {
                e2.printStackTrace();
            }
        }
    }

    /**
     * Gets the file name from given uri.
     *
     * @param contentResolver the content resolver to resolve the target's name from given uri.
     * @param uri             the uri pointing the target file.
     */
    private String getFileName(@NonNull final ContentResolver contentResolver, @NonNull final Uri uri) {
        String fileName = null;
        final String[] projection = new String[]{MediaStore.MediaColumns.DISPLAY_NAME};
        final Cursor metaCursor = contentResolver.query(uri, projection, null, null, null);
        if (metaCursor != null) {
            try {
                if (metaCursor.moveToFirst()) {
                    fileName = metaCursor.getString(0);
                }
            } finally {
                metaCursor.close();
            }
        }
        return fileName;
    }

    /**
     * Checks the server if it can be connected.
     *
     * @return true if the server can be connected, false otherwise.
     */
    private boolean checkServer() {
        return ftpClientUser.checkConnection();
    }

    @Override
    protected void onCleared() {
        if (job != null && !job.isDisposed()) {
            job.dispose();
        }
        super.onCleared();
    }
}
