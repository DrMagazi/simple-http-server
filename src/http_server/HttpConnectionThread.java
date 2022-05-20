package http_server;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class HttpConnectionThread extends Thread {
    private final Socket socket;
    private final File pubFile;
    private final int reqCount;
    private final String CRLF;
    private final Map<String, String> tempMovedPaths;

    public HttpConnectionThread(Socket socket, File pubFile, int reqCount) {
        this.socket = socket;
        this.pubFile = pubFile;
        this.reqCount = reqCount;
        this.CRLF = "\r\n";
        this.tempMovedPaths = new HashMap<>();
        tempMovedPaths.put("/funny_clown.png", "clown.png");
        tempMovedPaths.put("/blue_sphere.png", "world.png");
    }

    @Override
    public void run() {
        try {
            // Open the I/O streams.
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            // Read request header first line to parse method and URI.
            StringBuilder headerFirstLine = new StringBuilder();
            int reqByte;
            while ((reqByte = inputStream.read()) != 13) {
                headerFirstLine.append((char) reqByte);
            }

            String reqUrl = httpGetUrlParser(headerFirstLine.toString());
            System.out.printf("\"%s\" requested.%n", reqUrl);

            // Respond
            if (tempMovedPaths.containsKey(reqUrl)) {  // 302 Redirect
                respondFound(reqUrl, outputStream);
                System.out.printf("Redirect link provided to request #%d.%n%n", reqCount);
            } else if (reqUrl.equals("/500.html")) { // 500 Internal server error
                respondInternalServerError(outputStream);
                System.out.printf("Internal server error for request #%d. Client notified with 500 page.%n%n", reqCount);
            } else { // 200 OK or 404 Not Found
                reqUrl = pubFile.getPath() + reqUrl;

                // See if request is a file or a directory.
                File reqFile = new File(reqUrl);
                if (reqFile.isDirectory()) {
                    reqFile = new File(reqUrl + "/index.html");
                }

                try {
                    if (reqFile.exists()) {
                        if (isPng(reqFile)) {
                            respondOkPng(outputStream, reqFile);
                        } else {
                            respondOkHtml(reqFile, outputStream);
                        }
                        System.out.printf("Responded to request #%d with page.%n%n", reqCount);
                    } else {
                        respondNotFound(outputStream);
                        System.out.printf("Requested file #%d not found. Client notified with 404 page.%n%n", reqCount);
                    }
                } catch (SocketException e) {
                    System.out.printf("Failed to send for request #%d, pipe broken.%n%n", reqCount);
                }
            }

            // Close the I/O streams and socket.
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private String readHtml(File reqFile) throws IOException {
        // Read file to prepare HTML.
        BufferedReader reader = new BufferedReader(new FileReader(reqFile));
        StringBuilder html = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            html.append(line);
        }

        return html.toString();
    }

    private void respondOkHtml(File reqFile, OutputStream outputStream) throws IOException {
        // Read file to prepare HTML.
        String html = readHtml(reqFile);

        // Prepare HTTP response.
        String httpResponse =
                "HTTP/1.1 200 OK" + CRLF +
                "Content-Length: " + html.getBytes().length + CRLF +
                "Content-Type: text/html; charset=utf-8" + CRLF + CRLF +
                html + CRLF + CRLF; // Body

        // Send HTTP response.
        outputStream.write(httpResponse.getBytes());
        outputStream.flush();
    }

    private void respondNotFound(OutputStream outputStream) throws IOException {
        // Read file to prepare HTML.
        String html = readHtml(new File(pubFile + "/404.html"));

        // Prepare HTTP response.
        String httpResponse =
                "HTTP/1.1 404 Not Found" + CRLF + CRLF +
                 html + CRLF + CRLF; // Body

        // Send HTTP response.
        outputStream.write(httpResponse.getBytes());
        outputStream.flush();
    }

    private void respondFound(String foundPath, OutputStream outputStream) throws IOException {
        String httpResponse =
                "HTTP/1.1 302 Found" + CRLF +
                "Location: %s".formatted(tempMovedPaths.get(foundPath)) + CRLF + CRLF;

        // Send HTTP response.
        outputStream.write(httpResponse.getBytes());
        outputStream.flush();
    }

    private void respondInternalServerError(OutputStream outputStream) throws IOException {
        // Read file to prepare HTML.
        String html = readHtml(new File(pubFile + "/500.html"));

        // Prepare HTTP response.
        String httpResponse =
                "HTTP/1.1 500 Internal Server Error" + CRLF + CRLF +
                    html + CRLF + CRLF; // Body

        // Send HTTP response.
        outputStream.write(httpResponse.getBytes());
        outputStream.flush();
    }

    private void respondOkPng(OutputStream outputStream, File reqFile) throws IOException {

        // Prepare image byte array.
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(ImageIO.read(reqFile), "png", byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();

        // Prepare HTTP response.
        String httpResponseHeader =
                "HTTP/1.1 200 OK" + CRLF +
                "Content-Length " + imgBytes.length + CRLF +
                "Content-Type: image/png" + CRLF + CRLF;

        // Write.
        outputStream.write(httpResponseHeader.getBytes());
        outputStream.write(imgBytes);
        outputStream.write((CRLF + CRLF).getBytes());
        outputStream.flush();
    }

    private boolean isPng(File file) {
        String filePath = file.getPath();
        StringBuilder pathEnd = new StringBuilder();
        for (int i = filePath.length() - 1; filePath.charAt(i) != '.'; i--) {
            pathEnd.append(filePath.charAt(i));
        }
        pathEnd.reverse();
        return pathEnd.toString().equals("png");
    }

    private String httpGetUrlParser(String headerFirstLine) {
        StringBuilder getUrl = new StringBuilder();

        // Get to the start of the url.
        char[] headerChars = headerFirstLine.toCharArray();
        int readIdx = 0;
        while (headerChars[readIdx] != '/') {
            readIdx++;
        }

        // Build String until the end of url.
        while (headerChars[readIdx] != ' ') { // 32 is a line space.
            if (headerChars[readIdx] == '.' && headerChars[readIdx - 1] == '.') { // 46 is a dot.
                System.out.println("POTENTIAL HACK DETECTED!!!!!!!!!!");
                System.out.println("AUTHORITIES HAVE BEEN NOTIFIED.");
                return "/500.html";  // Throws a server error.
            }
            getUrl.append(headerChars[readIdx]);
            readIdx++;
        }

        // Make sure there is no slash at end of url.
        if (getUrl.charAt(getUrl.length()-1) == '/') {
            getUrl.deleteCharAt(getUrl.length()-1);
        }

        return getUrl.toString();
    }
}
