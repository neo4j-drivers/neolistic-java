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
import java.util.Map;
import java.util.UUID;

import org.neo4j.driver.v1.types.Node;

import static org.neo4j.driver.v1.Values.parameters;
import static org.neo4j.training.neolistic.Neolistic.driver;

public class ToDoEntry
{
    private UUID uuid;
    private String title;
    private long order;
    private boolean completed;

    /**
     * Load all entries from the database.
     *
     * @return
     */
    public static List<ToDoEntry> loadAll()
    {
        return driver().read( tx -> tx.run(
                "MATCH (entry:Entry) RETURN entry" )
                .list( record -> ToDoEntry.from( record.get( "entry" ).asNode() ) ) );
    }

    /**
     * Load a single entry from the graph database by UUID.
     *
     * @param uuid
     * @return
     */
    public static ToDoEntry load( UUID uuid )
    {
        return single( driver().read( tx -> tx.run(
                "MATCH (entry:Entry {uuid:$uuid}) RETURN entry",
                parameters( "uuid", uuid.toString() ) )
                .list( record -> ToDoEntry.from( record.get( "entry" ).asNode() ) ) ) );
    }

    /**
     * Create a new entry in the graph database.
     *
     * @param title
     * @param order
     * @return the new entry
     */
    public static ToDoEntry create( String title, int order )
    {
        return single( driver().read( tx -> tx.run(
                "CREATE (entry:Entry {uuid:$uuid, title:$title, order:$order}) RETURN entry",
                parameters(
                        "uuid", UUID.randomUUID().toString(),
                        "title", title,
                        "order", order ) )
                .list( record -> ToDoEntry.from( record.get( "entry" ).asNode() ) ) ) );
    }

    /**
     * Delete all entries from the graph database.
     *
     * @return
     */
    public static List<ToDoEntry> deleteAll()
    {
        return driver().write( tx -> tx.run(
                "MATCH (entry:Entry) DETACH DELETE entry RETURN entry" )
                .list( record -> ToDoEntry.from( record.get( "entry" ).asNode() ) ) );
    }

    /**
     * Construct an entry from the properties of a graph node.
     *
     * @param node
     * @return
     */
    static ToDoEntry from( Node node )
    {
        Map<String, Object> properties = node.asMap();
        return new ToDoEntry(
                UUID.fromString( (String) properties.getOrDefault( "uuid", UUID.randomUUID().toString() ) ),
                (String) properties.getOrDefault( "title", "" ),
                (Long) properties.getOrDefault( "order", 0L ),
                (Boolean) properties.getOrDefault( "completed", false ) );
    }

    private ToDoEntry( UUID uuid, String title, long order, boolean completed )
    {
        this.uuid = uuid;
        this.title = title;
        this.order = order;
        this.completed = completed;
    }

    public UUID uuid()
    {
        return uuid;
    }

    public void setUUID( UUID uuid )
    {
        this.uuid = uuid;
    }

    public String title()
    {
        return title;
    }

    public void setTitle( String title )
    {
        this.title = title;
    }

    public long order()
    {
        return order;
    }

    public void setOrder( long order )
    {
        this.order = order;
    }

    public boolean isCompleted()
    {
        return completed;
    }

    public void setCompleted( boolean completed )
    {
        this.completed = completed;
    }

    // TODO: move this helper into the driver?
    private static <T> T single( List<T> list )
    {
        if ( list.isEmpty() )
        {
            throw new AssertionError( "One item expected, none found" );
        }
        else if ( list.size() == 1 )
        {
            return list.get( 0 );
        }
        else
        {
            throw new AssertionError( "One item expected, " + list.size() + " found" );
        }
    }
}
