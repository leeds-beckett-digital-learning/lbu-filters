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
package uk.ac.leedsbeckett.lbufilters;

import uk.ac.leedsbeckett.lbufilters.util.ChunkedXmlFilterReader;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author maber01
 */
public class PropertiesToXmlMemoryEfficient extends ChunkedXmlFilterReader
{
    private static final String ELEMENT_ROOT = "properties";
    private static final String ELEMENT_COMMENT = "comment";
    private static final String ELEMENT_ENTRY = "entry";
    private static final String ATTR_KEY = "key";
    // The required DTD URI for exported properties
    private static final String PROPS_DTD_DECL =
        "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">";
    private static final String PROPS_DTD_URI =
        "http://java.sun.com/dtd/properties.dtd";
    
  BufferedReader reader;
  int count = 0;
  boolean in_comment=false;
  
  public PropertiesToXmlMemoryEfficient( Reader in )
  {
    super( in );
    reader = new BufferedReader( in );
  }
  
  @Override
  public void firstChunk( XMLStreamWriter writer  ) throws IOException
  {
    in_comment=false;
    try
    {
      writer.writeStartDocument( "UTF-8", "1.0" );
      writer.writeCharacters( "\n" );
      writer.writeDTD(PROPS_DTD_DECL);
      writer.writeCharacters( "\n" );
      writer.writeStartElement( ELEMENT_ROOT );
      writer.writeCharacters( "\n" );
    }
    catch ( XMLStreamException ex )
    {
      throw new IOException( "Trying to output invalid XML.", ex );
    }
  }

  @Override
  public void nextChunk( XMLStreamWriter writer ) throws IOException
  {    
    try
    {
      String line = reader.readLine();
      count++;
      
      if ( line == null )
      {
        setEnded();
        if ( in_comment )
        {
          writer.writeEndElement();
          writer.writeCharacters( "\n" );
        }
        writer.writeEndElement();
        writer.writeEndDocument();
        return;
      }
      
      if ( line.length() == 0 || line.trim().length() == 0 ||
           line.charAt( 0 ) == '#' || line.charAt( 0 ) == '!'   )
      {
        // Is non-standard, internal comment??
        if ( !in_comment )
        {
          writer.writeCharacters( "  " );
          writer.writeStartElement( ELEMENT_COMMENT );
          in_comment = true;
        }
        writer.writeCharacters( convertCommentLine(line) );
        writer.writeCharacters( "\n" );
      }
      else
      {
        // close comment header if there was one
        if ( in_comment )
        {
          writer.writeEndElement();
          writer.writeCharacters( "\n" );
          in_comment=false;
        }
        writer.writeCharacters( "  " );
        outputEntry( convertEntry( line ), writer );
      }
    }
    catch ( XMLStreamException ex )
    {
      throw new IOException( "Trying to output invalid XML.", ex );
    }
  }

  private String convertCommentLine( String line ) throws XMLStreamException, IOException
  {
    String pretendentry = "x=" + (( line.startsWith( "#" ) || line.startsWith( "!" ) )?line.substring( 1 ):line);
    Properties p = convertEntry( pretendentry );
    return p.getProperty( "x" );
  }
  
  private Properties convertEntry( String line ) throws XMLStreamException, IOException
  {
    CharArrayReader r = new CharArrayReader( line.toCharArray() );
    // Use an actual Properties object to parse a single property line.
    Properties p = new Properties();
    p.load( r );
    return p;
  }

  private void outputEntry( Properties p, XMLStreamWriter writer ) throws XMLStreamException, IOException
  {
    if ( p.size() != 1 )
    {
      // invalid line
      writer.writeStartElement( ELEMENT_COMMENT );
      writer.writeCharacters( "Conversion error" );
      writer.writeEndElement();
      writer.writeCharacters( "\n" );      
      return;
    }
    
    for ( Object name : p.keySet() )
    {
      writer.writeStartElement( ELEMENT_ENTRY );
      writer.writeAttribute( ATTR_KEY, name.toString() );
      writer.writeCharacters( p.getProperty( name.toString() ) );
      writer.writeEndElement();
      writer.writeCharacters( "\n" );    
    }
  }

}
