import java.util.*;
public class VotingSystem1 {
    public static void main(String[] args) throws InterruptedException {
        Blockchain electionChain = new Blockchain();
        List<String> currentBatch = new ArrayList<>();
        System.out.println("=========================================\n DECENTRALIZED ELECTION: AUTOMATED RUN \n=========================================\n");
        List<String> predefinedVotes = new ArrayList<>();
        for (int i = 0; i < 15; i++) predefinedVotes.add("Malla Reddy");
        for (int i = 0; i < 12; i++) predefinedVotes.add("Pawan Kalyan");
        for (int i = 0; i < 10; i++) predefinedVotes.add("Balayya Babu");
        for (int i = 0; i < 8; i++) predefinedVotes.add("M.K. Stalin");
        for (int i = 0; i < 5; i++) predefinedVotes.add("Jagan Babu");
        for (int i = 0; i < 4; i++) predefinedVotes.add("Rahul Gandhi");
        for (int i = 0; i < 2; i++) predefinedVotes.add("Yogi Bhai");
        for (int i = 0; i < 2; i++) predefinedVotes.add("Mamata Didi");
        for (int i = 0; i < 1; i++) predefinedVotes.add("RaviKishan");
        for (int i = 0; i < 1; i++) predefinedVotes.add("VD Sateeshan");
        Collections.shuffle(predefinedVotes);
        int blockCounter = 1;
        System.out.println(">> Processing 60 voters into blocks of 10...\n");
        for (int i = 0; i < 60; i++) {
            String voterId = "Voter_" + String.format("%03d", (i + 1));
            String candidate = predefinedVotes.get(i);
            currentBatch.add(voterId + ":" + candidate);
            if (currentBatch.size() == 10) {
                Block newBlock = new Block(currentBatch, electionChain.getLatestBlock().hash);
                electionChain.addBlock(newBlock);
                System.out.println("--- Block " + blockCounter + " Sealed ---\nVotes included: 10\nMerkle Root: " + newBlock.merkleRoot + "\nBlock Hash:  " + newBlock.hash + "\n");
                currentBatch.clear();
                blockCounter++;
            }
        }
        Scanner scanner = new Scanner(System.in);
        System.out.println("=========================================\n HACKER TERMINAL ACTIVATED \n=========================================");
        System.out.print("Do you want to attempt to tamper with the election data? (Y/N): ");
        String hackChoice = scanner.nextLine();
        if (hackChoice.equalsIgnoreCase("Y")) {
            System.out.println("\nAvailable blocks to hack: 1 through 6");
            System.out.print("Select Block Number to alter: ");
            int blockIndex = Integer.parseInt(scanner.nextLine());
            if (blockIndex < 1 || blockIndex > 6) {
                System.out.println(">> Invalid Block. Terminating hacker tool.");
                scanner.close();
                return;
            }
            Block hackedBlock = electionChain.chain.get(blockIndex);
            System.out.println("\nVotes in Block " + blockIndex + ":");
            for (int i = 0; i < hackedBlock.votes.size(); i++) System.out.println(i + ": " + hackedBlock.votes.get(i));
            System.out.print("\nSelect Vote Index to alter (0-9): ");
            int voteIndex = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter the forged vote data: ");
            String fakeData = scanner.nextLine();
            Thread.sleep(1000);
            hackedBlock.votes.set(voteIndex, fakeData);
            Thread.sleep(1500);
            MerkleTree tamperedTree = new MerkleTree(hackedBlock.votes);
            hackedBlock.merkleRoot = tamperedTree.getRoot();
            Thread.sleep(1000);
            System.out.println("\n--- RUNNING SYSTEM-WIDE CRYPTOGRAPHIC AUDIT ---");
            Thread.sleep(1500);
            if (electionChain.isChainValid()) {
                System.out.println(">> STATUS: Hack successful. System failed to detect tampering.");
            } else {
                System.out.println(">> STATUS: [CRITICAL ALERT] INTEGRITY COMPROMISED!\n>> RESULT: Blockchain validation failed. Attack isolated and exposed.");
            }
        } else {
            System.out.println(">> Election secured safely. Shutting down.");
        }
        System.out.println("=========================================\n OFFICIAL ELECTION RESULTS \n=========================================");
        Map<String, Integer> voteTally = new HashMap<>();
        for (int i = 1; i < electionChain.chain.size(); i++) {
            Block block = electionChain.chain.get(i);
            for (String voteData : block.votes) {
                String candidate = voteData.split(":")[1];
                voteTally.put(candidate, voteTally.getOrDefault(candidate, 0) + 1);
            }
        }
        String declaredWinner = "";
        int highestVotes = 0;
        boolean isTie = false;
        for (Map.Entry<String, Integer> entry : voteTally.entrySet()) {
            System.out.println("- " + entry.getKey() + ": \t" + entry.getValue() + " votes");
            if (entry.getValue() > highestVotes) {
                highestVotes = entry.getValue();
                declaredWinner = entry.getKey();
                isTie = false;
            } else if (entry.getValue() == highestVotes) {
                isTie = true;
            }
        }
        if (isTie) {
            System.out.println("\n*** ELECTION RESULT: TIE - NO OUTRIGHT WINNER ***\n");
        } else {
            System.out.println("\n*** WINNER DECLARED: " + declaredWinner + " with " + highestVotes + " votes! ***\n");
        }
        scanner.close();
    }
}