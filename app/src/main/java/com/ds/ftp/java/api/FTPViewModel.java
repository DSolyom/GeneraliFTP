package com.ds.ftp.java.api;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Interface of the ftp view model.
 */
public interface FTPViewModel {

    /**
     * Getter for {@link LiveData} to hold the status message.
     */
    MutableLiveData<Integer> getMessage();

    /**
     * Getter for {@link LiveData} to hold the upload state - true if uploading is in progress, false otherwise.
     */
    MutableLiveData<Boolean> getIsUploading();

    /**
     * Getter for {@link LiveData} to hold the server address.
     * (Could be an object with the rest of the data. Even though it could validate itself for this, it's fine this way)
     */
    MutableLiveData<String> getServer();

    /**
     * Getter for {@link LiveData} to hold the server port.
     */
    MutableLiveData<Integer> getPort();

    /**
     * Getter for {@link LiveData} to hold the user's name.
     */
    MutableLiveData<String> getUsername();

    /**
     * Getter for {@link LiveData} to hold the password.
     */
    MutableLiveData<String> getPassword();

    /**
     * Gets the start upload process action.
     *
     * @return the upload process action.
     */
    StartUploadProcessAction getStartUploadProcessAction(final FileSelectStarter fileSelectStarter);

    /**
     * Gets the upload file action.
     *
     * @return the upload file action.
     */
    UploadFileAction getUploadFileAction();
}
