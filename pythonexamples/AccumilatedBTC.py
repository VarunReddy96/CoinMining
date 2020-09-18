import os
from blockchain_parser.blockchain import Blockchain

# The distribution of the accumulated incoming BTC’s per address

def main():
    blockchain = Blockchain(os.path.expanduser('/media/varun/DATA/Courses/Capstone/PyBC/pybit/Blocks'))  #D:\Courses\Capstone\PyBC\pybit\Blocks
    acc_btc_per_address = {}
    script_count = {}
    script_count["p2pk"] = 0
    script_count["p2pkh"] = 0
    script_count["p2ms"] = 0
    script_count["p2sh"] = 0
    for block in blockchain.get_unordered_blocks():
        for tx in block.transactions:
            for output in tx.outputs:
                address = output.addresses
                if output.is_pubkey():
                    script_count["p2pk"]+=1
                elif output.is_pubkeyhash():
                    script_count["p2pkh"]+=1
                elif output.is_multisig():
                    script_count["p2ms"]+=1
                elif output.is_p2sh():
                    script_count["p2sh"]+=1
                else:
                    continue
                if address[0] in acc_btc_per_address.keys():
                    acc_btc_per_address[address[0]] = acc_btc_per_address[address[0]] + output.value
                else:
                    acc_btc_per_address[address[0]] = output.value

    analyze = {}
    analyze["0-1"] = 0
    analyze["1-10"] = 0
    analyze["10-100"] = 0
    analyze["100-1000"] = 0
    analyze["1000-10000"] = 0
    analyze["10000-100000"] = 0
    analyze[">=100000"] = 0

    for address in acc_btc_per_address.keys():
        btc_value = acc_btc_per_address[address] / 100000000
        if btc_value < 1:
            analyze["0-1"]+=1
        elif btc_value >= 1 and btc_value < 10:
            analyze["1-10"] +=1
        elif btc_value >= 10 and btc_value < 100:
            analyze["10-100"] +=1
        elif btc_value >= 100 and btc_value < 1000:
            analyze["100-1000"] +=1
        elif btc_value >= 1000 and btc_value < 10000:
            analyze["1000-10000"] +=1
        elif btc_value >= 10000 and btc_value < 100000:
            analyze["10000-100000"] +=1
        elif btc_value >= 100000:
            analyze[">=100000"] +=1
    print(analyze)

    for script in script_count.keys():
        print("Address type:",script,"count:",script_count[script])

if __name__ == '__main__':
    main()