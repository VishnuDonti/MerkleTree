package com.versionmanager.merkle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MerkleTreeHelper {

    private MerkleTreeHelper() {
    }

    public static String getSha256(String hash1, String hash2) {
        return getSha256(hash1 + hash2);
    }

    public static String getSha256(String originalString) {
        return DigestUtils.sha256Hex(originalString);
    }

    // `this method converts given input data to Node structure
    public static Node getNode(MerleTreeTester.InputNode data) {
        Node node = new Node();
        node.setLeaf(true);
        node.setResourceId(data.getId());
        node.setLevel((short) 0);
        if(data.isDeleted()) {
            Deleted deleted = new Deleted();
            deleted.setDeleted(data.isDeleted());
            List<Deleted> deletedList = new ArrayList<>(1);
            deletedList.add(deleted);
            node.setDeletedList(deletedList);
        }
        try {
            Hash hash = new Hash();
            hash.setHashValue(getSha256(new ObjectMapper().writeValueAsString(node)));
            List<Hash> hashesList = new ArrayList<>(1);
            hashesList.add(hash);
            node.setHashList(hashesList);
        } catch (JsonProcessingException e) {
            // can be logged
        }
        return node;
    }

    public static MerkleTree constructTree(List<Node> leafs) {
        MerkleTree merkleTree = new MerkleTree();
        //1. add start and end of the leafs using index
        if (leafs != null && leafs.size() > 0) {
            if (merkleTree.getStart() == null) {
                merkleTree.setStart(leafs.get(0));
            }
            if (merkleTree.getEnd() == null) {
                merkleTree.setEnd(leafs.get(leafs.size() - 1));
            }
        }
        //1. ended
        // 2. for the leaves construct parents top to bottom
        constructParents(leafs, merkleTree);
        //2. ended
        merkleTree.setLeaves(leafs);
        merkleTree.setOdd(leafs.size() % 2 != 0);
        return merkleTree;
    }

    private static void constructParents(List<Node> leafs, MerkleTree merkleTree) {
        //1. base condition if input leaves is null or empty
        if (leafs == null || leafs.isEmpty()) {
            return;
        }
        //1. ended
        //2. Collect intermediate nodes that gets formed when
        // constructing each level in top to bottom approach
        List<Node> intermediateNodes = new ArrayList<>(leafs.size() / 2);
        // If supplied leaves is root then add it as a root and return
        if (leafs.size() == 1) {
            merkleTree.setRoot(new ArrayList<>(leafs));
            return;
        }
        // 2. ended
        // 3. iterate over leaves to create nodes for the above level
        for (int i = 0; i < leafs.size(); i += 2) {
            // If number of given leaves is odd consider the right node as null
            // and create upper node same as left node
            Node node = getNewRoot(leafs.get(i), i + 1 >= leafs.size() ? null : leafs.get(i + 1), 0);
            intermediateNodes.add(node);
        }
        // 3. ended
        constructParents(intermediateNodes, merkleTree);
    }


    //
    public static void updateTree(Node leaf, MerkleTree merkleTree) {
        leaf.getHashList().get(0).setRootIndex(merkleTree.getRoot().size());
        if(leaf.getDeletedList() != null) {
            leaf.getDeletedList().get(0).setRootIndex(merkleTree.getRoot().size());
        }
        int index = getIndex(leaf, merkleTree);
        if (index == -1) {
            if (leaf.getDeletedList() != null && leaf.getDeletedList().get(0).isDeleted()) {
                return;
            }
            addLeaf(leaf, merkleTree);
        } else {
            // If already deleted why delete again
            Deleted deleted = findLastElementInList(merkleTree.getLeaves().get(index).getDeletedList());
            if (leaf.getDeletedList() != null && leaf.getDeletedList().get(0).isDeleted() && deleted != null && deleted.isDeleted()) {
                return;
            }
            updateLeaf(index, leaf, merkleTree);
        }
    }

    public static Difference getUpdates(MerkleTree merkleTree, String version) {
        // 1. Check given version is present
        List<Integer> hashCodes = new ArrayList<>(merkleTree.getRoot().size());
        Predicate<Node> hashCodeFilter = x -> {
            if (hashCodes.contains(x.hashCode())) {
                return false;
            }
            hashCodes.add(x.hashCode());
            return true;
        };
        Optional<Hash> hash = merkleTree.getRoot().stream().filter(hashCodeFilter).flatMap(y -> y.getHashList().stream()).filter(x -> x.getHashValue().equals(version)).findFirst();
        hashCodes.clear();
        // 1. ended

        // 2. check if hash is present
        if (hash.isPresent()) {
            // Get the difference between given version and current version
            return diff(merkleTree.getRoot().get(hash.get().getRootIndex()), findLastElementInList(merkleTree.getRoot()), merkleTree.getLeaves(), hash.get().getRootIndex());
        }
        // 2. ended

        // 3. If hash is not present return all the leaves that are not deleted.
        Difference difference = new Difference();
        difference.setUpdated(merkleTree.getLeaves().stream().filter(m -> {
            Deleted deletedStage = findLastElementInList(m.getDeletedList());
            if(deletedStage != null) {
               return  deletedStage.isDeleted();
            }
            return true;
        }).map(Node::getResourceId).collect(Collectors.toList()));
        return difference;
    }

    private static Difference diff(Node givenVersion, Node currentVersion, List<Node> leaves, int givenRootIndex) {
        List<String> updatedNodeIds = new ArrayList<>();
        if (currentVersion.getLevel() > givenVersion.getLevel()) {
            double nodesAtGivenLevel = Math.pow(2, givenVersion.getLevel());
            IntStream.range((int) nodesAtGivenLevel, leaves.size()).forEach(index -> updatedNodeIds.add(leaves.get(index).getResourceId()));
        }
        int diffOfLevels = currentVersion.getLevel() - givenVersion.getLevel();
        Node leftNodeInCurrentNodeVersion = currentVersion;
        while (diffOfLevels > 0) {
            leftNodeInCurrentNodeVersion = leftNodeInCurrentNodeVersion.getLeft();
            diffOfLevels--;
        }
        List<String> deletdNodeIds = new ArrayList<>();
        compareCopiesOfTwoTrees(leftNodeInCurrentNodeVersion, givenVersion, givenRootIndex, updatedNodeIds, deletdNodeIds);
        Difference difference = new Difference();
        difference.setUpdated(updatedNodeIds);
        difference.setDeleted(deletdNodeIds);
        return difference;
    }

    private static void compareCopiesOfTwoTrees(Node leftNodeInCurrentNodeVersion, Node givenVersion, int givenRootIndex, List<String> updateNodeIds, List<String> deletdNodeIds) {
        traverseTillEndCompare(leftNodeInCurrentNodeVersion, givenVersion, givenRootIndex, updateNodeIds, deletdNodeIds);
    }

    private static void traverseTillEndCompare(Node leftNodeInCurrentNodeVersion, Node givenVersion, int givenRootIndex, List<String> updateNodeIds, List<String> deletdNodeIds) {
        if (leftNodeInCurrentNodeVersion == null || givenVersion == null) {
            return;
        }
        int givenVersionIndex = getGivenVersionIndexOrPrevious(givenVersion, givenRootIndex);
        if (givenVersionIndex == -1) {
            if (leftNodeInCurrentNodeVersion.isLeaf()) {
                updateNodeIds.add(leftNodeInCurrentNodeVersion.getResourceId());
            } else {
                findAllLeavesOfNode(leftNodeInCurrentNodeVersion, updateNodeIds);
            }
            return;
        }
        if (givenVersion.getHashList().get(givenVersionIndex).equals(findLastElementInList(leftNodeInCurrentNodeVersion.getHashList()))) {
            return;
        }
        if (leftNodeInCurrentNodeVersion.isLeaf() || givenVersion.isLeaf()) {
            if (leftNodeInCurrentNodeVersion.getDeletedList() == null || !findLastElementInList(leftNodeInCurrentNodeVersion.getDeletedList()).isDeleted()) {
                updateNodeIds.add(leftNodeInCurrentNodeVersion.getResourceId());
            } else {
                deletdNodeIds.add(leftNodeInCurrentNodeVersion.getResourceId());
            }
        }
        traverseTillEndCompare(leftNodeInCurrentNodeVersion.getLeft(), givenVersion.getLeft(), givenRootIndex, updateNodeIds, deletdNodeIds);
        traverseTillEndCompare(leftNodeInCurrentNodeVersion.getRight(), givenVersion.getRight(), givenRootIndex, updateNodeIds, deletdNodeIds);
    }

    private static void findAllLeavesOfNode(Node leftNodeInCurrentNodeVersion, List<String> updateNodeIds) {
        if (leftNodeInCurrentNodeVersion == null) {
            return;
        }
        if (leftNodeInCurrentNodeVersion.isLeaf() && leftNodeInCurrentNodeVersion.getDeletedList() != null && !findLastElementInList(leftNodeInCurrentNodeVersion.getDeletedList()).isDeleted()) {
            updateNodeIds.add(leftNodeInCurrentNodeVersion.getResourceId());
        }
        findAllLeavesOfNode(leftNodeInCurrentNodeVersion.getLeft(), updateNodeIds);
        findAllLeavesOfNode(leftNodeInCurrentNodeVersion.getRight(), updateNodeIds);
    }

    private static int getGivenVersionIndexOrPrevious(Node inputNode, int expectedVersion) {
        int resultIndex = 0;
        for (int i = 0; i < inputNode.getHashList().size(); i++) {
            if (inputNode.getHashList().get(i).getRootIndex() == expectedVersion) {
                resultIndex = i;
                break;
            }
            if (inputNode.getHashList().get(i).getRootIndex() > expectedVersion) {
                resultIndex = i - 1;
                break;
            }
        }

        return resultIndex;
    }

    private static void updateLeaf(int index, Node leaf, MerkleTree merkleTree) {
        String hashValue = findLastElementInList(leaf.getHashList()).getHashValue();
        boolean isDeleted = leaf.getDeletedList() != null ? findLastElementInList(leaf.getDeletedList()).isDeleted() : false;
        Hash hash = new Hash();
        hash.setRootIndex(merkleTree.getRoot().size());
        hash.setHashValue(hashValue);
        List<Node> leavesList = merkleTree.getLeaves();
        Node node = leavesList.get(index);
        node.getHashList().add(hash);
        if (isDeleted || (node.getDeletedList() != null &&findLastElementInList(node.getDeletedList()).isDeleted())) {
            if(node.getDeletedList() != null && findLastElementInList(node.getDeletedList()).isDeleted() && (leaf.getDeletedList() == null || leaf.getDeletedList().isEmpty())) {
                Deleted deleted = new Deleted();
                deleted.setRootIndex(merkleTree.getRoot().size());
                deleted.setDeleted(false);
                List<Deleted> deletedList = new ArrayList<>(1);
                deletedList.add(deleted);
                leaf.setDeletedList(deletedList);
            }
            if(node.getDeletedList() == null) {
                node.setDeletedList(new ArrayList<>(1));
            }
            node.getDeletedList().add(leaf.getDeletedList().get(0));
        }
        Node parentNode = node.getParent();
        while (parentNode != null) {
            parentNode.getHashList().add(getHash(getLatestHashValue(parentNode), getLatestHashValue(parentNode), merkleTree.getRoot().size()));
            parentNode = parentNode.getParent();
        }
        merkleTree.getRoot().add(findLastElementInList(merkleTree.getRoot()));
    }

    private static void addLeaf(Node leaf, MerkleTree merkleTree) {
        Node newRoot = null;
        // 1. Started
        // get the latest root find the level
        // get the latest leaves version find the size.
        Node latestRoot = findLastElementInList(merkleTree.getRoot());
        int currentLevel = latestRoot.getLevel();

        List<Node> lastVersionLeaves = merkleTree.getLeaves();
        int currentSize = lastVersionLeaves.size();
        // 1. ended

        // 2. Started
        // For the latest root check max leaves that can be added
        // If level is 3 max leaves size should be 8
        // If leaves size is less than  8 then no need to increase height or else increase the height
        int maxLeavesForCurrentLevel = (int) Math.pow(2, currentLevel);
        if (currentSize == maxLeavesForCurrentLevel) {
            newRoot = addLevelToTree(leaf, merkleTree, (short) currentLevel);
        } else {
            newRoot = addNewHashToExistingRoot(leaf, merkleTree);
        }
        //2. ended

        // 3. started
        // Since we have to maintain leaves list for each version ,
        // create a new list version with existing objects and add the leaf
        merkleTree.getLeaves().add(leaf);
        merkleTree.setEnd(leaf);
        //3. ended
        if (newRoot != null) {
            merkleTree.getRoot().add(newRoot);
        }
    }

    private static Node addNewHashToExistingRoot(Node leaf, MerkleTree merkleTree) {
        // 1. started
        // check if end leaf parent right slot is available
        // If available add the leaf to the right and keep it.
        Node endNodeParent = merkleTree.getEnd().getParent();
        if (endNodeParent.getRight() == null) {
            endNodeParent.setRight(leaf);
            leaf.setParent(endNodeParent);
            while (endNodeParent != null) {
                endNodeParent.getHashList().add(getHash(getLatestHashValue(endNodeParent.getLeft()), getLatestHashValue(endNodeParent.getRight() == null ? endNodeParent.getLeft() : endNodeParent.getRight()), merkleTree.getRoot().size()));
                endNodeParent = endNodeParent.getParent();
            }
        } else {
            Node firstParentWhoseChildIsRight = findParentWhoseRightChildIsNull(endNodeParent);
            Node constructedRightNode = increaseHeight((short) (firstParentWhoseChildIsRight.getLevel() - 1), leaf, merkleTree.getRoot().size());
            constructedRightNode.setParent(firstParentWhoseChildIsRight);
            firstParentWhoseChildIsRight.setRight(constructedRightNode);
            while (firstParentWhoseChildIsRight != null) {
                firstParentWhoseChildIsRight.getHashList().add(getHash(getLatestHashValue(firstParentWhoseChildIsRight.getLeft()), getLatestHashValue(firstParentWhoseChildIsRight.getRight() == null ? firstParentWhoseChildIsRight.getLeft() : firstParentWhoseChildIsRight.getRight()), merkleTree.getRoot().size()));
                firstParentWhoseChildIsRight = firstParentWhoseChildIsRight.getParent();
            }
        }
        return findLastElementInList(merkleTree.getRoot());
    }

    private static Node findParentWhoseRightChildIsNull(Node end) {
        while (end != null && end.getRight() != null) {
            end = end.getParent();
        }
        return end;
    }

    private static String getLatestHashValue(Node parent) {
        return findLastElementInList(parent.getHashList()).getHashValue();
    }

    private static Hash getHash(String leftHash, String rightHash, int rootIndex) {
        Hash hash = new Hash();
        hash.setHashValue(getSha256(leftHash, rightHash));
        hash.setRootIndex(rootIndex);
        return hash;
    }

    private static Node addLevelToTree(Node leaf, MerkleTree merkleTree, short currentLevel) {
        Node resultRightNode = increaseHeight(currentLevel, leaf, merkleTree.getRoot().size());
        return getNewRoot(findLastElementInList(merkleTree.getRoot()), resultRightNode, merkleTree.getRoot().size());
    }

    private static <T> T findLastElementInList(List<T> list) {
        return list == null || list.isEmpty() ? null : list.get(list.size() - 1);
    }

    private static Node getNewRoot(Node left, Node right, int rootIndex) {
        // 1. started
        // Create a new instance of node add left and right
        // left and right setParents
        Node newRoot = new Node();
        newRoot.setLeft(left);
        left.setParent(newRoot);
        String rightHash = null;
        if (right != null) {
            newRoot.setRight(right);
            right.setParent(newRoot);
            rightHash = right.getHashList().get(0).getHashValue();
        } else {
            rightHash = left.getHashList().get(0).getHashValue();
        }
        //1. ended

        // 2. started
        // Create a new Hash and set it to the root
        Hash hash = new Hash();
        hash.setRootIndex(rootIndex);
        hash.setHashValue(getSha256(left.getHashList().get(0).getHashValue(), rightHash));
        List<Hash> hashList = new ArrayList<>(1);
        hashList.add(hash);
        newRoot.setHashList(hashList);
        // 2. ended

        // 3. started
        // set level
        newRoot.setLevel((short) (left.getLevel() + 1));
        // 3. ended
        return newRoot;
    }

    private static Node increaseHeight(short level, Node node, int rootIndex) {
        while (node.getLevel() < level) {
            Node parentNode = new Node();
            String leftHash = node.getHashList().get(0).getHashValue();
            Hash hash = new Hash();
            hash.setRootIndex(rootIndex);
            hash.setHashValue(getSha256(leftHash, leftHash));
            List<Hash> hashList = new ArrayList<>(1);
            hashList.add(hash);
            parentNode.setHashList(hashList);
            parentNode.setLevel((short) (node.getLevel() + 1));
            parentNode.setLeft(node);
            node.setParent(parentNode);
            node = parentNode;
        }
        return node;
    }


    private static int getIndex(Node leaf, MerkleTree merkleTree) {
        List<Node> nodeList = merkleTree.getLeaves();
        for (int k = 0; k < nodeList.size(); k++) {
            if (nodeList.get(k).getResourceId().equals(leaf.getResourceId())) {
                return k;
            }
        }
        return -1;
    }

}
