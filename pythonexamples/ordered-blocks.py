import sys
import os
from blockchain_parser.blockchain import Blockchain

def main():

    # Instantiate the Blockchain by giving the path to the directory
    # containing the .blk files created by bitcoind
    blockchain = Blockchain(os.path.expanduser('/media/varun/DATA/Courses/Capstone/PyBC/pybit/Blocks'))

    # To get the blocks ordered by height, you need to provide the path of the
    # `index` directory (LevelDB index) being maintained by bitcoind. It contains
    # .ldb files and is present inside the `blocks` directory
    for block in blockchain.get_ordered_blocks(sys.argv[1] + '/index', end=1000):
        print("height=%d block=%s" % (block.height, block.hash))