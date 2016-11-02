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

import java.util.Map;
import java.util.UUID;

import org.neo4j.driver.v1.types.Node;

public class ToDoEntry
{
    private UUID uuid;
    private String title;
    private long order;
    private boolean completed;

    public static ToDoEntry from( Node node )
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
}
