package com.versionmanager.merkle;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Output;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MerleTreeTester {

    public static void main(String[] args) throws IOException, InterruptedException {
        List<Node> leafs = IntStream.rangeClosed(1, 16000).mapToObj(MerleTreeTester::applyAsInt).collect(Collectors.toList()).stream().map(MerkleTreeHelper::getNode).collect(Collectors.toList());
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        MerkleTree merkleTree = MerkleTreeHelper.constructTree(leafs);
        stopWatch.stop();
        System.out.println("---- Time taken to Construct Tree ----"+stopWatch.getTime() + "---- Time taken to Construct Tree ---");
        stopWatch.reset();
        IntStream.rangeClosed(51, 16800).forEach(x -> MerkleTreeHelper.updateTree(MerkleTreeHelper.getNode(applyAsInt(x)), merkleTree));
        MerkleTreeHelper.updateTree(MerkleTreeHelper.getNode(applyAsInt(30, true)), merkleTree);
        MerkleTreeHelper.updateTree(MerkleTreeHelper.getNode(applyAsInt(3, true)), merkleTree);
        MerkleTreeHelper.updateTree(MerkleTreeHelper.getNode(applyAsInt(3, false)), merkleTree);
        MerkleTreeHelper.updateTree(MerkleTreeHelper.getNode(applyAsInt(61,true)),merkleTree);
        MerkleTreeHelper.updateTree(MerkleTreeHelper.getNode(applyAsInt(1300,true)),merkleTree);
        MerkleTreeHelper.updateTree(MerkleTreeHelper.getNode(applyAsInt(1200,true)),merkleTree);
        MerkleTreeHelper.updateTree(MerkleTreeHelper.getNode(applyAsInt(3200)),merkleTree);
        MerkleTreeHelper.updateTree(MerkleTreeHelper.getNode(applyAsInt(369)),merkleTree);
        MerkleTreeHelper.updateTree(MerkleTreeHelper.getNode(applyAsInt(3780)),merkleTree);
        MerkleTreeHelper.updateTree(MerkleTreeHelper.getNode(applyAsInt(3781)),merkleTree);
        MerkleTreeHelper.updateTree(MerkleTreeHelper.getNode(applyAsInt(3782)),merkleTree);
        MerkleTreeHelper.updateTree(MerkleTreeHelper.getNode(applyAsInt(3783)),merkleTree);
        MerkleTreeHelper.updateTree(MerkleTreeHelper.getNode(applyAsInt(3784)),merkleTree);
        MerkleTreeHelper.updateTree(MerkleTreeHelper.getNode(applyAsInt(3785)),merkleTree);
        MerkleTreeHelper.updateTree(MerkleTreeHelper.getNode(applyAsInt(3786)),merkleTree);
        MerkleTreeHelper.updateTree(MerkleTreeHelper.getNode(applyAsInt(3788)),merkleTree);
        MerkleTreeHelper.updateTree(MerkleTreeHelper.getNode(applyAsInt(3789)),merkleTree);
        IntStream.rangeClosed(3800, 7200).forEach(x -> MerkleTreeHelper.updateTree(MerkleTreeHelper.getNode(applyAsInt(x)),merkleTree));
        IntStream.rangeClosed(7600, 8700).forEach(x -> MerkleTreeHelper.updateTree(MerkleTreeHelper.getNode(applyAsInt(x,true)),merkleTree));
        Kryo kryo = new Kryo();
        kryo.register(MerkleTree.class);
        kryo.register(Node.class);
        kryo.register(ArrayList.class);
        kryo.register(Hash.class);
        kryo.setReferences(true);
        kryo.register(Deleted.class);
        kryo.register(LinkedList.class);

        stopWatch.start();
        Output output = new Output(new FileOutputStream("file.txt"));

        kryo.writeObject(output, merkleTree);
        output.close();
       // byte[] bytes = SerializationUtils.serialize(merkleTree);
        stopWatch.stop();
        System.out.println("---- Time taken to Serialiazation ----"+stopWatch.getTime() + "---- Time taken to Serialization ---");
        stopWatch.reset();
        byte[] bytes = SerializationUtils.serialize(merkleTree);
        stopWatch.start();
        FileUtils.writeByteArrayToFile(new File("ABC"), bytes);
        Thread.sleep(600);
        stopWatch.stop();
        System.out.println("---- Time taken to Write to DB ----"+stopWatch.getTime() + "---- Time taken to Write to DB ---");
        stopWatch.reset();
        stopWatch.start();
        Thread.sleep(600);
        byte[] abcs = FileUtils.readFileToByteArray(new File("ABC"));
        stopWatch.stop();
        System.out.println("---- Time taken to read File   ----"+stopWatch.getTime() + "---- Time taken to read file ---");
        stopWatch.reset();
        stopWatch.start();
        MerkleTree m = (MerkleTree) SerializationUtils.deserialize(abcs);
        stopWatch.stop();
        System.out.println("---- Time taken to Deserialize    ----"+stopWatch.getTime() + "---- Time taken to Deserialize ---");
        stopWatch.reset();
        stopWatch.start();
        Difference difference = MerkleTreeHelper.getUpdates(merkleTree, merkleTree.getRoot().get(1725).getHashList().stream().filter(x -> x.getRootIndex() == 1725).findFirst().orElse(null).getHashValue());
        stopWatch.stop();
        System.out.println(difference.getUpdated().size());
        System.out.println(difference.getDeleted().size());
        System.out.println("---- Time taken to Calculate Diff ----" + stopWatch.getTime() + "---- Time taken to Calculate Diff ---");
    }

    private static InputNode applyAsInt(int x) {
        InputNode inputNode = new InputNode();
        inputNode.setData(UUID.randomUUID().toString());
        inputNode.setId(String.valueOf(x));
        inputNode.setDeleted(false);
        return inputNode;
    }
    private static InputNode applyAsInt(int x, boolean isDeleted) {
        InputNode inputNode = new InputNode();
        inputNode.setData(UUID.randomUUID().toString());
        inputNode.setId(String.valueOf(x));
        inputNode.setDeleted(isDeleted);
        return inputNode;
    }

    public static class InputNode {
        String id;
        String data;
        boolean isDeleted;

        public boolean isDeleted() {
            return isDeleted;
        }

        public void setDeleted(boolean deleted) {
            isDeleted = deleted;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }
}
