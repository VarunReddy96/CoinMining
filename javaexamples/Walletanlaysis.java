import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.utils.BlockFileLoader;

public class Walletanlaysis {
    static String PREFIX = "D:\\Bitcoin data\\Blockdata_testing";

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

    public void analyzewallets(){
        // Just some initial setup
        NetworkParameters np = new MainNetParams();
        Context.getOrCreate(MainNetParams.get());

        // We create a BlockFileLoader object by passing a list of files.
        BlockFileLoader loader = new BlockFileLoader(np, buildList(PREFIX));
        HashMap<Sha256Hash,Long> txfee = new HashMap<>();
        HashMap<Sha256Hash,String> txfeedup = new HashMap<>(); // to check if the fee can be determined

        Sha256Hash txid= null;
        int blockcounter = 0;
        long maxbitcoins = Long.MIN_VALUE;
        HashMap<String,Integer> txfeecount = new HashMap<>();
        txfeecount.put("Zero",0);
        txfeecount.put("<0.25",0);
        txfeecount.put("<0.5",0);
        txfeecount.put("<0.75",0);
        txfeecount.put("<1",0);
        txfeecount.put(">=1",0);

        for(Block b: loader){
            List<Transaction> tx = b.getTransactions();
            for(Transaction t: tx){
                if(!txfee.containsKey(t.getTxId())) {
                    System.out.println("tx: "+ t.getTxId());
                    List<TransactionInput> in = t.getInputs();
                    Coin inputsum = Coin.ZERO;
                    for(TransactionInput ti: in){
                        if(ti.getValue()!=null){
                        inputsum = inputsum.add(ti.getValue());
                        }
                    }
                    List<TransactionOutput> out = t.getOutputs();
                    Coin outputsum = Coin.ZERO;
                    for(TransactionOutput ti: out){
                        if(ti.getValue()!=null){
                        outputsum = outputsum.add(ti.getValue());
                        }
                    }
                    long feecalculated =  outputsum.getValue() - inputsum.getValue();
                    System.out.println("Fee calculated per transaction: "+feecalculated * 1.0/100000000);
                    long feepertransaction = t.getOutputSum().getValue() - t.getInputSum().getValue(); // This value is in satoshis
                    System.out.println("Fee per transaction: " + feepertransaction * 1.0/100000000);
                    txfee.put(t.getTxId(),feepertransaction);
                    if(blockcounter==0){
                        txid = t.getTxId();
                    }else{
                        if(maxbitcoins < feecalculated){
                            maxbitcoins = feecalculated;
                            txid = t.getTxId();
                        }
                    }
                    blockcounter++;
                    //long feevalue = t.getFee().getValue();
                    //int

//                    if (!txfee.containsKey(t.getTxId())) {
//                        txfee.put(t.getTxId(), feepertransaction);
//                        if (t.getFee() == null) {
//                            txfeedup.put(t.getTxId(), "null. Cannot be determined");
//                        } else {
//                            txfeedup.put(t.getTxId(), "not null. " + t.getFee().getValue());
//                        }
//                    }
                }
            }

        }
        System.out.println("The total transactions are: "+ txfee.size());
        System.out.println("The largest transaction id: "+txid);
        System.out.println("The max bitcoins are: "+maxbitcoins/100000000);


//        for(Sha256Hash tx: txfee.keySet()){
//            System.out.println("tx: "+ tx);
//            System.out.println("fee: "+txfee.get(tx)/100000000); // 1 bitcoin = 100000000 satoshis
//            System.out.println("feevalue through bitcoinj api: "+txfeedup.get(tx));
//        }
    }

    public static void main(String[] args) {
        Walletanlaysis wa = new Walletanlaysis();
        wa.analyzewallets();
    }
}
