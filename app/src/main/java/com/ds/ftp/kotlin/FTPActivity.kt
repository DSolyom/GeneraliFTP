package com.ds.ftp.kotlin

import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ds.ftp.R
import com.ds.ftp.kotlin.api.FTPViewModel
import kotlinx.android.synthetic.main.activity_main.*

/**
 * The main and only activity. Could be a fragment but there is really no need for that for one screen.
 */
class FTPActivity : AppCompatActivity() {

    companion object {

        /**
         * Constant for file picking activity result.
         */
        const val PICK_FILE_RESULT_CODE = 12312
    }

    /**
     * The view model - set in [onCreate].
     */
    private lateinit var model: FTPViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        // create the view model
        model = ViewModelProviders.of(this).get(FTPViewModelImpl::class.java)
        // restore state from view model (this is not really needed as all states are also view states)
        server_view.setText(model.server.value)
        username_view.setText(model.username.value)
        password_view.setText(model.password.value)

        // add message and uploading observers
        model.message.observe(this, Observer<Int> { message ->
            setMessage(message)
        })
        model.uploading.observe(this, Observer<Boolean> { uploading ->
            upload_button_view.isEnabled = !uploading
        })

        // button click
        upload_button_view.setOnClickListener {
            model.server.value = server_view.text?.toString()
            model.port.value = port_view.text?.toString()?.toIntOrNull()
            model.username.value = username_view.text?.toString()
            model.password.value = password_view.text?.toString()
            model.getStartUploadProcessAction()(this::startFileSelection)
        }
    }

    /**
     * Stats the intent for file selection.
     */
    private fun startFileSelection() {
        val chooseFile = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = getString(R.string.file_type)
        }
        startActivityForResult(chooseFile, PICK_FILE_RESULT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (PICK_FILE_RESULT_CODE == requestCode) {
            model.getUploadFileAction()(contentResolver, data?.data, resultCode)
        }
    }

    /**
     * Sets the message using string resource.
     *
     * @param messageRes the message resource.
     */
    private fun setMessage(@StringRes messageRes: Int?) {
        message_view.text = when(messageRes) {
            null -> ""
            else -> getString(messageRes)
        }
    }
}
