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
package uk.ac.leedsbeckett.lbufilters.general.nodes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import uk.ac.leedsbeckett.lbufilters.general.Element;
import uk.ac.leedsbeckett.lbufilters.general.Processor;

/**
 *
 * @author maber01
 */
public class Tokenise extends Processor
{
  ArrayList<Delimiter> delimiters = new ArrayList<>();
  
  Reader reader;
  StringBuilder pushedBack = new StringBuilder();
  
  int maxDelimiterLength;
  
  
  private int read() throws IOException
  {
    if ( pushedBack.length() > 0 )
    {
      char c = pushedBack.charAt( 0 );
      pushedBack.deleteCharAt( 0 );
      return (int)c;
    }
    return reader.read();
  }
  
  private void pushBack( String s )
  {
    pushedBack.append( s );
  }
  
  public void setReader( Reader reader )
  {
    this.reader = reader;
    maxDelimiterLength = 0;
    for ( Delimiter d : delimiters )
      if ( d.value.length() > maxDelimiterLength )
        maxDelimiterLength = d.value.length();
  }
  
  public boolean next( Writer writer ) throws IOException
  {
    String[] r = new String[2];
    int n;
    char  c;
    String str="";
    while ( true )
    {
      n = this.read();      
      if ( n < 0 )
      {
        if ( str.length() == 0 )
          return false;
        else
          break;
      }
      c = (char)n;
      str = str + c;
      if ( oneFullMatch( str ) && noPartialMatches( str ) )
        break;
    }
    
      
    if ( oneFullMatch( str ) )
    {
      r[1] = bestFullMatch( str );
      r[0] = str.substring( 0, str.indexOf( r[1] ) );
      if ( ( r[0].length() + r[1].length() ) < str.length() )
        pushBack( str.substring( r[0].length() + r[1].length() ) );
    }
    else
    {
      r[0] = str;
      r[1] = "";
    }

    this.processChildren( writer, r );
    
    return true;
  }
  
  private boolean oneFullMatch( String s )
  {
    for ( Delimiter d : delimiters )
      if ( s.contains( d.value ) )
        return true;
    return false;
  }

  private String bestFullMatch( String s )
  {
    for ( Delimiter d : delimiters )
      if ( s.contains( d.value ) )
        return d.value;
    return null;
  }

  private boolean noPartialMatches( String s )
  {
    for ( Delimiter d : delimiters )
    {
      String part = "";
      for ( int i=0; i<( d.value.length()-1 ); i++ )
      {
        part = part + d.value.charAt( i );
        if ( s.endsWith( part ) )
          return false;
      }
    }
    return true;
  }
  
  
  public void addElement( Delimiter d )
  {
    delimiters.add( d );
  }
  
  @Override
  public void process( Writer writer, String[] s ) throws IOException
  {
    throw new UnsupportedOperationException( "Tokenise doesn't implement 'process'." );
  }
}
