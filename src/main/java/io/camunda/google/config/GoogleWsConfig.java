package io.camunda.google.config;

import java.util.Set;

import com.google.api.services.drive.DriveScopes;
import com.google.api.services.gmail.GmailScopes;

public class GoogleWsConfig {

	private String applicationName = "Camunda Project";

	private int callBackPort = 8888;

	private Set<String> scopes = Set.of(DriveScopes.DRIVE_FILE, "https://mail.google.com/");

	private String tokensDirectoryPath = "tokens";

	private String credentialsFilePath = "/client_secret_google_api.json";

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public int getCallBackPort() {
		return callBackPort;
	}

	public void setCallBackPort(int callBackPort) {
		this.callBackPort = callBackPort;
	}

	public Set<String> getScopes() {
		return scopes;
	}

	public void setScopes(Set<String> scopes) {
		this.scopes = scopes;
	}

	public String getTokensDirectoryPath() {
		return tokensDirectoryPath;
	}

	public void setTokensDirectoryPath(String tokensDirectoryPath) {
		this.tokensDirectoryPath = tokensDirectoryPath;
	}

	public String getCredentialsFilePath() {
		return credentialsFilePath;
	}

	public void setCredentialsFilePath(String credentialsFilePath) {
		this.credentialsFilePath = credentialsFilePath;
	}
}
