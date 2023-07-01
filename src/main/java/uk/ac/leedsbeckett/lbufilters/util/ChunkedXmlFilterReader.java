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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author maber01
 */
public abstract class ChunkedXmlFilterReader extends ChunkedFilterReader
{
  XMLStreamWriter xmlwriter = null;
  
  public ChunkedXmlFilterReader( Reader in )
  {
    super( in );
  }

  public XMLStreamWriter getXmlChunkWriter() throws IOException
  {
    if ( xmlwriter == null )
      throw new IOException( "Unable to create xml stream writer." );
    return xmlwriter;
  }

  @Override
  public final void firstChunk( Writer writer ) throws IOException
  {  
    try
    {
      xmlwriter = XMLOutputFactory.newFactory().createXMLStreamWriter( writer );
    }
    catch ( XMLStreamException ex )
    {
      Logger.getLogger( ChunkedXmlFilterReader.class.getName() ).log( Level.SEVERE, null, ex );
    }
    firstChunk( xmlwriter );
  }
  
  @Override
  public final void nextChunk( Writer writer ) throws IOException
  {
    nextChunk( xmlwriter );
  }


  /**
   * Read filter input and create the next chunk.
   * 
   * @param writer Where chunks should be written.
   * @throws IOException 
   */
  abstract public void nextChunk( XMLStreamWriter writer ) throws IOException;
  
  
  /**
   * Create the first chunk. May or may not read filter input.
   * 
   * @param writer Where chunks should be written.
   * @throws IOException 
   */
  abstract public void firstChunk( XMLStreamWriter writer ) throws IOException;

}
