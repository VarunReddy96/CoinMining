import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.utils.BlockFileLoader;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BitcoinGlobalProperties {

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
//        Sha256Hash addresshash = Sha256Hash.hash()
        HashMap<String,Coin> amountaccumulatedperaddress = new HashMap<>();
        HashSet<Sha256Hash> txfeedup = new HashSet<>(); // to check if the fee can be determined
        HashMap<String,Integer> amountcategories = new HashMap<>();
        amountcategories.put("0-1",0);
        amountcategories.put("1-10",0);
        amountcategories.put("10-100",0);
        amountcategories.put("100-1000",0);
        amountcategories.put("1000-10000",0);
        amountcategories.put(">=10000",0);

        for(Block b: loader){
            List<Transaction> tx = b.getTransactions();
            for(Transaction t: tx){
                if(!txfeedup.contains(t.getTxId())) {
                    System.out.println("tx: "+ t.getTxId());
                    List<TransactionInput> in = t.getInputs();
//                    Coin inputsum = Coin.ZERO;
//                    for(TransactionInput ti: in){
//                        if(ti.getValue()!=null){
//                            inputsum = inputsum.add(ti.getValue());
//                        }
//                    }
                    List<TransactionOutput> out = t.getOutputs();
                    for(TransactionOutput to: out){
                        try {
                            String pubkey = new String(to.getScriptPubKey().getPubKeyHash(), StandardCharsets.UTF_8);
                            if (!amountaccumulatedperaddress.containsKey(pubkey)) {
                                if (to.getValue() != null) {
                                    amountaccumulatedperaddress.put(pubkey, to.getValue());
                                }
                            } else {
                                if (to.getValue() != null) {
                                    amountaccumulatedperaddress.put(pubkey,
                                            amountaccumulatedperaddress.get(pubkey).add(to.getValue()));
                                }
                            }
                        }catch (Exception e){
                            continue;
                        }
                    }


//                    Coin outputsum = Coin.ZERO;
//                    for(TransactionOutput ti: out){
//                        if(ti.getValue()!=null){
//                            outputsum = outputsum.add(ti.getValue());
//                        }
//                    }
                    //long feecalculated = inputsum.getValue() - outputsum.getValue();
                    //System.out.println("Fee calculated per transaction: "+feecalculated * 1.0/100000000);
//                    long feepertransaction = t.getInputSum().getValue()-t.getOutputSum().getValue(); // This value is in satoshis
//                    System.out.println("Fee per transaction: " + feepertransaction * 1.0/100000000);
                    txfeedup.add(t.getTxId());

                    //long feevalue = t.getFee().getValue();
                    //int

//                    if (!txfeedup.contains(t.getTxId())) {
//                        txfeedup.add(t.getTxId());
//                        if (t.getFee() == null) {
//                            txfeedup.put(t.getTxId(), "null. Cannot be determined");
//                        } else {
//                            txfeedup.put(t.getTxId(), "not null. " + t.getFee().getValue());
//                        }
//                    }
                }
            }

        }
        System.out.println("The total transactions are: "+ txfeedup.size());

        for(String s: amountaccumulatedperaddress.keySet()){
            Coin value = amountaccumulatedperaddress.get(s);
            if(value.getValue() * 1.0 /100000000 < 1){
                amountcategories.put("0-1",amountcategories.get("0-1")+1);
            }else if(value.getValue() * 1.0 /100000000 >=1 && value.getValue() * 1.0 /100000000 < 10){
                amountcategories.put("1-10",amountcategories.get("1-10")+1);
            }else if(value.getValue() * 1.0 /100000000 >=10 && value.getValue() * 1.0 /100000000 <100){
                amountcategories.put("10-100",amountcategories.get("10-100")+ 1);
            }else if(value.getValue() * 1.0 /100000000 >=100 && value.getValue() * 1.0 /100000000 < 1000){
                amountcategories.put("100-1000",amountcategories.get("100-1000") + 1);
            }else if(value.getValue() * 1.0 /100000000 >= 1000 && value.getValue() * 1.0 /100000000 < 10000){
                amountcategories.put("1000-10000",amountcategories.get("1000-10000") + 1);
            }else{
                amountcategories.put(">=10000",amountcategories.get(">=10000") + 1);
            }

        }
        System.out.println("Total number of addresses: "+amountaccumulatedperaddress.size());
        System.out.println("Amount categories are:-----------------------------------------");
        System.out.println(amountcategories);

//        for(Sha256Hash tx: txfee.keySet()){
//            System.out.println("tx: "+ tx);
//            System.out.println("fee: "+txfee.get(tx)/100000000); // 1 bitcoin = 100000000 satoshis
//            System.out.println("feevalue through bitcoinj api: "+txfeedup.get(tx));
//        }
    }

    public static void main(String[] args) {
        BitcoinGlobalProperties wa = new BitcoinGlobalProperties();
        wa.analyzewallets();
    }
}
