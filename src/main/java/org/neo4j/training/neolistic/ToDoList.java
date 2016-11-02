package org.neo4j.training.neolistic;

import java.util.List;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import spark.Request;
import spark.Response;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;

import static org.neo4j.driver.v1.Values.parameters;
import static org.neo4j.training.neolistic.Neolistic.driver;

public class ToDoList
{
    public static List<ToDoEntry> allEntries( Request request, Response response )
    {
        return driver().read( tx -> tx.run( "MATCH (entry:Entry) RETURN entry" )
                .list( record -> ToDoEntry.from( record.get( "entry" ).asNode() ) ) );
    }

    public static ToDoEntry createEntry( Request request, Response response )
    {
        return driver().write( tx -> {
            JsonParser parser = new JsonParser();
            JsonObject parsed = parser.parse( request.body() ).getAsJsonObject();
            UUID uuid = UUID.randomUUID();
            String title = parsed.get( "title" ).getAsString();
            int order = parsed.get( "order" ).getAsInt();

            StatementResult result = tx.run(
                    "CREATE (entry:Entry {uuid:$uuid, title:$title, order:$order}) RETURN entry",
                    parameters( "uuid", uuid.toString(), "title", title, "order", order ) );
            ToDoEntry entry = ToDoEntry.from( result.next().get( "entry" ).asNode() );
            result.consume();
            return entry;
        } );
    }

    public static Void deleteAllEntries( Request request, Response response )
    {
        driver().write( tx -> tx.run( "MATCH (entry:Entry) DETACH DELETE entry" ).consume() );
        response.status( 204 );
        return null;
    }

}
