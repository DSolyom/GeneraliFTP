package com.ds.ftp.java.ftp;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

/**
 * Class that uses an {@link FTPClient} to check ftp server connection or upload file.
 */
public class FTPClientUser {

    /**
     * Logging tag.
     */
    private final static String TAG = "FTPClientUser";

    /**
     * The [FTPClient].
     */
    private FTPClient ftpClient = new FTPClient();

    /**
     * The server address.
     */
    @Nullable
    private String server;

    /**
     * The server port.
     */
    @Nullable
    private Integer port;

    /**
     * The user's name.
     */
    @Nullable
    private String username;

    /**
     * The password.
     */
    @Nullable
    private String password;

    /**
     * Sets the server address.
     *
     * @param server the server address.
     */
    public void setServer(@Nullable String server) {
        this.server = server;
    }

    /**
     * Sets the server port.
     *
     * @param port the server port.
     */
    public void setPort(@Nullable Integer port) {
        this.port = port;
    }

    /**
     * Sets the user's name.
     *
     * @param username the user's name.
     */
    public void setUsername(@Nullable String username) {
        this.username = username;
    }

    /**
     * Sets the password.
     *
     * @param password the passwrd.
     */
    public void setPassword(@Nullable String password) {
        this.password = password;
    }

    /**
     * Suspend function to check the connection.
     *
     * @return true of connection can be made to the given ftp server, false otherwise.
     */
    public boolean checkConnection() {
        boolean connected = true;
        if (!ftpClient.isConnected()) {
            connected = connect();
        }
        try {
            ftpClient.disconnect();
            return connected;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Suspend function to upload given file.
     *
     * @param fileName    the file's name.
     * @param inputStream the input stream pointing at the file.
     * @return true if upload is successful, false otherwise
     */
    public boolean uploadFile(@NonNull final String fileName, @Nullable final InputStream inputStream) {
        if (inputStream == null) {
            return false;
        }
        if (!ftpClient.isConnected()) {
            connect();
        }
        if (!ftpClient.isConnected()) {
            return false;
        } else {
            try {
                boolean success;
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
                ftpClient.enterLocalPassiveMode();
                success = ftpClient.storeFile(fileName, inputStream);
                if (!success) {
                    Log.e(TAG, "reply code: " + ftpClient.getReplyCode() +
                            "  Code message: " + ftpClient.getReplyString());
                }
                ftpClient.disconnect();

                return success;
            } catch (IOException e) {
                return false;
            }
        }
    }

    /**
     * Connects to the given ftp server.
     *
     * @return true if connection was successful, false otherwise.
     */
    private boolean connect() {
        try {
            if (port == null) {
                ftpClient.connect(server);
            } else {
                ftpClient.connect(server, port);
            }
            return ftpClient.login(username, password);
        } catch (SocketException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
