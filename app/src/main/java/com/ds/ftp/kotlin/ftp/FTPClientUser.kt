package com.ds.ftp.kotlin.ftp

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import java.io.IOException
import java.io.InputStream
import java.net.SocketException

/**
 * Class that uses an [FTPClient] to check ftp server connection or upload file.
 */
class FTPClientUser {

    /**
     * The [FTPClient].
     */
    private val ftpClient: FTPClient = FTPClient()

    /**
     * The server address.
     */
    var server: String? = null

    /**
     * The server port.
     */
    var port: Int? = null

    /**
     * The user's name.
     */
    var username: String? = null

    /**
     * The password.
     */
    var password: String? = null

    /**
     * Suspend function to check the connection.
     *
     * @return true of connection can be made to the given ftp server, false otherwise.
     */
    suspend fun checkConnection(): Boolean {
        return withContext(Dispatchers.Default) {
            val connected = if (!ftpClient.isConnected) {
                connect()
            } else {
                true
            }
            try {
                ftpClient.disconnect()
                connected
            } catch (e: IOException) {
                false
            }
        }
    }

    /**
     * Suspend function to upload given file.
     *
     * @param fileName the file's name.
     * @param inputStream the input stream pointing at the file.
     * @return true if upload is successful, false otherwise
     */
    suspend fun uploadFile(fileName: String, inputStream: InputStream?): Boolean {
        if (inputStream == null) {
            return false
        }
        return withContext(Dispatchers.Default) {
            if (!ftpClient.isConnected) {
                connect()
            }
            if (!ftpClient.isConnected) {
                false
            } else {
                try {
                    val success: Boolean
                    ftpClient.apply {
                        setFileType(FTP.BINARY_FILE_TYPE)
                        setFileTransferMode(FTP.STREAM_TRANSFER_MODE)
                        enterLocalPassiveMode()
                        success = storeFile(fileName, inputStream)
                        if (!success) {
                            Log.e("FTPClientUser", "reply code: $replyCode message: $replyString")
                        }
                        disconnect()
                    }
                    success
                } catch (e: IOException) {
                    false
                }
            }
        }
    }

    /**
     * Connects to the given ftp server.
     *
     * @return true if connection was successful, false otherwise.
     */
    private fun connect(): Boolean {
        ftpClient.apply {
            return try {
                when (port) {
                    null -> connect(server)
                    else -> connect(server, port!!)
                }
                login(username, password)
            } catch (e: SocketException) {
                false
            } catch (e: IOException) {
                false
            }
        }
    }
}