import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.*;

public class BlockchainServer {
    private static Blockchain electionChain = new Blockchain();
    private static List<String> currentBatch = new ArrayList<>();
    private static Set<String> hasVotedSet = new HashSet<>();
    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/state", new StateHandler());
        server.createContext("/api/vote", new VoteHandler());
        server.createContext("/api/bulk_vote", new BulkVoteHandler());
        server.createContext("/api/seal", new SealHandler());
        server.createContext("/api/audit", new AuditHandler());
        server.createContext("/api/tamper", new TamperHandler());
        server.createContext("/api/reset", new ResetHandler());

        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();
        System.out.println("=========================================");
        System.out.println(" DECENTRALIZED ELECTION NODE ONLINE");
        System.out.println(" Open in browser: http://localhost:" + PORT);
        System.out.println("=========================================");
    }

    private static String extractJsonField(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) {
            search = "\"" + key + "\":";
            start = json.indexOf(search);
            if (start == -1) return "";
            start += search.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            return json.substring(start, end).trim().replace("\"", "");
        }
        start += search.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    private static void sendJsonResponse(HttpExchange t, int statusCode, String jsonResponse) throws IOException {
        byte[] bytes = jsonResponse.getBytes("UTF-8");
        t.getResponseHeaders().set("Content-Type", "application/json");
        t.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = t.getResponseBody();
        os.write(bytes);
        os.close();
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            File file = new File("index.html");
            if (!file.exists()) file = new File("src/index.html");
            if (!file.exists()) {
                String response = "Error: index.html not found.";
                t.sendResponseHeaders(404, response.length());
                t.getResponseBody().write(response.getBytes());
                t.close();
                return;
            }
            t.getResponseHeaders().set("Content-Type", "text/html");
            t.sendResponseHeaders(200, file.length());
            Files.copy(file.toPath(), t.getResponseBody());
            t.close();
        }
    }

    static class StateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            StringBuilder json = new StringBuilder("{");
            json.append("\"pendingVotes\":").append(currentBatch.size()).append(",");
            json.append("\"isChainValid\":").append(electionChain.isChainValid()).append(",");

            Map<String, Integer> tallies = new HashMap<>();
            json.append("\"blocks\":[");
            for (int i = 0; i < electionChain.chain.size(); i++) {
                Block b = electionChain.chain.get(i);
                json.append("{")
                        .append("\"index\":").append(i).append(",")
                        .append("\"hash\":\"").append(b.hash).append("\",")
                        .append("\"previousHash\":\"").append(b.previousHash).append("\",")
                        .append("\"merkleRoot\":\"").append(b.merkleRoot != null ? b.merkleRoot : "").append("\",")
                        .append("\"timestamp\":").append(b.timestamp).append(",")
                        .append("\"votes\":[");
                for (int j = 0; j < b.votes.size(); j++) {
                    String v = b.votes.get(j);
                    json.append("\"").append(v).append("\"");
                    if (j < b.votes.size() - 1) json.append(",");
                    if (i > 0 && v.contains(":")) {
                        String cand = v.split(":")[1];
                        tallies.put(cand, tallies.getOrDefault(cand, 0) + 1);
                    }
                }
                json.append("]}");
                if (i < electionChain.chain.size() - 1) json.append(",");
            }
            json.append("],");

            json.append("\"tallies\":{");
            int count = 0;
            for (Map.Entry<String, Integer> entry : tallies.entrySet()) {
                json.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
                if (++count < tallies.size()) json.append(",");
            }
            json.append("}}");
            sendJsonResponse(t, 200, json.toString());
        }
    }

    static class VoteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                String body = new String(t.getRequestBody().readAllBytes());
                String voterId = extractJsonField(body, "voterId");
                String candidate = extractJsonField(body, "candidate");

                if (hasVotedSet.contains(voterId)) {
                    sendJsonResponse(t, 400, "{\"status\":\"error\",\"message\":\"Voter ID already used!\"}");
                    return;
                }
                hasVotedSet.add(voterId);
                currentBatch.add(voterId + ":" + candidate);
                sendJsonResponse(t, 200, "{\"status\":\"success\",\"message\":\"Vote buffered.\"}");
            }
        }
    }

    // NEW: Adds N random votes to the buffer for a dynamic constituency
    static class BulkVoteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                String body = new String(t.getRequestBody().readAllBytes());
                int count = Integer.parseInt(extractJsonField(body, "count"));
                String[] candidates = {"Malla Reddy",
                        "M.K. Stalin",
                        "Pawan Kalyan",
                        "Balayya Babu",
                        "Yogi Bhai",
                        "Mamata Didi",
                        "Jagan Reddy",
                        "Rahul Gandhi",
                        "RaviKishan",
                        "VD Sateeshan",
                        "NOTA"
                };
                Random rand = new Random();

                for (int i = 0; i < count; i++) {
                    String voterId = "Auto_" + System.currentTimeMillis() + "_" + i;
                    String chosenCandidate = candidates[rand.nextInt(candidates.length)];
                    hasVotedSet.add(voterId);
                    currentBatch.add(voterId + ":" + chosenCandidate);
                }
                sendJsonResponse(t, 200, "{\"status\":\"success\",\"message\":\"" + count + " votes added to buffer.\"}");
            }
        }
    }

    static class SealHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (currentBatch.isEmpty()) {
                sendJsonResponse(t, 400, "{\"status\":\"error\",\"message\":\"Batch buffer is empty.\"}");
                return;
            }
            Block newBlock = new Block(currentBatch, electionChain.getLatestBlock().hash);
            electionChain.addBlock(newBlock);
            currentBatch.clear();
            sendJsonResponse(t, 200, "{\"status\":\"success\",\"message\":\"Block sealed!\"}");
        }
    }

    static class AuditHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            boolean valid = true;
            int brokenIndex = -1;

            for (int i = 1; i < electionChain.chain.size(); i++) {
                Block current = electionChain.chain.get(i);
                Block previous = electionChain.chain.get(i - 1);

                // 1. Verify the votes haven't been decoupled from the Merkle Root
                MerkleTree checkTree = new MerkleTree(current.votes);
                if (!current.merkleRoot.equals(checkTree.getRoot())) {
                    valid = false;
                    brokenIndex = i;
                    break;
                }

                // 2. Verify the Block Hash hasn't been decoupled from the Merkle Root & Timestamp
                if (!current.hash.equals(current.calculateBlockHash())) {
                    valid = false;
                    brokenIndex = i;
                    break;
                }

                // 3. Verify the Previous Hash pointer matches the previous block
                if (!current.previousHash.equals(previous.hash)) {
                    valid = false;
                    brokenIndex = i;
                    break;
                }
            }
            sendJsonResponse(t, 200, "{\"isValid\":" + valid + ",\"brokenBlockIndex\":" + brokenIndex + "}");
        }
    }

    static class TamperHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                String body = new String(t.getRequestBody().readAllBytes());
                int bIndex = Integer.parseInt(extractJsonField(body, "blockIndex"));
                int vIndex = Integer.parseInt(extractJsonField(body, "voteIndex"));
                String fakeData = extractJsonField(body, "fakeData");

                if (bIndex <= 0 || bIndex >= electionChain.chain.size()) {
                    sendJsonResponse(t, 400, "{\"status\":\"error\",\"message\":\"Target block index out of bounds.\"}");
                    return;
                }

                Block hackedBlock = electionChain.chain.get(bIndex);
                if (vIndex < 0 || vIndex >= hackedBlock.votes.size()) {
                    sendJsonResponse(t, 400, "{\"status\":\"error\",\"message\":\"Target vote index out of bounds.\"}");
                    return;
                }

                // Inject tampering into the data string
                hackedBlock.votes.set(vIndex, fakeData);

                // NEW: Intentionally DO NOT recalculate the Merkle Root to simulate active defense failure.
                // MerkleTree tamperedTree = new MerkleTree(hackedBlock.votes);
                // hackedBlock.merkleRoot = tamperedTree.getRoot();

                sendJsonResponse(t, 200, "{\"status\":\"partial_success\",\"message\":\"Data overwritten, but Merkle Root forgery FAILED!\"}");
            }
        }
    }

    static class ResetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getRequestBody().readAllBytes();
            electionChain = new Blockchain();
            currentBatch.clear();
            hasVotedSet.clear();
            sendJsonResponse(t, 200, "{\"status\":\"success\",\"message\":\"Blockchain reset.\"}");
        }
    }
}