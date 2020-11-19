import os
from blockchain_parser.blockchain import Blockchain
from neo4j import GraphDatabase

def create_friend_of(tx, name, friend):
    tx.run("CREATE (a:Person)-[:KNOWS]->(f:Person {name: $friend}) "
           "WHERE a.name = $name "
           "RETURN f.name AS friend", name=name, friend=friend)

def create_blocks(tx,blocks):
    counter = 0
    for b in blocks:
        if counter==5000:
            break
        blockheader = b.header
        #print("entering in counter:",counter)
        tx.run("MERGE (block:block {hash:$blockhash}) CREATE (block)-[:coinbase]->(out:output:coinbase {value:$value," +
                                        "index:$outindex})"
                                        " SET block.size=$size,block.prevblock=$prevblock,block.merkleroot=$merkleroot," +
                                        "block.time=$timestamp,block.bits=$bits,block.nonce=$nonce," +
                                        "block.txcount=$txcount,block.version=$version MERGE (prevblock:block {hash:$prevblock})" +
                                        " MERGE (block)-[:chain]->(prevblock)", blockhash=b.hash, size=b.size, prevblock=blockheader.previous_block_hash, merkleroot=blockheader.merkle_root, timestamp=blockheader.timestamp
                                        , bits= blockheader.bits, nonce=blockheader.nonce, txcount=len(b.transactions),
                                        version=blockheader.version,value=b.transactions[0].outputs[0].value,outindex=b.transactions[0].txid)
        counter+=1

def create_transaction(tx,blocks):
    counter = 0
    for b in blocks:
        if counter==5000:
            break
        count = 0
        intcount = 0
        for tran in b.transactions:
            #print("transaction list size",len(b.transactions))
            #print("tran length",len(b.transactions),"is coin_base: ",tran.is_coinbase)
            if count!=0:
                print("ENtering YOLOLOLOLOL")
                tx.run("MATCH (block :block {hash:$hash}) CREATE (n:test)  MERGE (tx:tx {txid:$txid,version:$version,locktime:$locktime}) MERGE (tx)-[:inc {i:$i}]->(block)",
                       hash=b.hash, txid=tran.txid
                       , version=tran.version, locktime=tran.locktime, i=intcount)
                intcount+=1
            print("count",count,"transaction size",len(b.transactions))
            count+=1
        counter+=1

def create_transactionInputs(tx,blocks):
    counter = 0
    for b in blocks:
        if counter == 5000:
            break
        count = 0
        intcount = 0
        for tran in b.transactions:
            # print("transaction list size",len(b.transactions))
            # print("tran length",len(b.transactions),"is coin_base: ",tran.is_coinbase)
            for tranin in tran.inputs:
                if count != 0:

                    tx.run(
                        "MATCH (tx:tx {txid:$txid}) MERGE (in :output {index: $parenttxid,vin: $inputvin}) MERGE (in)-[:in {vin: $inputvin, scriptSig: $inputscriptSig, sequence: $inputsequence, "
                        "witness: $inputwitness}]->(tx)",
                        txid=tran.txid,
                        parenttxid=tranin.transaction_hash,
                        inputvin=tranin.transaction_index, inputscriptSig=tranin.script.value, inputsequence=tranin.sequence_number
                        , inputwitness=tranin.witnesses)
                    intcount += 1
                #print("count", count, "transaction size", len(b.transactions))
            count += 1
        counter += 1

def create_transactionOutputs(tx,blocks):
    counter = 0
    for b in blocks:
        if counter == 5000:
            break
        count = 0
        intcount = 0
        for tran in b.transactions:
            # print("transaction list size",len(b.transactions))
            # print("tran length",len(b.transactions),"is coin_base: ",tran.is_coinbase)
            for tranin in tran.outputs:

                if count != 0:

                    tx.run(
                        "MATCH (tx:tx{txid:$txid}) MERGE (out :output {index: $outputindex,vin: $outputvout}) MERGE (tx)-[:out {vout: $outputvout}]->(out) "
                        "SET out.value= $outputvalue, out.scriptPubKey= $outputscriptPubKey",
                        txid = tran.txid, outputindex=tran.txid, outputvout = intcount, outputvalue= tranin.value,
                        outputscriptPubKey=tranin.script.value)
                    intcount += 1
                print("count", count, "transaction size", len(b.transactions))
            count += 1
        counter += 1

def create_addresses(tx,blocks):
    counter = 0
    for b in blocks:
        if counter == 5000:
            break
        count = 0
        intcount = 0
        for tran in b.transactions:
            # print("transaction list size",len(b.transactions))
            # print("tran length",len(b.transactions),"is coin_base: ",tran.is_coinbase)
            for tranin in tran.outputs:
                print("count", count, "transaction size", len(b.transactions), "addresses", tranin.addresses)
                if count != 0:
                    #print("ENtering YOLOLOLOLOL")
                    tx.run(
                        "MERGE (out :output {index: $outputindex,vin: $outputvout}) MERGE (out)-[:locked]->(n:address {address: $add})",
                        outputindex=tran.txid, outputvout = intcount,add=tranin.addresses)
                    intcount += 1

            count += 1
        counter += 1


