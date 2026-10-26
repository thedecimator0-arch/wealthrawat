package com.bank;

import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.service.BankService;
import com.bank.util.DatabaseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class WebServer {
    private static final int MAX_REQUEST_BYTES = 4096;
    private final BankService bankService = new BankService();
    private final Path publicDirectory;

    public WebServer(Path publicDirectory) {
        this.publicDirectory = publicDirectory.toAbsolutePath().normalize();
    }

    public void start(int port) throws IOException, InterruptedException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", this::handleRequest);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        System.out.println("[WEB] Dashboard and API listening at http://127.0.0.1:" + port);
        System.out.println("[WEB] Database URL: " + DatabaseUtil.getDatabaseUrl());
        new CountDownLatch(1).await();
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            if (path.startsWith("/api/")) {
                handleApi(exchange, path);
            } else {
                serveStaticFile(exchange, path);
            }
        } catch (SQLException e) {
            System.err.println("[API ERROR] Database request failed: " + e.getMessage());
            sendJson(exchange, 500, "{\"error\":\"Database request failed.\"}");
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, jsonObject("error", e.getMessage()));
        } catch (IOException e) {
            System.err.println("[WEB ERROR] Request failed: " + e.getMessage());
            throw e;
        } finally {
            exchange.close();
        }
    }

    private void handleApi(HttpExchange exchange, String path) throws IOException, SQLException {
        if (path.equals("/api/health")) {
            if (!exchange.getRequestMethod().equals("GET")) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed.\"}");
                return;
            }
            sendJson(exchange, 200, health());
            return;
        }
        if (path.equals("/api/accounts")) {
            if (!exchange.getRequestMethod().equals("GET")) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed.\"}");
                return;
            }
            sendJson(exchange, 200, accountsJson(bankService.getAccounts()));
            return;
        }
        if (path.equals("/api/transactions")) {
            if (!exchange.getRequestMethod().equals("GET")) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed.\"}");
                return;
            }
            int limit = parseLimit(exchange.getRequestURI().getRawQuery());
            sendJson(exchange, 200, transactionsJson(bankService.getRecentTransactions(limit)));
            return;
        }
        if (path.equals("/api/deposit") || path.equals("/api/transfer")) {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed.\"}");
                return;
            }
            Map<String, String> form = readForm(exchange);
            double amount = parseAmount(form.get("amount"));
            if (path.equals("/api/deposit")) {
                String accountNumber = required(form, "accountNumber");
                Transaction transaction = bankService.depositToAccount(accountNumber, amount);
                sendJson(exchange, 201, transactionJson(transaction));
            } else {
                String from = required(form, "fromAccount");
                String to = required(form, "toAccount");
                bankService.transferFunds(from, to, amount);
                sendJson(exchange, 200, "{\"status\":\"completed\"}");
            }
            return;
        }
        sendJson(exchange, 404, "{\"error\":\"API endpoint not found.\"}");
    }

    private static String health() throws SQLException {
        try (Connection connection = DatabaseUtil.getConnection();
             Statement statement = connection.createStatement();
             ResultSet database = statement.executeQuery("PRAGMA database_list")) {
            String databasePath = database.next() ? database.getString("file") : "";
            try (ResultSet accounts = statement.executeQuery("SELECT COUNT(*) FROM accounts")) {
                int accountCount = accounts.next() ? accounts.getInt(1) : 0;
                return "{\"status\":\"ok\",\"database\":" + jsonString(databasePath)
                        + ",\"accountCount\":" + accountCount + "}";
            }
        }
    }

    private static String accountsJson(List<Account> accounts) {
        StringBuilder json = new StringBuilder("[");
        for (Account account : accounts) {
            if (json.length() > 1) json.append(',');
            json.append("{\"accountNumber\":").append(jsonString(account.getAccountNumber()))
                    .append(",\"customerId\":").append(jsonString(account.getCustomerId()))
                    .append(",\"accountType\":").append(jsonString(account.getAccountType()))
                    .append(",\"balance\":").append(account.getBalance())
                    .append(",\"status\":").append(jsonString(account.getStatus())).append('}');
        }
        return json.append(']').toString();
    }

    private static String transactionsJson(List<Transaction> transactions) {
        StringBuilder json = new StringBuilder("[");
        for (Transaction transaction : transactions) {
            if (json.length() > 1) json.append(',');
            json.append(transactionJson(transaction));
        }
        return json.append(']').toString();
    }

    private static String transactionJson(Transaction transaction) {
        return "{\"transactionId\":" + jsonString(transaction.getTransactionId())
                + ",\"accountNumber\":" + jsonString(transaction.getAccountNumber())
                + ",\"counterpartyAccount\":" + jsonString(transaction.getCounterpartyAccount())
                + ",\"type\":" + jsonString(transaction.getType())
                + ",\"amount\":" + transaction.getAmount()
                + ",\"balanceAfter\":" + transaction.getBalanceAfter()
                + ",\"timestamp\":" + jsonString(transaction.getTimestamp() == null
                ? null : transaction.getTimestamp().toString()) + "}";
    }

    private static String jsonObject(String key, String value) {
        return "{\"" + key + "\":" + jsonString(value) + "}";
    }

    private static String jsonString(String value) {
        if (value == null) return "null";
        StringBuilder escaped = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch (character) {
                case '"': escaped.append("\\\""); break;
                case '\\': escaped.append("\\\\"); break;
                case '\b': escaped.append("\\b"); break;
                case '\f': escaped.append("\\f"); break;
                case '\n': escaped.append("\\n"); break;
                case '\r': escaped.append("\\r"); break;
                case '\t': escaped.append("\\t"); break;
                default:
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
            }
        }
        return escaped.append('"').toString();
    }

    private static int parseLimit(String query) {
        if (query == null || query.isBlank()) return 50;
        for (String entry : query.split("&")) {
            String[] pair = entry.split("=", 2);
            if (pair[0].equals("limit")) {
                try {
                    int limit = Integer.parseInt(pair.length == 2 ? pair[1] : "");
                    if (limit < 1 || limit > 100) {
                        throw new IllegalArgumentException("Transaction limit must be between 1 and 100.");
                    }
                    return limit;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Transaction limit must be a whole number.");
                }
            }
        }
        return 50;
    }

    private static Map<String, String> readForm(HttpExchange exchange) throws IOException {
        byte[] body = exchange.getRequestBody().readNBytes(MAX_REQUEST_BYTES + 1);
        if (body.length > MAX_REQUEST_BYTES) {
            throw new IllegalArgumentException("Request body is too large.");
        }
        Map<String, String> values = new HashMap<>();
        String encoded = new String(body, StandardCharsets.UTF_8);
        for (String entry : encoded.split("&")) {
            if (entry.isEmpty()) continue;
            String[] pair = entry.split("=", 2);
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String value = URLDecoder.decode(pair.length == 2 ? pair[1] : "", StandardCharsets.UTF_8);
            values.put(key, value);
        }
        return values;
    }

    private static String required(Map<String, String> values, String name) {
        String value = values.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required field: " + name);
        }
        return value;
    }

    private static double parseAmount(String value) {
        try {
            double amount = Double.parseDouble(value);
            if (!Double.isFinite(amount) || amount <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero.");
            }
            return amount;
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("Enter a valid amount greater than zero.");
        }
    }

    private void serveStaticFile(HttpExchange exchange, String requestPath) throws IOException {
        if (!exchange.getRequestMethod().equals("GET") && !exchange.getRequestMethod().equals("HEAD")) {
            sendJson(exchange, 405, "{\"error\":\"Method not allowed.\"}");
            return;
        }
        String relativePath = requestPath.equals("/") ? "index.html" : requestPath.substring(1);
        Path file = publicDirectory.resolve(relativePath).normalize();
        if (!file.startsWith(publicDirectory) || !Files.isRegularFile(file)) {
            sendJson(exchange, 404, "{\"error\":\"File not found.\"}");
            return;
        }
        String contentType = Files.probeContentType(file);
        if (contentType == null) contentType = "application/octet-stream";
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        byte[] content = Files.readAllBytes(file);
        exchange.sendResponseHeaders(200, exchange.getRequestMethod().equals("HEAD") ? -1 : content.length);
        if (!exchange.getRequestMethod().equals("HEAD")) {
            exchange.getResponseBody().write(content);
        }
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] content = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, content.length);
        exchange.getResponseBody().write(content);
    }
}
