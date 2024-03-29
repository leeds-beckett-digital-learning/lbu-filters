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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import uk.ac.leedsbeckett.lbufilters.util.ChunkedFilterReader;

/**
 *
 * @author maber01
 */
public class PropertiesToXml extends ChunkedFilterReader
{
  public PropertiesToXml( Reader in )
  {
    super( in );
  }
  
  @Override
  public void firstChunk( Writer writer  ) throws IOException
  {
    Properties props = new Properties();
    props.load( in );
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    props.storeToXML( baos, "Normalised properties file.", "UTF-8" );
    writer.write( new String( baos.toByteArray(), "UTF-8" ) );
  }

  @Override
  public void nextChunk( Writer writer ) throws IOException
  {    
    setEnded();
  }
}
