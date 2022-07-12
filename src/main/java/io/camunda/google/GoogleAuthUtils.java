package io.camunda.google;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import io.camunda.google.config.GoogleWsConfig;

public class GoogleAuthUtils {
    
    private static GoogleWsConfig googleWsConfig;
    
    public static void configure(GoogleWsConfig googleWsConfig) {
        GoogleAuthUtils.googleWsConfig = googleWsConfig;
    }
    
    public static GoogleWsConfig getGoogleWsConfig() {
        if (googleWsConfig==null) {
            googleWsConfig = new GoogleWsConfig();
        }
        return googleWsConfig;
    }
    
    public static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    
    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    public static Credential getCredentials(NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = DriveUtils.class.getResourceAsStream(getGoogleWsConfig().getCredentialsFilePath());
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + getGoogleWsConfig().getCredentialsFilePath());
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, getGoogleWsConfig().getScopes())
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(getGoogleWsConfig().getTokensDirectoryPath())))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(getGoogleWsConfig().getCallBackPort()).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.
        return credential;
    }
    
}
