
import org.bitcoinj.core.*;
import org.neo4j.driver.*;
//import org.neo4j.driver.Transaction;
import static org.neo4j.driver.Values.parameters;

import java.io.File;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;

import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.utils.BlockFileLoader;
import org.neo4j.driver.Transaction;

import java.io.File;


public class Importdata implements AutoCloseable {


    static String PREFIX = "D:\\Bitcoin data\\Blockdata_testing";

    private final Driver driver;

    public void close() throws Exception {
        driver.close();
    }

    public void printGreeting(final String message) {
        try (Session session = driver.session()) {
            String greeting = session.writeTransaction(new TransactionWork<String>() {
                @Override
                public String execute(org.neo4j.driver.Transaction tx) {
                    Result result = tx.run("CREATE (a:Greeting) " +
                                    "SET a.message = $message " + "RETURN a.message + ', from node ' + id(a)",
                            parameters("message", message));
                    return result.single().get(0).asString();
                }
            });
            System.out.println(greeting);
        }
    }

    public void createNodes(ArrayList<Block> blocks) {
        try (Session session = driver.session()) {
            int counter = 0;
            //for(Block b:blocks) {

            int ans = session.writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(org.neo4j.driver.Transaction tx) {
                    int counter = 0;
                    for (Block b : blocks) {
                        //System.out.println("Counter: "+counter++);
                        if (counter == 5000) {
                            break;
                        }
                        tx.run("MERGE (block:block {hash:$blockhash}) CREATE (block)-[:coinbase]->(out:output:coinbase {value:$value," +
                                        "index:$outindex})" + //changes made to the outindex
                                        " SET block.size=$size,block.prevblock=$prevblock,block.merkleroot=$merkleroot," +
                                        "block.time=$timestamp,block.bits=$bits,block.nonce=$nonce," +
                                        "block.txcount=$txcount,block.version=$version MERGE (prevblock:block {hash:$prevblock})" +
                                        " MERGE (block)-[:chain]->(prevblock)"
                                , parameters("blockhash", b.getHash().toString(), "size", b.getMessageSize(), "prevblock", b.getPrevBlockHash().toString(), "merkleroot", b.getMerkleRoot().toString(), "timestamp", b.getTime().toString()
                                        , "bits", b.getDifficultyTarget(), "nonce", b.getNonce(), "txcount", b.getTransactions().size(),
                                        "version", b.getVersion(),"value",b.getTransactions().get(0).getOutputSum().toString(),"outindex",
                                        b.getTransactions().get(0).getTxId().toString()
                                ));
                        counter++;
                    }

                    //System.out.println("Out of for loop------------------------");
                    return 1;
                }
            });


