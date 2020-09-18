import os
from blockchain_parser.blockchain import Blockchain

def main():
    blockchain = Blockchain(os.path.expanduser('/media/varun/DATA/Courses/Capstone/PyBC/pybit/Blocks'))  #D:\Courses\Capstone\PyBC\pybit\Blocks
    for block in blockchain.get_unordered_blocks():
        for tx in block.transactions:
            for no, output in enumerate(tx.outputs):
                print("tx=%s outputno=%d type=%s value=%s" % (tx.hash, no, output.type, output.value))


if __name__ == '__main__':
    main()