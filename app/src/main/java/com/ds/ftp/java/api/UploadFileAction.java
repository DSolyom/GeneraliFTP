package com.ds.ftp.java.api;

import android.content.ContentResolver;
import android.net.Uri;

/**
 * Interface for the upload file action.
 */
public interface UploadFileAction {

    /**
     * Uploads the file.
     *
     * @param contentResolver the content resolver to resolve the target and it's name from given uri.
     * @param uri             the uri pointing the target file.
     * @param resultCode      the result code from onActivityResult.
     */
    void uploadFile(final ContentResolver contentResolver, final Uri uri, final int resultCode);
}
