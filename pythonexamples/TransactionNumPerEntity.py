import os
from blockchain_parser.blockchain import Blockchain

# The distribution of the number of transactions per address

def main():
    blockchain = Blockchain(os.path.expanduser('/media/varun/DATA/Courses/Capstone/PyBC/pybit/Blocks'))  #D:\Courses\Capstone\PyBC\pybit\Blocks
    trans_per_address = {}
    same_transaction_counter = 0

    for block in blockchain.get_unordered_blocks():
        for tx in block.transactions:
            for output in tx.outputs:
                address = output.addresses
                if len(address) != 0:
                    if address[0] in trans_per_address.keys():
                        same_transaction_counter+=1
                        trans_per_address[address[0].hash] += 1
                    else:
                        trans_per_address[address[0].hash] = 1

    analyze = {}
    analyze["1-2"] = 0
    analyze["2-4"] = 0
    analyze["4-10"] = 0
    analyze["10-100"] = 0
    analyze["100-1000"] = 0
    analyze["1000-10000"] = 0
    analyze["10000-100000"] = 0
    analyze[">=100000"] = 0

    for address in trans_per_address.keys():
        trans_value = trans_per_address[address]
        if trans_value < 2:
            analyze["1-2"] += 1
        elif trans_value >= 2 and trans_value < 4:
            analyze["2-4"] += 1
        elif trans_value >= 4 and trans_value < 10:
            analyze["4-10"] += 1
        elif trans_value >= 10 and trans_value < 100:
            analyze["10-100"] += 1
        elif trans_value >= 100 and trans_value < 1000:
            analyze["100-1000"] += 1
        elif trans_value >= 1000 and trans_value < 10000:
            analyze["1000-10000"] += 1
        elif trans_value >= 10000 and trans_value < 100000:
            analyze["10000-100000"] += 1
        elif trans_value >= 100000:
            analyze[">=100000"] += 1
    print(analyze)
    print(same_transaction_counter)


if __name__ == '__main__':
    main()