def create_inputs(session,blocks):
    counter = 0
    for b in blocks:
        if counter == 5000:
            break
        count = 0
        intcount = 0
        for tran in b.transactions:
            # print("transaction list size",len(b.transactions))
            # print("tran length",len(b.transactions),"is coin_base: ",tran.is_coinbase)
            for tranin in tran.inputs:
                if count != 0:

                    tx = session.begin_transaction()
                    tx.run(
                        "MATCH (tx:tx {txid:$txid}) MERGE (in :output {index: $parenttxid,vin: $inputvin}) MERGE (in)-[:in {vin: $inputvin, scriptSig: $inputscriptSig, sequence: $inputsequence, "
                        "witness: $inputwitness}]->(tx)",
                        txid=tran.txid,
                        parenttxid=tranin.transaction_hash,
                        inputvin=tranin.transaction_index, inputscriptSig=tranin.script.value,
                        inputsequence=tranin.sequence_number
                        , inputwitness=tranin.witnesses)
                    tx.commit()
                    tx.close()
                    intcount += 1
                # print("count", count, "transaction size", len(b.transactions))
            count += 1
        counter += 1
    print("completed Inputs#################################################################################################################################################")

def create_outputs(session,blocks):
    counter = 0
    for b in blocks:
        if counter < 0:
            counter+=1
            continue
        if counter == 5000:
            break
        count = 0

        for tran in b.transactions:
            # print("transaction list size",len(b.transactions))
            # print("tran length",len(b.transactions),"is coin_base: ",tran.is_coinbase)
            intcount = 0
            for tranin in tran.outputs:
                if count != 0:

                    tx = session.begin_transaction()
                    tx.run(
                        "MATCH (tx:tx{txid:$txid}) MERGE (out :output {index: $outputindex,vin: $outputvout}) MERGE (tx)-[:out {vout: $outputvout}]->(out) "
                        "SET out.value= $outputvalue, out.scriptPubKey= $outputscriptPubKey",
                        txid=tran.txid, outputindex=tran.txid, outputvout=intcount, outputvalue=tranin.value,
                        outputscriptPubKey=tranin.script.value)
                    tx.commit()
                    tx.close()

                    #print("length of addresses is:",len(tranin.addresses))
                    for i in range(0,len(tranin.addresses)):
                        print("count", count, "transaction size", len(b.transactions), "addresses", tranin.addresses[i].address,"bcount",counter,"address type is",tranin.type,"output count:",
                              len(tran.outputs),"output count number",intcount,"length of addresses",len(tranin.addresses))
                        tx = session.begin_transaction()
                        tx.run(
                            "MERGE (out :output {index: $outputindex,vin: $outputvout}) MERGE (out)-[:locked]->(n:address {address: $add})",
                            outputindex=tran.txid, outputvout=intcount, add=tranin.addresses[i].address)
                        tx.commit()
                        tx.close()

                    intcount += 1
                else:
                    for i in range(0, len(tranin.addresses)):
                        print("count", count, "transaction size", len(b.transactions), "addresses", tranin.addresses[i].address,"bcount",counter,"address type is",tranin.type)
                        tx = session.begin_transaction()
                        tx.run(
                            "MERGE (out :output {index: $outputindex,vin: $outputvout}) MERGE (out)-[:locked]->(n:address {address: $add})",
                            outputindex=tran.txid, outputvout=0, add=tranin.addresses[i].address)
                        tx.commit()
                        tx.close()
                #print("count", count, "transaction size", len(b.transactions))
            count += 1
        counter += 1

def commit_address(session,blocks):
    counter = 0
    for b in blocks:
        if counter == 5000:
            break
        count = 0

        for tran in b.transactions:
            # print("transaction list size",len(b.transactions))
            # print("tran length",len(b.transactions),"is coin_base: ",tran.is_coinbase)
            intcount = 0
            for tranin in tran.outputs:

                if count != 0:


                    for i in range(0,len(tranin.addresses)):
                        print("count", count, "transaction size", len(b.transactions), "addresses", tranin.addresses[i].address,"bcount",counter)
                        tx = session.begin_transaction()
                        tx.run(
                            "MERGE (out :output {index: $outputindex,vin: $outputvout}) MERGE (out)-[:locked]->(n:address {address: $add})",
                            outputindex=tran.txid, outputvout=intcount, add=tranin.addresses[i].address)
                        tx.commit()
                        tx.close()

                    intcount += 1
                else:
                    for i in range(0, len(tranin.addresses)):
                        print("count", count, "transaction size", len(b.transactions), "addresses", tranin.addresses[i].address,"bcount",counter)
                        tx = session.begin_transaction()
                        tx.run(
                            "MERGE (out :output {index: $outputindex,vin: $outputvout}) MERGE (out)-[:locked]->(n:address {address: $add})",
                            outputindex=tran.txid, outputvout=0, add=tranin.addresses[i].address)
                        tx.commit()
                        tx.close()

            count += 1
        counter += 1

def main():
    uri = "bolt://localhost:7687"
    driver = GraphDatabase.driver(uri, auth=("neo4j", "n"))
    blockchain = Blockchain(os.path.expanduser('/media/varun/DATA/Bitcoin data/Blockdata_testing/1'))
    blocks =blockchain.get_unordered_blocks()
    with driver.session() as session:
        #session.write_transaction(create_addresses, blocks)
        create_inputs(session,blocks)
        create_outputs(session, blocks)
    # with driver.session() as session:
    #     create_outputs(session, blocks)

    # session.write_transaction(create_blocks,blocks)
    # with driver.session() as session2:
    #     session2.write_transaction(create_transaction, blocks)
    # tx_hash_handle = open("txhash_00.txt","w+")
    # tx_index = open("txindex_00.txt", "w+")
    #   #D:\Courses\Capstone\PyBC\pybit\Blocks
    # for block in blockchain.get_unordered_blocks():
    #     for tx in block.transactions:
    #         for input in tx.inputs:
    #             tx_hash_handle.write(str(input.transaction_hash)+"\n")
    #             tx_index.write(str(input.transaction_index)+"\n")
    #
    # tx_hash_handle.flush()
    # tx_index.flush()
    # tx_index.close()
    # tx_hash_handle.close()
    driver.close()

if __name__ == '__main__':
    main()