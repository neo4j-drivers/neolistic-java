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

import org.neo4j.driver.v1.AuthToken;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;

import static java.lang.String.format;

public class Neolistic
{
    private final Driver driver;

    public Neolistic( String uri, AuthToken authToken ) throws ServiceUnavailableException
    {
        driver = GraphDatabase.driver( uri, authToken );
    }

    public static void main( String... args )
    {
        if ( args.length == 1 )
        {
            System.err.println( args[0] );
            System.exit( 0 );
        }
        try
        {
            Neolistic app = new Neolistic( uri(), authToken() );
            for ( NeolisticList list : NeolisticList.all( app.driver, 3 ) )
            {
                System.out.println( format( "%s [#%d]", list.title(), list.id() ) );
                for ( int i = 1; i <= list.size(); i++ )
                {
                    System.out.println( format( "%d. %s", i, list.get( i - 1 ).description() ) );
                }
                System.out.println();
            }
        }
        catch ( NeolisticException e )
        {
            System.err.println( e.getMessage() );
            System.exit( 1 );
        }
    }

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

}
