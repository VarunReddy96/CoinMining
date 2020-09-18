import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.utils.BlockFileLoader;


public class BitcoinAnalyzer {

    // Location of block files. This is where your blocks are located.
    // Check the documentation of Bitcoin Core if you are using
    // it, or use any other directory with blk*dat files.
    static String PREFIX = "D:\\Bitcoin data\\Blockdata_testing";

    // A simple method with everything in it
    public void analyzetransactions() {

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

        for (Block block : loader) {

            blockCounter++;
            System.out.println("Analysing block " + blockCounter);

            // Extract the day from the block
            String day = new SimpleDateFormat("yyyy-MM-dd").format(block.getTime());

            // Now we start populating the map day -> number of transactions.
            if (!dailyTotTxs.containsKey(day)) {
                dailyTotTxs.put(day, 0);
            }

            // The following is highly inefficient.do
            // block.getTransactions().size()
            for (Transaction tx : block.getTransactions()) {
                dailyTotTxs.put(day, dailyTotTxs.get(day) + 1);
            }
        } // End of iteration over blocks

        // Finally, let's print the results
        for (String d : dailyTotTxs.keySet()) {
            System.out.println(d + "," + dailyTotTxs.get(d));
        }
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


    // Main method: simply invoke everything
    public static void main(String[] args) {
        BitcoinAnalyzer tb = new BitcoinAnalyzer();
        tb.analyzetransactions();

        // Arm the blockchain file loader.
        NetworkParameters np = new MainNetParams();
        List<File> blockChainFiles = new ArrayList<File>();
        blockChainFiles.add(new File("D:\\Bitcoin data\\Blockdata_testing\\blk00000.dat"));
        BlockFileLoader bfl = new BlockFileLoader(np, blockChainFiles);

// Data structures to keep the statistics.
        Map<String, Integer> monthlyTxCount = new HashMap<String, Integer>();
        Map<String, Integer> monthlyBlockCount = new HashMap<String, Integer>();

        // Iterate over the blocks in the dataset.
        for (Block block : bfl) {

            // Extract the month keyword.
            String month = new SimpleDateFormat("yyyy-MM").format(block.getTime());

            // Make sure there exists an entry for the extracted month.
            if (!monthlyBlockCount.containsKey(month)) {
                monthlyBlockCount.put(month, 0);
                monthlyTxCount.put(month, 0);
            }

            // Update the statistics.
            monthlyBlockCount.put(month, 1 + monthlyBlockCount.get(month));
            monthlyTxCount.put(month, block.getTransactions().size() + monthlyTxCount.get(month));

        }

        // Compute the average number of transactions per block per month.
        Map<String, Float> monthlyAvgTxCountPerBlock = new HashMap<String, Float>();
        for (String month : monthlyBlockCount.keySet())
            monthlyAvgTxCountPerBlock.put(
                    month, (float) monthlyTxCount.get(month) / monthlyBlockCount.get(month));

        System.out.println(monthlyAvgTxCountPerBlock);

    }

}