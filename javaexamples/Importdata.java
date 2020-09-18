
import org.bitcoinj.core.TransactionInput;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.*;

import java.io.File;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;


public class Importdata {
    public static void main(String[] args) {
        DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(new File(
                "D:\\neo4j\\neo4j-community-4.1.0")).build();
        GraphDatabaseService graphDb = managementService.database(DEFAULT_DATABASE_NAME);

        try (Transaction tx = graphDb.beginTx()) {
            Node firstNode = tx.createNode();
            firstNode.setProperty("message", "Hello, ");
            Node secondNode = tx.createNode();
            secondNode.setProperty("message", "World!");

            Relationship relationship = firstNode.createRelationshipTo(secondNode, RelationshipTypes.IS_FRIEND_OF);
            relationship.setProperty("message", "Testing neo4j ");

            ResourceIterator<Node> nodes = tx.findNodes(Label.label("Person"));
            while (nodes.hasNext()){

                System.out.println(nodes.next().getProperty("name"));
            }
        }

        managementService.shutdown();

    }
}

enum RelationshipTypes implements RelationshipType {
    IS_FRIEND_OF,
    HAS_SEEN;
}


