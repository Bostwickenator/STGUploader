package org.bostwickenator.googlephotos;

import com.github.ma1co.pmcademo.app.Logger;
import com.google.api.client.auth.oauth2.Credential;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class GooglePhotosClient {

    Credential credential;

    public GooglePhotosClient(Credential credential) {
            this.credential = credential;
    }

    public void httpPhotoPost(File file) throws Exception {

        HttpsURLConnection connection = (HttpsURLConnection) new URL("https://photoslibrary.googleapis.com/v1/uploads").openConnection();
        connection.setUseCaches(false);
        connection.setRequestProperty("Authorization", "Bearer " + credential.getAccessToken());
        connection.setRequestProperty("Connection", "close");
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        connection.setRequestProperty("Transfer-Encoding", "chunked");
        connection.setDoOutput(true); // Triggers POST.
        connection.setRequestMethod("POST");
        connection.setRequestProperty("X-Goog-Upload-File-Name", file.getName());
        connection.setRequestProperty("X-Goog-Upload-Protocol", "raw");

        OutputStream output = connection.getOutputStream();
        writeFileToStream(file, output);
        output.flush();

        Logger.info("Response Code: " + connection.getResponseCode());
        String token = getResponseString(connection);
        Logger.info(token);
        createMediaItem(token);
    }

    public void createMediaItem(String uploadToken) throws Exception{
        HttpsURLConnection connection = (HttpsURLConnection) new URL("https://photoslibrary.googleapis.com/v1/mediaItems:batchCreate").openConnection();


        connection.setRequestProperty("Authorization", "Bearer " + credential.getAccessToken());
        connection.setUseCaches(false);
        connection.setRequestProperty("Connection", "close");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true); // Triggers POST.

        String json = "{\n" +
                "  \"newMediaItems\": [\n" +
                "    {\n" +
                "      \"simpleMediaItem\": {\n" +
                "        \"uploadToken\": \""+uploadToken+"\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        OutputStream output = connection.getOutputStream();
        output.write(json.getBytes());
        output.flush();

        Logger.info("Response Code: " + connection.getResponseCode());
        Logger.info(getResponseString(connection));
    }

    private String getResponseString(HttpsURLConnection connection) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }


    private void writeFileToStream(File file, OutputStream output) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        int buf_size = 1024 * 1024;
        byte[] buf = new byte[buf_size];
        int read;
        while ((read = fis.read(buf, 0, buf_size)) == buf_size) {
            output.write(buf);
            //Logger.info("chunk sent");
        }
        if (read > 0) {
            output.write(buf, 0, read);
        }
        fis.close();
    }

}
