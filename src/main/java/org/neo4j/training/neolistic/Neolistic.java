/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neolistic.
 *
 * Neolistic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.training.neolistic;

import java.util.List;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.neo4j.driver.v1.AuthToken;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;
import org.neo4j.driver.v1.types.Node;

import static java.util.Collections.singletonList;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.debug.DebugScreen.enableDebugScreen;

import static org.neo4j.driver.v1.Values.parameters;

public class Neolistic
{
    private static String uri()
    {
        String uri = System.getenv( "NEO4J_URI" );
        return uri == null ? "bolt://127.0.0.1:7687" : uri;
    }

    private static AuthToken authToken()
    {
        String user = System.getenv( "NEO4J_USER" );
        String password = System.getenv( "NEO4J_PASSWORD" );
        if ( password == null )
        {
            password = "password";
        }
        if ( user == null )
        {
            return AuthTokens.basic( "neo4j", password );
        }
        else
        {
            return AuthTokens.basic( user, password );
        }
    }

    private static Driver newDriver( List<String> uris, AuthToken authToken, Config config )
    {
        for ( String uri : uris )
        {
            try
            {
                return GraphDatabase.driver( uri, authToken, config );
            }
            catch ( ServiceUnavailableException ex )
            {
                // This URI failed, so loop around again if we have another.
            }
        }
        throw new ServiceUnavailableException( "No valid database URI found" );
    }

    public static void main( String... args )
    {
        Driver driver = newDriver( singletonList( uri() ), authToken(), Config.defaultConfig() );
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();

        before( ( request, response ) -> response.type( "application/json" ) );

        get( "/", ( request, response ) -> driver.read( tx -> tx.run( "MATCH (entry:Entry) RETURN entry" )
                .list( record -> Entry.from( record.get( "entry" ).asNode() ) ) ), gson::toJson );

        post( "/", ( request, response ) -> driver.write( tx -> {
            JsonObject parsed = parser.parse( request.body() ).getAsJsonObject();
            UUID uuid = UUID.randomUUID();
            String title = parsed.get( "title" ).getAsString();
            int order = parsed.get( "order" ).getAsInt();
            return Entry.from( tx.run( "CREATE (entry:Entry {uuid:$uuid,title:$title,order:$order}) RETURN entry",
                    parameters( "uuid", uuid.toString(), "title", title, "order", order ) )
                    .next().get( "entry" ).asNode() );
        } ), gson::toJson );

        enableDebugScreen();
    }

}
