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

public class NeolisticItem
{
    private final String description;
    private boolean done;

    public NeolisticItem( String description )
    {
        this.description = description;
        this.done = false;
    }

    public String description()
    {
        return description;
    }

    public boolean done()
    {
        return done;
    }

    public void setDone()
    {
        done = true;
    }
}