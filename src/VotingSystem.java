import java.util.*;
public class VotingSystem {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Blockchain electionChain = new Blockchain();
        List<String> currentBatch = new ArrayList<>();
        Set<String> hasVotedSet = new HashSet<>();
        String[] candidates = {
                "Malla Reddy",
                "M.K. Stalin",
                "Pawan Kalyan",
                "Balayya Babu",
                "Yogi Bhai",
                "Mamata Didi",
                "Jagan Babu",
                "Rahul Gandhi",
                "RaviKishan",
                "VD Sateeshan",
                "NOTA"
        };
        boolean isVotingOpen = true;
        System.out.println("=========================================");
        System.out.println(" DECENTRALIZED ELECTION TERMINAL ");
        System.out.println("=========================================");
        while (isVotingOpen) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Cast a Vote");
            System.out.println("2. Seal Current Batch into Blockchain");
            System.out.println("3. Verify Election Integrity");
            System.out.println("4. HACKER MODE: Tamper with a Block");
            System.out.println("5. End Election & Exit");
            System.out.print("Choose an option (1-5): ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    System.out.print("Enter Voter ID: ");
                    String voterId = scanner.nextLine();
                    if (hasVotedSet.contains(voterId)) {
                        System.out.println(">> SECURITY ALERT: Vote Rejected! Voter ID '" + voterId + "' has already cast a vote.");
                        break;
                    }
                    System.out.println("\nCandidates:");
                    for (int i = 0; i < candidates.length; i++) {
                        System.out.println((i + 1) + ". " + candidates[i]);
                    }
                    System.out.print("Select a candidate (1-5): ");
                    int candChoice;
                    try {
                        candChoice = Integer.parseInt(scanner.nextLine());
                        if (candChoice < 1 || candChoice > 5) throw new Exception();
                    } catch (Exception e) {
                        System.out.println(">> Invalid candidate selection. Vote cancelled.");
                        break;
                    }
                    String candidate = candidates[candChoice - 1];

                    hasVotedSet.add(voterId);

                    String voteData = voterId + ":" + candidate;
                    currentBatch.add(voteData);
                    System.out.println(">> Vote securely buffered for " + candidate + "! (" + currentBatch.size() + " vote(s) waiting to be sealed)");
                    break;

                case "2":
                    if (currentBatch.isEmpty()) {
                        System.out.println(">> No votes in the current batch to seal.");
                    } else {
                        System.out.println(">> Sealing batch of " + currentBatch.size() + " vote(s) into the blockchain...");
                        Block newBlock = new Block(currentBatch, electionChain.getLatestBlock().hash);
                        electionChain.addBlock(newBlock);

                        currentBatch.clear();
                        System.out.println(">> Block successfully added! Merkle Root recorded.");
                    }
                    break;

                case "3":
                    System.out.println(">> Running cryptographic audit on all blocks...");
                    if (electionChain.isChainValid()) {
                        System.out.println(">> STATUS: SECURE. No tampering detected.");
                    } else {
                        System.out.println(">> STATUS: ALERT! Election data has been tampered with!");
                    }
                    break;

                case "4":
                    if (electionChain.chain.size() <= 1) {
                        System.out.println(">> No sealed blocks available to tamper with yet! Seal a batch first.");
                        break;
                    }

                    System.out.println("\n--- HACKER TERMINAL ---");
                    System.out.println("Available blocks to hack: 1 to " + (electionChain.chain.size() - 1));
                    System.out.print("Select Block Number to alter: ");

                    int blockIndex;
                    try {
                        blockIndex = Integer.parseInt(scanner.nextLine());
                        if (blockIndex < 1 || blockIndex >= electionChain.chain.size()) throw new Exception();
                    } catch (Exception e) {
                        System.out.println(">> Invalid block selection.");
                        break;
                    }
                    Block hackedBlock = electionChain.chain.get(blockIndex);
                    System.out.println("Votes in Block " + blockIndex + ":");
                    for (int i = 0; i < hackedBlock.votes.size(); i++) {
                        System.out.println(i + ": " + hackedBlock.votes.get(i));
                    }
                    System.out.print("Select Vote Index to alter (0 to " + (hackedBlock.votes.size() - 1) + "): ");
                    int voteIndex;
                    try {
                        voteIndex = Integer.parseInt(scanner.nextLine());
                        if (voteIndex < 0 || voteIndex >= hackedBlock.votes.size()) throw new Exception();
                    } catch (Exception e) {
                        System.out.println(">> Invalid vote index.");
                        break;
                    }
                    System.out.print("Enter the new tampered vote data (e.g., Hacker_ID:Candidate_X): ");
                    String fakeData = scanner.nextLine();
                    hackedBlock.votes.set(voteIndex, fakeData);
                    MerkleTree tamperedTree = new MerkleTree(hackedBlock.votes);
                    hackedBlock.merkleRoot = tamperedTree.getRoot();
                    System.out.println(">> HACK SUCCESSFUL: Data overwritten and Merkle Root recalculated.");
                    System.out.println(">> Run 'Verify Election Integrity' (Option 3) to see if the Blockchain catches it!");
                    break;
                case "5":
                    System.out.println(">> Shutting down election system...");
                    if (!currentBatch.isEmpty()) {
                        System.out.println(">> Warning: " + currentBatch.size() + " unsealed vote(s) were discarded.");
                    }
                    isVotingOpen = false;
                    break;
                default:
                    System.out.println(">> Invalid option. Please select 1-5.");
            }
        }
        scanner.close();
    }
}