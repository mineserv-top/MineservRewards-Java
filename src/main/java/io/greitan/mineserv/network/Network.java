package io.greitan.mineserv.network;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.net.InetSocketAddress;
import java.net.URLDecoder;

import io.greitan.mineserv.methods.Methods;
import io.greitan.mineserv.utils.*;

public class Network {

    private static HttpServer server;

    public static Boolean startWebServer(String ip, int port, String secretKey) throws IOException {
        server = HttpServer.create(new InetSocketAddress(ip, port), 0);
        server.createContext("/", new MyHandler(secretKey));
        server.setExecutor(null);
        server.start();
        Logger.info(Language.getMessage("ru", "plugin-webserver-run").replace("$ip", ip).replace("$port", String.valueOf(port)));
        return true;
    }

    public static void stopWebServer() {
        if (server != null) {
            Logger.info(Language.getMessage("ru", "plugin-webserver-stop"));
            server.stop(0);
        }
    }

    static class MyHandler implements HttpHandler {
        private final String secretKey;

        public MyHandler(String secretKey) {
            this.secretKey = secretKey;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            String response;
            if ("POST".equals(t.getRequestMethod())) {
                String requestBody = readRequestBody(t);
                Map<String, String> map = parse(requestBody);

                String project = map.get("project");
                String username = map.get("username");
                String timestamp = map.get("timestamp");
                String signature = map.get("signature");

                Boolean isSuccess = checkSign(project, username, timestamp, signature, secretKey);
                if (isSuccess) {
                    response = "done";
                    t.sendResponseHeaders(200, response.length());
                    Methods.runMethods(project, username, timestamp, signature);
                } else {
                    response = "error";
                    t.sendResponseHeaders(500, response.length());
                }
            } else {
                response = "Bad Request";
                t.sendResponseHeaders(400, response.length());
            }

            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private String readRequestBody(HttpExchange t) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(t.getRequestBody(), StandardCharsets.UTF_8));
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            return requestBody.toString();
        }
    }

    private static String SHA256(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    private static boolean checkSign(String project, String username, String timestamp, String signature, String secretKey) {
        try {
            String hash = SHA256(project + '.' + secretKey + '.' + timestamp + '.' + username);
            return hash.equals(signature);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Map<String, String> parse(String queryString) {
        Map<String, String> parameters = new HashMap<>();

        if (queryString != null && queryString.length() > 0) {
            String[] pairs = queryString.split("&");

            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                try {
                    String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                    parameters.put(key, value);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        return parameters;
    }
}
