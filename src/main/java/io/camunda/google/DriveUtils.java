package io.camunda.google;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;

public class DriveUtils {

	public static Drive drive() {
		// Build a new authorized API client service.
		try {
			final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			Drive service = new Drive.Builder(HTTP_TRANSPORT, GoogleAuthUtils.JSON_FACTORY,
			        GoogleAuthUtils.getCredentials(HTTP_TRANSPORT))
			        .setApplicationName(GoogleAuthUtils.getGoogleWsConfig().getApplicationName()).build();
			return service;
		} catch (GeneralSecurityException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Sheets sheets() {
		// Build a new authorized API client service.
		try {
			final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			Sheets service = new Sheets.Builder(HTTP_TRANSPORT, GoogleAuthUtils.JSON_FACTORY,
			        GoogleAuthUtils.getCredentials(HTTP_TRANSPORT))
			        .setApplicationName(GoogleAuthUtils.getGoogleWsConfig().getApplicationName()).build();
			return service;
		} catch (GeneralSecurityException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static File getFromDrive(String driveId, String localFileName) throws IOException {
		Drive drive = drive();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		drive.files().get(driveId).executeMediaAndDownloadTo(outputStream);
		File result = new File(localFileName);
		try (FileOutputStream fos = new FileOutputStream(result)) {
			outputStream.writeTo(fos);
		}
		return result;
	}

	public static String storeInDrive(File file) throws IOException {
		Drive drive = DriveUtils.drive();

		String mimeType = Files.probeContentType(file.toPath());

		// upload to Google Drive
		com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
		fileMetadata.setName(file.getName());
		fileMetadata.setMimeType(mimeType);

		InputStreamContent mediaContent = new InputStreamContent(mimeType, new FileInputStream(file));
		com.google.api.services.drive.model.File driveFile = drive.files().create(fileMetadata, mediaContent)
		        .setFields("id").execute();

		return driveFile.getId();
	}
}