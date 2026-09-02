import java.util.ArrayList;
import java.util.List;

public class MerkleTree {
    private String root;
    private List<String> leaves;

    public MerkleTree(List<String> votes) {
        this.leaves = new ArrayList<>();
        for (String vote : votes) {
            this.leaves.add(StringUtil.applySha256(vote));
        }
        this.root = buildTree(this.leaves);
    }

    private String buildTree(List<String> nodes) {
        if (nodes.size() == 1) {
            return nodes.get(0);
        }

        List<String> nextLayer = new ArrayList<>();

        for (int i = 0; i < nodes.size(); i += 2) {
            String left = nodes.get(i);
            String right = (i + 1 < nodes.size()) ? nodes.get(i + 1) : left;

            String combinedHash = StringUtil.applySha256(left + right);
            nextLayer.add(combinedHash);
        }

        return buildTree(nextLayer);
    }

    public String getRoot() {
        return this.root;
    }
}