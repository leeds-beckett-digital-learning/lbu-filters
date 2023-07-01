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
import java.nio.CharBuffer;

/**
 *
 * @author maber01
 */
public class NoCloseReaderWrapper extends Reader
{
  final private Reader wrapped;

  public NoCloseReaderWrapper( Reader wrapped )
  {
    this.wrapped = wrapped;
  }

  @Override
  public long transferTo( Writer out ) throws IOException
  {
    return wrapped.transferTo( out );
  }

  @Override
  public void reset() throws IOException
  {
    wrapped.reset();
  }

  @Override
  public void mark( int readAheadLimit ) throws IOException
  {
    wrapped.mark( readAheadLimit );
  }

  @Override
  public boolean markSupported()
  {
    return wrapped.markSupported();
  }

  @Override
  public boolean ready() throws IOException
  {
    return wrapped.ready();
  }

  @Override
  public long skip( long n ) throws IOException
  {
    return wrapped.skip( n );
  }

  @Override
  public int read( char[] cbuf ) throws IOException
  {
    return wrapped.read( cbuf );
  }

  @Override
  public int read() throws IOException
  {
    return wrapped.read();
  }

  @Override
  public int read( CharBuffer target ) throws IOException
  {
    return wrapped.read( target );
  }

  @Override
  public int read( char[] cbuf, int off, int len ) throws IOException
  {
    return wrapped.read( cbuf, off, len );
  }

  @Override
  public void close() throws IOException
  {
    // does not close the wrapped reader
  }
  
}
