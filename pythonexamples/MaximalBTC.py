import os
from blockchain_parser.blockchain import Blockchain

# The distribution of the maximal balance of BTC’s ever seen per address


def main():
    blockchain = Blockchain(os.path.expanduser('/media/varun/DATA/Courses/Capstone/PyBC/pybit/Blocks'))  #D:\Courses\Capstone\PyBC\pybit\Blocks
    max_btc_per_address = {}

    for block in blockchain.get_unordered_blocks():
        for tx in block.transactions:
            for output in tx.outputs:
                address = output.addresses
                if len(address) != 0:
                    if address[0] in max_btc_per_address.keys():
                        if max_btc_per_address[address[0]] < output.value:
                            max_btc_per_address[address[0]] = output.value
                    else:
                        max_btc_per_address[address[0]] = output.value

    analyze = {}
    analyze["0-0.1"] = 0
    analyze["0.1-10"] = 0
    analyze["10-100"] = 0
    analyze["100-1000"] = 0
    analyze["1000-10000"] = 0
    analyze["10000-100000"] = 0
    analyze[">=100000"] = 0

    for address in max_btc_per_address.keys():
        btc_value = max_btc_per_address[address] / 100000000
        if btc_value < 0.1:
            analyze["0-0.1"] += 1
        elif btc_value >= 1 and btc_value < 10:
            analyze["0.1-10"] += 1
        elif btc_value >= 10 and btc_value < 100:
            analyze["10-100"] += 1
        elif btc_value >= 100 and btc_value < 1000:
            analyze["100-1000"] += 1
        elif btc_value >= 1000 and btc_value < 10000:
            analyze["1000-10000"] += 1
        elif btc_value >= 10000 and btc_value < 100000:
            analyze["10000-100000"] += 1
        elif btc_value >= 100000:
            analyze[">=100000"] += 1
    print(analyze)

if __name__ == '__main__':
    main()

