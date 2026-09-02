import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Block {
    public long timestamp;
    public List<String> votes;
    public String previousHash;
    public String merkleRoot;
    public String hash;

    public Block(List<String> votes, String previousHash) {
        this.votes = new ArrayList<>(votes);
        this.previousHash = previousHash;
        this.timestamp = new Date().getTime();

        MerkleTree tree = new MerkleTree(this.votes);
        this.merkleRoot = tree.getRoot();

        this.hash = calculateBlockHash();
    }

    public String calculateBlockHash() {
        return StringUtil.applySha256(previousHash + Long.toString(timestamp) + merkleRoot);
    }
}