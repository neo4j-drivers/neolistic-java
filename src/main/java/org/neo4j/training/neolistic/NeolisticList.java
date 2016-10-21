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

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.AccessMode;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;
import org.neo4j.driver.v1.exceptions.SessionExpiredException;

import static java.lang.String.format;

import static org.neo4j.driver.v1.Values.parameters;

public class NeolisticList
{
    public static List<NeolisticList> all( Driver driver, int maxTries ) throws NeolisticException
    {
        int tries = 0;
        while ( tries < maxTries )
        {
            try ( Session session = driver.session( AccessMode.READ ) )
            {
                try ( Transaction tx = session.beginTransaction() )
                {
                    StatementResult result = tx.run( "MATCH (li:List) RETURN li.id, li.title" );
                    List<NeolisticList> lists = new ArrayList<>();
                    while ( result.hasNext() )
                    {
                        Record record = result.next();
                        NeolisticList list = new NeolisticList(
                                record.get( "li.id" ).asInt(),
                                record.get( "li.title" ).asString() );
                        list.loadItems( driver, maxTries );
                        lists.add( list );
                    }
                    return lists;
                }
            }
            catch ( SessionExpiredException e )
            {
                tries -= 1;
            }
            catch ( ServiceUnavailableException e )
            {
                throw new NeolisticException( "Database service unavailable", e );
            }
        }
        throw new NeolisticException( format( "Database service unavailable after %d tries", maxTries ) );
    }

    private final int id;
    private final String title;
    private final List<NeolisticItem> items;

    public NeolisticList( int id, String title )
    {
        this.id = id;
        this.title = title;
        this.items = new ArrayList<>();
    }

    public int id()
    {
        return id;
    }

    public String title()
    {
        return title;
    }

    int loadItems( Driver driver, int maxTries ) throws NeolisticException
    {
        int tries = 0;
        while ( tries < maxTries )
        {
            try ( Session session = driver.session( AccessMode.READ ) )
            {
                try ( Transaction tx = session.beginTransaction() )
                {
                    StatementResult result = tx.run( "MATCH (li:List {id:$list_id})-[:ITEM]->(it) RETURN it.description ORDER BY it.no", parameters( "list_id", id ) );
                    List<NeolisticItem> newItems = new ArrayList<>();
                    while ( result.hasNext() )
                    {
                        Record record = result.next();
                        NeolisticItem item = new NeolisticItem( record.get( "it.description" ).asString() );
                        newItems.add( item );
                    }
                    items.clear();
                    items.addAll( newItems );
                    return newItems.size();
                }
            }
            catch ( SessionExpiredException e )
            {
                tries -= 1;
            }
            catch ( ServiceUnavailableException e )
            {
                throw new NeolisticException( "Database service unavailable", e );
            }
        }
        throw new NeolisticException( format( "Database service unavailable after %d tries", maxTries ) );
    }

    public int size()
    {
        return items.size();
    }

    public NeolisticItem get( int index )
    {
        return items.get( index );
    }
}
