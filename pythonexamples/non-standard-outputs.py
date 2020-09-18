# outputs which are not ordered

import sys
from blockchain_parser.blockchain import Blockchain
import os

def main():
    blockchain = Blockchain(os.path.expanduser('/media/varun/DATA/Courses/Capstone/PyBC/pybit/Blocks'))
    for block in blockchain.get_unordered_blocks():
        for transaction in block.transactions:
            for output in transaction.outputs:
                if output.is_unknown():
                    print(block.header.timestamp, output.script.value)
