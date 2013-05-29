package org.apache.maven.surefire.booter;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.surefire.util.UrlUtils;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * An ordered list of classpath elements with set behaviour
 *
 * A Classpath is immutable and thread safe.
 *
 *
 * @author Kristian Rosenvold
 */
@ThreadSafe
@Immutable
public class Classpath implements Iterable<String>
{

    private final List<String> unmodifiableElements;

    public static Classpath join( Classpath firstClasspath, Classpath secondClasspath )
    {
        LinkedHashSet<String> accumulated =  new LinkedHashSet<String>(  );
        if (firstClasspath != null) firstClasspath.addTo( accumulated );
        if (secondClasspath != null) secondClasspath.addTo( accumulated );
        return new Classpath( accumulated );
    }


    private void addTo(Collection<String> c){
        c.addAll( unmodifiableElements );
    }

    private Classpath()
    {
        this.unmodifiableElements = Collections.emptyList();
    }


    public Classpath( Classpath other, String additionalElement )
    {
        ArrayList<String> elems = new ArrayList<String>( other.unmodifiableElements );
        elems.add( additionalElement );
        this.unmodifiableElements = Collections.unmodifiableList( elems );
    }

    public Classpath( Iterable<String> elements )
    {
        List<String> newCp = new ArrayList<String>(  );
        for ( String element : elements )
        {
            newCp.add( element );
        }
        this.unmodifiableElements = Collections.unmodifiableList( newCp );
    }

    public static Classpath emptyClasspath()
    {
        return new Classpath();
    }

    public Classpath addClassPathElementUrl( String path )
    {
        if ( path == null )
        {
            throw new IllegalArgumentException( "Null is not a valid class path element url." );
        }
        return !unmodifiableElements.contains( path ) ? new Classpath( this, path ) : this;
    }

    public List<String> getClassPath()
    {
        return unmodifiableElements;
    }

    public List<URL> getAsUrlList()
        throws MalformedURLException
    {
        List<URL> urls = new ArrayList<URL>();
        for ( String url : unmodifiableElements )
        {
            File f = new File( url );
            urls.add( UrlUtils.getURL( f ) );
        }
        return urls;
    }

    public void writeToSystemProperty( String propertyName )
    {
        StringBuilder sb = new StringBuilder();
        for ( String element : unmodifiableElements )
        {
            sb.append( element ).append( File.pathSeparatorChar );
        }
        System.setProperty( propertyName, sb.toString() );
    }

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        Classpath classpath = (Classpath) o;

        return !( unmodifiableElements
            != null ? !unmodifiableElements.equals( classpath.unmodifiableElements ) : classpath.unmodifiableElements != null );

    }

    public ClassLoader createClassLoader( ClassLoader parent, boolean childDelegation, boolean enableAssertions,
                                          String roleName )
        throws SurefireExecutionException
    {
        try
        {
            List urls = getAsUrlList();
            IsolatedClassLoader classLoader = new IsolatedClassLoader( parent, childDelegation, roleName );
            for ( Object url1 : urls )
            {
                URL url = (URL) url1;
                classLoader.addURL( url );
            }
            if ( parent != null )
            {
                parent.setDefaultAssertionStatus( enableAssertions );
            }
            classLoader.setDefaultAssertionStatus( enableAssertions );
            return classLoader;
        }
        catch ( MalformedURLException e )
        {
            throw new SurefireExecutionException( "When creating classloader", e );
        }
    }


    public int hashCode()
    {
        return unmodifiableElements != null ? unmodifiableElements.hashCode() : 0;
    }

    public String getLogMessage( String descriptor )
    {
        StringBuilder result = new StringBuilder();
        result.append( descriptor ).append( " classpath:" );
        for ( String element : unmodifiableElements )
        {
            result.append( "  " ).append( element );
        }
        return result.toString();
    }

    public String getCompactLogMessage( String descriptor )
    {
        StringBuilder result = new StringBuilder();
        result.append( descriptor ).append( " classpath:" );
        for ( String element : unmodifiableElements )
        {
            result.append( "  " );
            if ( element != null )
            {
                int pos = element.lastIndexOf( File.separatorChar );
                if ( pos >= 0 )
                {
                    result.append( element.substring( pos + 1 ) );
                }
                else
                {
                    result.append( element );
                }

            }
            else
            {
                result.append( element );
            }
        }
        return result.toString();
    }

    public Iterator<String> iterator()
    {
        return unmodifiableElements.iterator();
    }
}
