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

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 *
 * @author maber01
 */
public abstract class ChunkedFilterReader extends FilterReader
{
  private boolean started = false;
  private boolean ended = false;
  private final CharArrayWriter chunkWriter = new CharArrayWriter();
  private CharArrayReader chunkReader;

  /**
   * Standard FilterReader constructor.
   * 
   * @param in The reader that this filter will process.
   */
  public ChunkedFilterReader( Reader in )
  {
    super( in );
  }

  /**
   * Has processing ended.
   * 
   * @return The ended property.
   */
  public boolean isEnded()
  {
    return ended;
  }

  /**
   * Indicate if that processing has ended when no more chunks will be
   * output.
   * 
   * @param ended 
   */
  public void setEnded()
  {
    this.ended = true;
  }
  
  /**
   * Reads a character from the most recent output chunk but if that is
   * already empty fetches the next chunk.
   * 
   * @return A character as an int or -1 if at end of file.
   * @throws IOException 
   */
  @Override
  public int read() throws IOException
  {
    if ( !started )
      getFirstChunk();
    int c = chunkReader.read();
    if ( c < 0 && !ended )
    {
      getNextChunk();
      c = chunkReader.read();
    }
    //System.err.println( "Sending " + c );
    return c;
  }

  /**
   * Reads characters from the most recent output chunk but if that is
   * already empty fetches the next chunk.
   * 
   * @param cbuf Destination buffer.
   * @param off Offset into that buffer.
   * @param len Maximum number of characters.
   * @return The number of characters read or -1 if at end of file.
   * @throws IOException 
   */
  @Override
  public int read( char[] cbuf, int off, int len ) throws IOException
  {
    if ( !started )
      getFirstChunk();
    int n = chunkReader.read( cbuf, off, len );
    if ( n < 0 && !ended )
    {
      getNextChunk();
      n = chunkReader.read( cbuf, off, len );
      //System.err.println( "Sending " + n + " chars." );
    }
    return n;
  }


  /**
   * This abstract class uses this method to process the next chunk.
   * 
   * @throws IOException 
   */  
  void getNextChunk() throws IOException
  {
    // empty the chunk receiver
    chunkWriter.reset();
    // fill with another chunk
    while ( !ended && chunkWriter.size()==0 )
      nextChunk( chunkWriter );
    // move chunk into the output chunk
    //System.err.println( "Chunk [" + chunkWriter.toString() + "]" );
    chunkReader = new CharArrayReader( chunkWriter.toCharArray() );
  }
  
  /**
   * This abstract class uses this method to process the first chunk.
   * 
   * @throws IOException 
   */  
  void getFirstChunk() throws IOException
  {
    started=true;
    // empty the chunk receiver
    chunkWriter.reset();
    // fill with another chunk
    firstChunk( chunkWriter );
    while ( !ended && chunkWriter.size()==0 )
      nextChunk( chunkWriter );
    // move chunk into the output chunk
    //System.err.println( "Chunk [" + chunkWriter.toString() + "]" );
    chunkReader = new CharArrayReader( chunkWriter.toCharArray() );
  }
    
  /**
   * Read filter input and create the next chunk.
   * 
   * @param writer Where chunks should be written.
   * @throws IOException 
   */
  abstract public void nextChunk( Writer writer ) throws IOException;
  
  
  /**
   * Create the first chunk. May or may not read filter input.
   * 
   * @param writer Where chunks should be written.
   * @throws IOException 
   */
  abstract public void firstChunk( Writer writer ) throws IOException;
}
