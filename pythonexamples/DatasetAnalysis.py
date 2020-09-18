import os
from blockchain_parser.blockchain import Blockchain

# an overview of the dataset used


def main():
    blockchain = Blockchain(os.path.expanduser('/media/varun/DATA/Courses/Capstone/PyBC/pybit/Blocks'))  #D:\Courses\Capstone\PyBC\pybit\Blocks
    timelist = []
    n_transactions = {}
    n_transactions_count = 0
    n_col_transactions = []  # transactions with the same txid
    block_counter = 0
    tx_per_block = {}
    for block in blockchain.get_unordered_blocks():
        block_counter+=1
        timelist.append(block.header.timestamp)
        tx_counter = 0
        for tx in block.transactions:
            n_transactions_count+=1
            tx_counter+=1
            if tx.txid in n_transactions.keys():
                n_transactions[tx.txid].append(block.header.timestamp)  #adding timestamp to the
                n_col_transactions.append(tx.txid)

            else:
                n_transactions[tx.txid] = []
                n_transactions[tx.txid].append(block.header.timestamp)

            # for no, output in enumerate(tx.outputs):
            #     print("tx=%s outputno=%d type=%s value=%s" % (tx.hash, no, output.type, output.value))
        tx_per_block[block.hash] = tx_counter

    print("the start date: ",min(timelist),"the end date",max(timelist),"of the datset")
    print("Total number of Transactions:",n_transactions_count)
    for txid in n_col_transactions:
        print("Same hash collision hash:", txid, "at time",n_transactions[txid], "boolean segwit: ",
        tx.is_segwit, "boolean isCoinbase:", tx.is_coinbase())

    print("total number of blocks in the datset are:",block_counter)
    max_transaction_inblock = max(tx_per_block,key= tx_per_block.get)
    print("The maximum transactions in a block are: ",tx_per_block[max_transaction_inblock],"in block",max_transaction_inblock)
    min_transaction_inblock = min(tx_per_block,key= tx_per_block.get)
    print("The minimum transactions in a block are: ", tx_per_block[min_transaction_inblock], "in block",
          min_transaction_inblock)



if __name__ == '__main__':
    main()