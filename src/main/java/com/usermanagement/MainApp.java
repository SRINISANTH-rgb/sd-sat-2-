package com.usermanagement;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.usermanagement.dao.InMemoryUserDAO;
import com.usermanagement.servlet.UserServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Embedded Server Runner for instant practical demonstration without manual Tomcat setup.
 * Listens on http://localhost:8080
 */
public class MainApp {
    private static final int PORT = 8080;
    private static final String WEBAPP_DIR = "src/main/webapp";

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // Initialize UserServlet instance
        UserServlet userServlet = new UserServlet();
        userServlet.init();

        // Servlet API Handler for /users endpoint
        server.createContext("/users", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                try {
                    dispatchToServlet(userServlet, exchange);
                } catch (Exception e) {
                    e.printStackTrace();
                    String err = "{\"error\": \"" + e.getMessage() + "\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(500, err.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(err.getBytes());
                    os.close();
                }
            }
        });

        // Static File Handler for HTML/CSS/JS frontend
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String path = exchange.getRequestURI().getPath();
                if (path.equals("/") || path.trim().isEmpty()) {
                    path = "/index.html";
                }

                // Check static files in webapp directory
                Path filePath = Paths.get(WEBAPP_DIR, path);
                if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                    String notFound = "<h1>404 Not Found</h1>";
                    exchange.sendResponseHeaders(404, notFound.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(notFound.getBytes());
                    os.close();
                    return;
                }

                String contentType = getContentType(path);
                byte[] content = Files.readAllBytes(filePath);
                
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, content.length);
                OutputStream os = exchange.getResponseBody();
                os.write(content);
                os.close();
            }
        });

        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("==================================================================");
        System.out.println("[SUCCESS] JAVA SERVLET USER MANAGEMENT SYSTEM RUNNING!");
        System.out.println("------------------------------------------------------------------");
        System.out.println("  Web Application URL : http://localhost:" + PORT);
        System.out.println("  User Servlet Endpoint: http://localhost:" + PORT + "/users");
        System.out.println("==================================================================");
        System.out.println("Press Ctrl+C to stop the server.");
    }

    private static String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=UTF-8";
        if (path.endsWith(".css")) return "text/css; charset=UTF-8";
        if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (path.endsWith(".json")) return "application/json; charset=UTF-8";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        return "text/plain";
    }

    /**
     * Bridges com.sun.net.httpserver HttpExchange to HttpServlet invocation
     */
    private static void dispatchToServlet(UserServlet servlet, HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod().toUpperCase();
        String query = exchange.getRequestURI().getQuery();
        
        // Read request body
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream is = exchange.getRequestBody()) {
            byte[] buf = new byte[1024];
            int n;
            while ((n = is.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
        }
        byte[] bodyBytes = baos.toByteArray();
        String bodyString = new String(bodyBytes, StandardCharsets.UTF_8);

        Map<String, String[]> parameters = parseParameters(query, bodyString, exchange.getRequestHeaders().getFirst("Content-Type"));

        ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
        int[] responseStatusCode = new int[]{200};
        Map<String, String> responseHeadersMap = new HashMap<>();

        // Create Proxy for HttpServletRequest
        HttpServletRequest requestProxy = (HttpServletRequest) Proxy.newProxyInstance(
            HttpServletRequest.class.getClassLoader(),
            new Class<?>[]{HttpServletRequest.class},
            (proxy, m, args) -> {
                String methodName = m.getName();
                if ("getMethod".equals(methodName)) return method;
                if ("getParameter".equals(methodName)) {
                    String[] vals = parameters.get((String) args[0]);
                    return (vals != null && vals.length > 0) ? vals[0] : null;
                }
                if ("getParameterValues".equals(methodName)) return parameters.get((String) args[0]);
                if ("getParameterMap".equals(methodName)) return parameters;
                if ("getContentType".equals(methodName)) return exchange.getRequestHeaders().getFirst("Content-Type");
                if ("getReader".equals(methodName)) return new BufferedReader(new StringReader(bodyString));
                if ("getInputStream".equals(methodName)) return new ServletInputStreamAdapter(bodyBytes);
                if ("getQueryString".equals(methodName)) return query;
                if ("getRequestURI".equals(methodName)) return exchange.getRequestURI().getPath();
                if ("getCharacterEncoding".equals(methodName)) return "UTF-8";
                
                // Return safe default primitives
                if (m.getReturnType().equals(boolean.class) || m.getReturnType().equals(Boolean.class)) {
                    return Boolean.FALSE;
                }
                if (m.getReturnType().equals(int.class) || m.getReturnType().equals(Integer.class)) {
                    return 0;
                }
                return null;
            }
        );

        PrintWriter[] writerHolder = new PrintWriter[1];

        // Create Proxy for HttpServletResponse
        HttpServletResponse responseProxy = (HttpServletResponse) Proxy.newProxyInstance(
            HttpServletResponse.class.getClassLoader(),
            new Class<?>[]{HttpServletResponse.class},
            (proxy, m, args) -> {
                String methodName = m.getName();
                if ("setStatus".equals(methodName)) {
                    responseStatusCode[0] = (int) args[0];
                    return null;
                }
                if ("setHeader".equals(methodName) || "addHeader".equals(methodName)) {
                    responseHeadersMap.put((String) args[0], (String) args[1]);
                    return null;
                }
                if ("containsHeader".equals(methodName)) {
                    return responseHeadersMap.containsKey((String) args[0]);
                }
                if ("setContentType".equals(methodName)) {
                    responseHeadersMap.put("Content-Type", (String) args[0]);
                    return null;
                }
                if ("getWriter".equals(methodName)) {
                    if (writerHolder[0] == null) {
                        writerHolder[0] = new PrintWriter(new OutputStreamWriter(responseBuffer, StandardCharsets.UTF_8), true);
                    }
                    return writerHolder[0];
                }
                if ("getOutputStream".equals(methodName)) {
                    return new ServletOutputStreamAdapter(responseBuffer);
                }
                if ("getCharacterEncoding".equals(methodName)) return "UTF-8";

                // Return safe default primitives
                if (m.getReturnType().equals(boolean.class) || m.getReturnType().equals(Boolean.class)) {
                    return Boolean.FALSE;
                }
                if (m.getReturnType().equals(int.class) || m.getReturnType().equals(Integer.class)) {
                    return 0;
                }
                return null;
            }
        );

        // Invoke Servlet Service
        servlet.service(requestProxy, responseProxy);

        if (writerHolder[0] != null) {
            writerHolder[0].flush();
        }

        // Send headers back to exchange
        for (Map.Entry<String, String> entry : responseHeadersMap.entrySet()) {
            exchange.getResponseHeaders().set(entry.getKey(), entry.getValue());
        }

        byte[] respBytes = responseBuffer.toByteArray();
        exchange.sendResponseHeaders(responseStatusCode[0], respBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(respBytes);
        os.close();
    }

    private static Map<String, String[]> parseParameters(String query, String body, String contentType) {
        Map<String, String[]> map = new HashMap<>();
        parseUrlEncoded(query, map);

        if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
            parseUrlEncoded(body, map);
        }
        return map;
    }

    private static void parseUrlEncoded(String data, Map<String, String[]> map) {
        if (data == null || data.trim().isEmpty()) return;
        String[] pairs = data.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            try {
                String k = URLDecoder.decode(kv[0], "UTF-8");
                String v = kv.length > 1 ? URLDecoder.decode(kv[1], "UTF-8") : "";
                String[] existing = map.get(k);
                if (existing == null) {
                    map.put(k, new String[]{v});
                } else {
                    String[] newArr = Arrays.copyOf(existing, existing.length + 1);
                    newArr[existing.length] = v;
                    map.put(k, newArr);
                }
            } catch (Exception e) {
                // ignore invalid encodings
            }
        }
    }

    private static class ServletInputStreamAdapter extends javax.servlet.ServletInputStream {
        private final ByteArrayInputStream bais;
        public ServletInputStreamAdapter(byte[] data) {
            this.bais = new ByteArrayInputStream(data);
        }
        @Override public int read() throws IOException { return bais.read(); }
        @Override public boolean isFinished() { return bais.available() == 0; }
        @Override public boolean isReady() { return true; }
        @Override public void setReadListener(javax.servlet.ReadListener readListener) {}
    }

    private static class ServletOutputStreamAdapter extends javax.servlet.ServletOutputStream {
        private final ByteArrayOutputStream baos;
        public ServletOutputStreamAdapter(ByteArrayOutputStream baos) {
            this.baos = baos;
        }
        @Override public void write(int b) throws IOException { baos.write(b); }
        @Override public boolean isReady() { return true; }
        @Override public void setWriteListener(javax.servlet.WriteListener writeListener) {}
    }
}
