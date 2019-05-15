package com.ds.ftp.kotlin

import android.app.Activity
import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ds.ftp.R
import com.ds.ftp.kotlin.api.FTPViewModel
import com.ds.ftp.kotlin.ftp.FTPClientUser
import kotlinx.coroutines.*
import org.apache.commons.net.ftp.FTPClient
import java.io.FileNotFoundException

/**
 * Implementation of the [FTPViewModel].
 */
class FTPViewModelImpl : ViewModel(), FTPViewModel {

    companion object {
        // Some default values for testing
        const val SERVER = "ftp.drivehq.com"
        const val USER = "dsdsdsds"
        const val PASSWORD = "0Almacska0"
    }

    override val message: MutableLiveData<Int> = MutableLiveData()
    override val uploading: MutableLiveData<Boolean> = MutableLiveData()
    override val server: MutableLiveData<String> = MutableLiveData()
    override val port: MutableLiveData<Int> = MutableLiveData()
    override val username: MutableLiveData<String> = MutableLiveData()
    override val password: MutableLiveData<String> = MutableLiveData()

    /**
     * The [FTPClientUser] to use [FTPClient] functionality.
     */
    private val ftpClientUser = FTPClientUser()

    /**
     * The background job.
     */
    private var job: Job = Job()

    /**
     * The coroutine scope.
     */
    private val scope = CoroutineScope(job + Dispatchers.Main)



    init {
        server.value = SERVER
        username.value = USER
        password.value = PASSWORD
    }

    override fun getStartUploadProcessAction(): (() -> Unit) -> Unit {
        return this::startUploadProcess
    }

    override fun getUploadFileAction(): (ContentResolver, Uri?, Int) -> Unit {
        return this::startUpload
    }

    /**
     * Stats the upload process.
     *
     * @param startFileSelector the method to call to start the file selecting.
     */
    private fun startUploadProcess(startFileSelector: () -> Unit) {
        if (!checkInput()) {
            message.postValue(R.string.warning_fill_fields)
            return
        }
        setupFtpClientUser()
        uploading.postValue(true)
        message.postValue(R.string.trying_to_connect)
        scope.launch {
            val serverOk = checkServer()
            if (!serverOk) {
                message.postValue(R.string.error_connection)
                uploading.postValue(false)
                return@launch
            }
            message.postValue(R.string.selecting_file)
            // Check for cancellation
            if (isActive) {
                startFileSelector()
            }
        }
    }

    /**
     * Checks the inputs.
     *
     * @return true if everything is ok, false otherwise.
     */
    private fun checkInput(): Boolean {
        return !server.value.isNullOrEmpty() || !username.value.isNullOrEmpty() || !password.value.isNullOrEmpty()
    }

    /**
     * Setup the [ftpClientUser]. Sets the server and user related data.
     */
    private fun setupFtpClientUser() {
        ftpClientUser.apply {
            server = this@FTPViewModelImpl.server.value
            port = this@FTPViewModelImpl.port.value
            username = this@FTPViewModelImpl.username.value
            password = this@FTPViewModelImpl.password.value
        }
    }

    /**
     * Starts the uploading in [scope].
     *
     * @param contentResolver the content resolver to resolve the target and it's name from given uri.
     * @param uri the uri pointing the target file.
     */
    private fun startUpload(contentResolver: ContentResolver, uri: Uri?, activityResultCode: Int) {
        if (activityResultCode != Activity.RESULT_OK) {
            // onActivityResult with failed file select attempt
            uploading.postValue(false)
            message.postValue(null)
            return
        }
        if (uri == null) {
            message.postValue(R.string.error_file_not_found)
            return
        }
        message.postValue(R.string.uploading)
        scope.launch {
            if (withContext(Dispatchers.Default) { uploadFile(contentResolver, uri) }) {
                message.postValue(R.string.success_upload)
            } else {
                message.postValue(R.string.error_upload)
            }
            uploading.postValue(false)
        }
    }

    /**
     * Uploads the file the [ftpClientUser].
     *
     * @param contentResolver the content resolver to resolve the target and it's name from given uri.
     * @param uri the uri pointing the target file.
     */
    private suspend fun uploadFile(contentResolver: ContentResolver, uri: Uri): Boolean {
        val fileName = getFileName(contentResolver, uri) ?: return false
        return try {
            ftpClientUser.uploadFile(fileName, contentResolver.openInputStream(uri))
        } catch (e: FileNotFoundException) {
            false
        }
    }

    /**
     * Gets the file name from given uri.
     *
     * @param contentResolver the content resolver to resolve the target's name from given uri.
     * @param uri the uri pointing the target file.
     */
    private fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
        var fileName: String? = null
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        val metaCursor = contentResolver.query(uri, projection, null, null, null)
        if (metaCursor != null) {
            try {
                if (metaCursor.moveToFirst()) {
                    fileName = metaCursor.getString(0)
                }
            } finally {
                metaCursor.close()
            }
        }
        return fileName
    }

    /**
     * Checks the server if it can be connected.
     *
     * @return true if the server can be connected, false otherwise.
     */
    private suspend fun checkServer(): Boolean {
        return ftpClientUser.checkConnection()
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}