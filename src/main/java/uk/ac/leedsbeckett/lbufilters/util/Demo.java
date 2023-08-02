/*
 * Copyright 2023 maber01.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.leedsbeckett.lbufilters.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.Parameterizable;
import uk.ac.leedsbeckett.lbufilters.GeneralFilter;
import uk.ac.leedsbeckett.lbufilters.JsonPrettyPrint;
import uk.ac.leedsbeckett.lbufilters.JsonToXml;
import uk.ac.leedsbeckett.lbufilters.NormaliseProperties;
import uk.ac.leedsbeckett.lbufilters.PropertiesToXml;
import uk.ac.leedsbeckett.lbufilters.Xslt;

/**
 *
 * @author maber01
 */
public class Demo
{

  public static void unpack( String name, File dir ) throws IOException
  {
    try ( InputStream is = Demo.class.getClassLoader().getResourceAsStream( name ) )
    {
      File f = new File( dir, name );
      if ( f.exists() ) f.delete();
      Files.copy( is, f.toPath() );
    }
  }

  public static void testFilter( Constructor<? extends FilterReader> con, String infile, Charset inc, String outfile, Charset outc, Parameter p )
          throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
  {
    try ( 
          FileReader reader = new FileReader( "test/" + infile, inc );
          FileWriter writer = new FileWriter( "test/" + outfile, outc );
        )
    {
      FilterReader filter = con.newInstance( reader );
      if ( filter instanceof Parameterizable && p != null )
      {
        Parameterizable pable = (Parameterizable)filter;
        pable.setParameters( p );
      }
      int c;
      while ( (c=filter.read()) >=0 )
        writer.write( c );
    }
  }
  
  public static void main( String[] args )
  {
    File dir = new File( "test" );
    if ( !dir.exists() ) dir.mkdir();
    
    try
    {
      Demo.unpack( "sample.properties", dir );
      Demo.unpack( "sample.json", dir );
      Demo.unpack( "sample.xml", dir );
      Demo.unpack( "sample.xsl", dir );
      Demo.unpack( "findreplace.xsl", dir );
      Demo.unpack( "spec.xml", dir );
    }
    catch ( IOException ex )
    {
      Logger.getLogger( Demo.class.getName() ).log( Level.SEVERE, null, ex );
      System.exit( 1 );
    }

    try
    {
//      testFilter( JsonPrettyPrint.class.getConstructor( Reader.class ), 
//                  "sample.json", StandardCharsets.UTF_8, 
//                   "pretty.sample.json", StandardCharsets.UTF_8, null );
//      testFilter( JsonToXml.class.getConstructor( Reader.class ), 
//                  "sample.json", StandardCharsets.UTF_8, 
//                   "sample.json.xml", StandardCharsets.UTF_8, null );
//      testFilter( NormaliseProperties.class.getConstructor( Reader.class ), 
//                  "sample.properties", StandardCharsets.ISO_8859_1, 
//                   "nomalised.properties", StandardCharsets.UTF_8, null );
//      testFilter( PropertiesToXml.class.getConstructor( Reader.class ), 
//                  "sample.properties", StandardCharsets.ISO_8859_1, 
//                   "sample.properties.xml", StandardCharsets.UTF_8, null );
//      Parameter p = new Parameter();
//      p.setName( "transform" );
//      p.setValue( "test/findreplace.xsl" );
//      testFilter( Xslt.class.getConstructor( Reader.class ), 
//                  "sample.properties.xml", StandardCharsets.UTF_8, 
//                   "sample.edited.properties", StandardCharsets.ISO_8859_1, p );
      Parameter p = new Parameter();
      p.setName( "specification" );
      p.setValue( "test/spec.xml" );
      testFilter( GeneralFilter.class.getConstructor( Reader.class ), 
                  "sample.properties", StandardCharsets.ISO_8859_1, 
                   "sample.edited.properties", StandardCharsets.ISO_8859_1, p );
//      p = new Parameter();
//      p.setName( "transform" );
//      p.setValue( "test/sample.xsl" );
//      testFilter( Xslt.class.getConstructor( Reader.class ), 
//                  "sample.xml", StandardCharsets.UTF_8, 
//                   "sample.html", StandardCharsets.UTF_8, p );
    }
    catch ( Exception ex )
    {
      Logger.getLogger( Demo.class.getName() ).log( Level.SEVERE, null, ex );
      System.exit( 1 );
    }
    
//    try ( 
//      FileReader reader = new FileReader( "test/sample.properties.xml", StandardCharsets.UTF_8 );
//      FileWriter writer = new FileWriter( "test/sample.edited.properties", StandardCharsets.ISO_8859_1 );
//            )
//    {
//      Xslt filter = new Xslt( reader );
//      Parameter p = new Parameter();
//      p.setName( "transform" );
//      p.setValue( "findreplace.xsl" );
//      filter.setParameters( p );
//      int c;
//      while ( (c=filter.read()) >=0 )
//        writer.write( c );
//    }
//    catch ( IOException ex )
//    {
//      Logger.getLogger( Demo.class.getName() ).log( Level.SEVERE, null, ex );
//    }
    
  }
}