            //}
            System.out.println("Written Blocks******************************");
        }
    }

    public void createTransactions(ArrayList<Block> blocks){
        try (Session session = driver.session()) {
            int counter = 0;
            //for(Block b:blocks) {

            int ans = session.writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(org.neo4j.driver.Transaction tx) {
                    int counter = 0;
                    for (Block b : blocks) {
                        //System.out.println("Counter: "+counter++);
                        if (counter == 5000) {
                            break;
                        }
                        int count = 0;
                        List<org.bitcoinj.core.Transaction> txs = b.getTransactions();
                        //System.out.println("Size of transactions: "+txs.size());
                        for(org.bitcoinj.core.Transaction btx:txs) {
                            if (!btx.isCoinBase()) {
                                //System.out.println("Executing count number: " + count);
                                tx.run("MATCH (block :block {hash:$hash}) MERGE (tx:tx {txid:$txid,version:$version,locktime:$locktime}) " +
                                                "MERGE (tx)-[:inc {i:$i}]->(block)"
                                        , parameters("hash", b.getHash().toString(), "txid", btx.getTxId().toString()
                                                , "version", btx.getVersion(), "locktime", btx.getLockTime(), "i", count));
                                count++;

                                for (TransactionInput ti : btx.getInputs()) {
                                    System.out.println("Print connected output: "+ti.getParentTransaction().getHashAsString()+"***"+btx.getTxId().toString());
                                    tx.run("MATCH (tx:tx {txid:$txid}) MERGE (in :output {index: $parenttxid})" +
                                            " MERGE (in)-[:in {vin: $inputvin, scriptSig: $inputscriptSig, sequence: $inputsequence, " +
                                            "witness: $inputwitness}]->(tx)", parameters("txid", btx.getTxId().toString(),
                                            "parenttxid", ti.getOutpoint().getConnectedOutput() == null ? "" : ti.getConnectedOutput().getHash().toString(),
                                            "inputvin", ti.getIndex(), "inputscriptSig", ti.getScriptSig().toString(), "inputsequence", ti.getSequenceNumber()
                                            , "inputwitness", ti.getWitness().toString()));
                                }
                                for (TransactionOutput out : btx.getOutputs()) {
                                    tx.run("MERGE (out :output {index: $outputindex}) MERGE (tx)-[:out {vout: $outputvout}]->(out)" +
                                                    " SET out.value= $outputvalue, out.scriptPubKey= $outputscriptPubKey",
                                            parameters("outputindex", btx.getTxId().toString(), "outputvout", out.getIndex(), "outputvalue"
                                                    , out.getValue().toString(), "outputscriptPubKey", out.getScriptPubKey().toString()));
                                }
                                //System.out.println("Exiting transaction count: " + count);
                            }
                        }
                        //System.out.println("Out of Transaction block number: "+counter+"************************************************");
                        counter++;
                    }

                    System.out.println("Out of for loop------------------------");
                    return 1;
                }
            });


            //}
            System.out.println("Written Transactions******************************");
        }
    }

    public List<String> getPeople() {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                List<String> names = new ArrayList<>();
                Result result = tx.run("MATCH (a:Person) RETURN a.name ORDER BY a.name");
                while (result.hasNext()) {
                    names.add(result.next().get(0).asString());
                }
                return names;
            });
        }
    }

    public void createCoinbase(Transaction t, ArrayList<Block> blocks) {
        int counter = 0;
        for (Block b : blocks) {
            if (counter == 5000) {
                break;
            }

            t.run("MERGE (block:block {hash:$blockhash}) CREATE (block)-[:coinbase]->(out:output:coinbase {value:$value})"
                    , parameters("blockhash", b.getHash().toString(),
                            "value", b.getTransactions().get(0).getOutputSum().toString()));

            counter++;
        }

    }

    public void checkcoinbase(ArrayList<Block> blocks) {
        int counter = 0;
        for (Block b : blocks) {
            if (counter == 5000) break;
            List<org.bitcoinj.core.Transaction> tx = b.getTransactions();
            if (tx.get(0).isCoinBase()) {
                System.out.println("True counter: " + counter);
            }
            counter++;
        }
    }

    public static void main(String... args) throws Exception {
        try (Importdata blockdata = new Importdata("bolt://localhost:7687",
                "neo4j", "n")) {
//            blockdata.printGreeting( "hello, world" );

            ArrayList<Block> blocks = blockdata.analyzetransactions();
            System.out.println("Blocks array size: " + blocks.size());
            //blockdata.checkcoinbase(blocks);
            blockdata.createNodes(blocks);
            blockdata.createTransactions(blocks);
            blockdata.driver.close();
        }
    }


    // A simple method with everything in it
    public ArrayList<Block> analyzetransactions() {

        // Just some initial setup
        NetworkParameters np = new MainNetParams();
        Context.getOrCreate(MainNetParams.get());

        // We create a BlockFileLoader object by passing a list of files.
        BlockFileLoader loader = new BlockFileLoader(np, buildList(PREFIX));

        // We are going to store the results in a map of the form
        // day -> n. of transactions
        Map<String, Integer> dailyTotTxs = new HashMap<String, Integer>();

        // A simple counter to have an idea of the progress
        int blockCounter = 0;
        ArrayList<Block> blocks = new ArrayList<>();

        for (Block block : loader) {

            blockCounter++;
            //System.out.println("Adding block " + blockCounter+" size: "+block.getMessageSize());
            blocks.add(block);

        } // End of iteration over blocks

        return blocks;
    }  // end of doSomething() method.


    // The method returns a list of files in a directory according to a certain
    // pattern (block files have name blkNNNNN.dat)
    private List<File> buildList(String folderName) {
        File folder = new File(folderName);
        List<File> list = new LinkedList<File>();
        list = Arrays.asList(folder.listFiles());
//        for (int i = 0; true; i++) {
//            File file = new File(PREFIX +);
//            if (!file.exists())
//                break;
//            list.add(file);
//        }
//        System.out.println();
        return list;
    }

    public Importdata(String uri, String user, String password) {

        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }


//    public static void main(String[] args) {
////        DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(new File(
////                "D:\\neo4j\\neo4j-community-4.1.0")).build();
////        GraphDatabaseService graphDb = managementService.database(DEFAULT_DATABASE_NAME);
////
////        try (org.neo4j.graphdb.Transaction tx = graphDb.beginTx()) {
////            Node firstNode = tx.createNode();
////            firstNode.setProperty("message", "Hello, ");
////            Node secondNode = tx.createNode();
////            secondNode.setProperty("message", "World!");
////
////            Relationship relationship = firstNode.createRelationshipTo(secondNode, RelationshipTypes.IS_FRIEND_OF);
////            relationship.setProperty("message", "Testing neo4j ");
////
////            ResourceIterator<Node> nodes = tx.findNodes(Label.label("Person"));
////            while (nodes.hasNext()){
////
////                System.out.println(nodes.next().getProperty("name"));
////            }
////        }
////
////        managementService.shutdown();
//
//    }
}




