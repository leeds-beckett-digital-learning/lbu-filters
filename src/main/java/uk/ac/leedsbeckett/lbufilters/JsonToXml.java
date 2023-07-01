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
import uk.ac.leedsbeckett.lbufilters.util.NoCloseReaderWrapper;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author maber01
 */
public class JsonToXml extends ChunkedXmlFilterReader
{
  public static final String NAMESPACE="http://leedsbeckett.ac.uk/versatilexslt";
  public static final String PREFIX="vx";
  
  JsonFactory jsonFactory;
  JsonParser jsonParser = null;
  int depth;

  public JsonToXml( Reader in )
  {
    super( in );
    jsonFactory = new JsonFactory();
  }

  private final static JsonToken[] starttokens =
  {
    JsonToken.START_ARRAY,
    JsonToken.START_OBJECT,
    JsonToken.VALUE_FALSE,
    JsonToken.VALUE_NULL,
    JsonToken.VALUE_NUMBER_FLOAT,
    JsonToken.VALUE_NUMBER_INT,
    JsonToken.VALUE_STRING,
    JsonToken.VALUE_TRUE
  };
  private final static JsonToken[] endtokens =
  {
    JsonToken.END_ARRAY,
    JsonToken.END_OBJECT,
    JsonToken.VALUE_FALSE,
    JsonToken.VALUE_NULL,
    JsonToken.VALUE_NUMBER_FLOAT,
    JsonToken.VALUE_NUMBER_INT,
    JsonToken.VALUE_STRING,
    JsonToken.VALUE_TRUE
  };
  private final static HashSet<JsonToken> starttokenset = new HashSet<>( Arrays.asList( starttokens ) );
  private final static HashSet<JsonToken> endtokenset   = new HashSet<>( Arrays.asList( endtokens ) );


  /**
   * Gets the JSON parser ready and outputs the XML document start.
   * @param writer
   * @throws IOException 
   */
  @Override
  public void firstChunk( XMLStreamWriter writer ) throws IOException
  {
    jsonParser = jsonFactory.createParser( new NoCloseReaderWrapper( in ) );
    depth=0;
    try
    {
      writer.writeStartDocument( "UTF-8", "1.0" );
      writer.writeCharacters( "\n" );
    }
    catch ( XMLStreamException ex )
    {
      throw new IOException( "Trying to output invalid XML.", ex );
    }
  }
  
  /**
   * Reads the next JSON stream token
   * @param writer
   * @throws IOException 
   */
  @Override
  public void nextChunk( XMLStreamWriter writer ) throws IOException
  {
    try
    {      
      JsonToken token = jsonParser.nextToken();
      if ( token == null )
      {
        setEnded();
        jsonParser.close();
        writer.writeEndDocument();
        return;
      }

      if ( token == JsonToken.NOT_AVAILABLE || token == JsonToken.VALUE_EMBEDDED_OBJECT )
      {
        throw new IOException( "Unsupported JSON token " + token.toString() );
      }

      String previousFieldName = null;
      if ( token == JsonToken.FIELD_NAME )
      {
        previousFieldName = jsonParser.currentName();
        token = jsonParser.nextToken();
        if ( token == null )
        {
          // System.err.println( "No more tokens." );
          setEnded();
          jsonParser.close();
          writer.writeEndDocument();
          return;
        }
      }

      // System.err.println( "Token = " + token.toString() + " depth " + stack.size() );
      
      if ( starttokenset.contains( token ) )
      {
        // System.err.println( "    current element = " + element );
        writeSpaces( writer, depth );
        depth++;
        if ( previousFieldName == null )
        {
          if ( (depth - 1) == 0 )
          {
            writer.writeStartElement( PREFIX, "root", NAMESPACE );
            writer.writeNamespace( PREFIX, NAMESPACE );
          }
          else
          {
            writer.writeStartElement( PREFIX, "item", NAMESPACE );            
          }
        }
        else
          writer.writeStartElement( previousFieldName );

        if ( token == JsonToken.START_ARRAY )
        {
          writer.writeAttribute( "t", "a" );
          writer.writeCharacters( "\n" );
        }
        
        if ( token == JsonToken.START_OBJECT )
        {
          writer.writeAttribute( "t", "o" );
          writer.writeCharacters( "\n" );
        }
      }

      switch ( token )
      {
        case VALUE_NULL:
          writer.writeAttribute( "t", "n" );
          writer.writeCharacters( "null" );
          break;
        case VALUE_STRING:
          writer.writeAttribute( "t", "s" );
          writer.writeCharacters( jsonParser.getValueAsString() );
          break;
        case VALUE_NUMBER_INT:
          writer.writeAttribute( "t", "i" );
          writer.writeCharacters( Long.toString( jsonParser.getLongValue() ) );
          break;
        case VALUE_NUMBER_FLOAT:
          writer.writeAttribute( "t", "f" );
          writer.writeCharacters( Double.toString( jsonParser.getDoubleValue() ) );
          break;
        case VALUE_FALSE:
          writer.writeAttribute( "t", "b" );
          writer.writeCharacters( "false" );
          break;
        case VALUE_TRUE:
          writer.writeAttribute( "t", "b" );
          writer.writeCharacters( "true" );
          break;
      }

      if ( endtokenset.contains( token ) )
      {
        depth--;
        // System.err.println( "    current element = " + element );
        if ( ( token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT ) )
          writeSpaces( writer, depth );
        writer.writeEndElement();
        writer.writeCharacters( "\n" );
      }
    }
    catch ( XMLStreamException ex )
    {
      throw new IOException( "Trying to output invalid XML.", ex );
    }
  }

  /**
   * Writes a variable number of characters to the XML stream.
   * 
   * @param writer The stream to write to.
   * @param n The number of spaces.
   * @throws IOException
   * @throws XMLStreamException 
   */
  private void writeSpaces( XMLStreamWriter writer, int n ) throws IOException, XMLStreamException
  {
    if ( n <= 0 )
      return;
    StringBuilder sb = new StringBuilder();
    for ( int i = 0; i < n; i++ )
      sb.append( ' ' );
    writer.writeCharacters( sb.toString() );
  }
}
