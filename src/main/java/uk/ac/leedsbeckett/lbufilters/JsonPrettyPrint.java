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

import uk.ac.leedsbeckett.lbufilters.util.NoCloseReaderWrapper;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import uk.ac.leedsbeckett.lbufilters.util.ChunkedFilterReader;

/**
 * Reads in JSON using Jackson streaming API and produces pretty printed
 * JSON.
 * 
 * @author maber01
 */
public class JsonPrettyPrint extends ChunkedFilterReader
{
  JsonFactory jsonFactory;
  JsonParser jsonParser = null;
  JsonGenerator jsonGenerator = null;
  
  /**
   * Standard FilterReader constructor.
   * 
   * @param in The filter source reader.
   */
  public JsonPrettyPrint( Reader in )
  {
    super( in );
    jsonFactory = new JsonFactory();
  }

  /**
   * Pump through some more JSON.
   * @param writer
   * @throws IOException 
   */
  @Override
  public void nextChunk( Writer writer ) throws IOException
  {
    JsonToken token = jsonParser.nextToken();
    if ( token == null )
    {
      setEnded();
      jsonParser.close();
      jsonGenerator.close();
      return;
    }
    
    switch ( token )
    {
      case START_OBJECT:
        jsonGenerator.writeStartObject();
        break;
      case END_OBJECT:
        jsonGenerator.writeEndObject();
        break;
      case START_ARRAY:
        jsonGenerator.writeStartArray();
        break;
      case END_ARRAY:
        jsonGenerator.writeEndArray();
        break;
      case FIELD_NAME:
        jsonGenerator.writeFieldName( jsonParser.currentName() );
        break;
      case VALUE_NULL:
        jsonGenerator.writeNull();
        break;
      case VALUE_STRING:
        jsonGenerator.writeString( jsonParser.getValueAsString() );
        break;
      case VALUE_NUMBER_INT:
        jsonGenerator.writeNumber( jsonParser.getValueAsLong() );
        break;
      case VALUE_NUMBER_FLOAT:
        jsonGenerator.writeNumber( jsonParser.getValueAsDouble() );
        break;
      case VALUE_FALSE:
        jsonGenerator.writeBoolean( false );
        break;
      case VALUE_TRUE:
        jsonGenerator.writeBoolean( true );
        break;
    }
  }

  /**
   * Prepare to parse and generate JSON.
   * 
   * @param writer
   * @throws IOException 
   */
  @Override
  public void firstChunk( Writer writer ) throws IOException
  {
    jsonParser = jsonFactory.createParser( new NoCloseReaderWrapper( in ) );
    jsonGenerator = jsonFactory.createGenerator( writer );
    jsonGenerator.setPrettyPrinter( new DefaultPrettyPrinter() );
  }
}
