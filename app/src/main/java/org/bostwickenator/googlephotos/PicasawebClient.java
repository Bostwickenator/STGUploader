/*
    Copyright 2015 Mark Otway

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package org.bostwickenator.googlephotos;

import com.github.ma1co.pmcademo.app.Logger;
import com.google.api.client.auth.oauth2.Credential;
import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.photos.*;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * This is a simple client that provides high-level operations on the Picasa Web
 * Albums GData API. It can also be used as a command-line application to test
 * out some of the features of the API.
 *
 *
 */
public class PicasawebClient {
    private static final String SYNC_CLIENT_NAME = "STG Uploader";
    private static final int CONNECTION_TIMEOUT_SECS = 10;

    private static final String API_PREFIX
            = "https://picasaweb.google.com/data/feed/api/user/";

    private final PicasawebService service = new PicasawebService(SYNC_CLIENT_NAME);

    private Credential credential;

    /**
     * Constructs a new un-authenticated client.
     */
    public PicasawebClient(Credential credential) {
        this.credential = credential;

        service.setOAuth2Credentials(credential);
        service.setConnectTimeout(1000 * CONNECTION_TIMEOUT_SECS);
        service.setReadTimeout(1000 * CONNECTION_TIMEOUT_SECS);
    }

    private HttpsURLConnection getConnection() throws Exception {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(API_PREFIX + "default/albumid/" + "default").openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true); // Triggers POST.
        connection.setRequestProperty("Authorization", "Bearer " + credential.getAccessToken());
        connection.setUseCaches(false);
        connection.setRequestProperty("GData-Version", "2");
        connection.setRequestProperty("Transfer-Encoding", "chunked");
        connection.setRequestProperty("Connection", "close");
        return connection;
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

    private String generateBoundary() {
        String strBase = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder boundary = new StringBuilder();
        /*Randomly generate the boundary string */
        for (int i = 0; i < 16; i++) {
            int index = (int) (Math.random() * (strBase.length()));
            char character = strBase.charAt(index);
            boundary.append(character);
        }
        return boundary.toString();
    }

    public void httpPhotoPost(File file) throws Exception {

        HttpsURLConnection connection = getConnection();

        String boundary = generateBoundary();

        StringBuilder preamble = new StringBuilder();
        preamble.append("--").append(boundary).append("\r\n");
        preamble.append("Content-Type: application/atom+xml;\r\n\r\n");
        preamble.append("<entry xmlns='http://www.w3.org/2005/Atom'>\r\n");
        preamble.append("<title>");
        preamble.append(file.getName());
        preamble.append("</title>\r\n");
        //preamble.append("<summary>Real cat wants attention too.</summary>\r\n");
        preamble.append("<category scheme=\"http://schemas.google.com/g/2005#kind\"\r\n");
        preamble.append("term=\"http://schemas.google.com/photos/2007#photo\"/>\r\n");
        preamble.append("</entry>\r\n");
        preamble.append("--").append(boundary).append("\r\n");
        preamble.append("Content-type: ");
        preamble.append(FilesystemScanner.isFileAVideo(file) ? "video/mp4" : "image/jpeg");
        preamble.append("\r\n\r\n");

        StringBuilder endOfPart = new StringBuilder();
        endOfPart.append("\r\n--").append(boundary).append("--");
        long totalLength = preamble.length() + file.length() + endOfPart.length();

        connection.setRequestProperty("Content-Length", String.valueOf(totalLength));
        connection.setRequestProperty("Content-Type", "multipart/related; boundary=\"" + boundary + "\"");
        connection.setRequestProperty("MIME-version", "1.0");

        OutputStream output = connection.getOutputStream();
        output.write(preamble.toString().getBytes());
        writeFileToStream(file, output);
        output.write(endOfPart.toString().getBytes());
        output.flush();
        //print result
        Logger.info("Response Code : " + connection.getResponseCode());
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

}
