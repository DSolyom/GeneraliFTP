package com.ds.ftp.kotlin.api

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Interface of the ftp view model.
 */
interface FTPViewModel {

    /**
     * [LiveData] to hold the status message.
     */
    val message: MutableLiveData<Int>

    /**
     * [LiveData] to hold the upload state - true if uploading is in progress, false otherwise.
     */
    val uploading: MutableLiveData<Boolean>

    /**
     * [LiveData] to hold the server address.
     * (Could be an object with the rest of the data. Even though it could validate itself for this, it's fine this way)
     */
    val server: MutableLiveData<String>

    /**
     * [LiveData] to hold the server port.
     */
    val port: MutableLiveData<Int>

    /**
     * [LiveData] to hold the user's name.
     */
    val username: MutableLiveData<String>

    /**
     * [LiveData] to hold the password.
     */
    val password: MutableLiveData<String>

    /**
     * Gets the start upload process action.
     *
     * @return the upload process action.
     */
    fun getStartUploadProcessAction(): (() -> Unit) -> Unit

    /**
     * Gets the upload file action.
     *
     * @return the upload file action.
     */
    fun getUploadFileAction(): (ContentResolver, Uri?, Int) -> Unit
}