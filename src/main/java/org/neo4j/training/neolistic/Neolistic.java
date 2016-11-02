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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.neo4j.driver.v1.AuthToken;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;

import static java.util.Collections.singletonList;

import static spark.Spark.before;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.debug.DebugScreen.enableDebugScreen;

public class Neolistic
{
    private static Driver driver = null;

    public Neolistic()
    {
        Gson gson = new Gson();

        before( ( request, response ) -> response.type( "application/json" ) );

        get( "/", ( request, response ) -> ToDoEntry.loadAll(), gson::toJson );

        post( "/", ( request, response ) -> driver().write( tx -> {
            JsonObject parsed = new JsonParser().parse( request.body() ).getAsJsonObject();
            return ToDoEntry.create( parsed.get( "title" ).getAsString(), parsed.get( "order" ).getAsInt() );
        } ), gson::toJson );

        delete( "/", ( request, response ) -> ToDoEntry.deleteAll(), gson::toJson );

        enableDebugScreen();
    }

    public static Driver driver()
    {
        if ( driver == null /* TODO: or defunct */ )
        {
            String u = System.getenv( "NEO4J_URI" );
            List<String> uris = singletonList( u == null ? "bolt://127.0.0.1:7687" : u );

            String password = System.getenv( "NEO4J_PASSWORD" );
            AuthToken authToken = AuthTokens.basic( "neo4j", password == null ? "password" : password );

            Config config = Config.defaultConfig();

            for ( String uri : uris )
            {
                try
                {
                    driver = GraphDatabase.driver( uri, authToken, config );
                    break;
                }
                catch ( ServiceUnavailableException ex )
                {
                    // This URI failed, so loop around again if we have another.
                }
            }
            if ( driver == null )
            {
                throw new ServiceUnavailableException( "No valid database URI found" );
            }
        }
        return driver;
    }

    public static void main( String... args )
    {
        Neolistic app = new Neolistic();
    }

}
