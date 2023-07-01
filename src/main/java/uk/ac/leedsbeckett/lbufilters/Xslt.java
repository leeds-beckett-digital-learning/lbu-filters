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

import java.io.CharArrayReader;
import java.io.File;
import uk.ac.leedsbeckett.lbufilters.util.ChunkedFilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.Parameterizable;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import uk.ac.leedsbeckett.lbufilters.util.NoCloseReaderWrapper;

/**
 *
 * @author maber01
 */
public class Xslt extends ChunkedFilterReader implements Parameterizable
{
  String transform;
          
  public Xslt( Reader in )
  {
    super( in );
  }

  
  @Override
  public void setParameters( Parameter... prmtrs )
  {
    for ( Parameter p : prmtrs )
    {
      if ( "transform".equals( p.getName() ) )
        transform = p.getValue();
    }
    
  }  

  @Override
  public void nextChunk( Writer writer ) throws IOException
  {
    setEnded();
  }

  @Override
  public void firstChunk( Writer writer ) throws IOException
  {
    try
    {
      if ( transform == null )
        throw new IOException( "No transform parameter was specified." );
      File t = new File( transform );
      if ( !t.exists() )
        throw new IOException( "Specied transform does not exist." );
      if ( !t.isFile() )
        throw new IOException( "Specied transform is not a file." );

    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setValidating( false );
    SAXParser sp = spf.newSAXParser();
    InputSource s = new InputSource( new NoCloseReaderWrapper( in ) );
    System.out.println( "Saxsource getXMLReader " + sp.getXMLReader() );
    sp.getXMLReader().setEntityResolver( new EntityResolver(){
        @Override
        public InputSource resolveEntity( String publicId, String systemId ) throws SAXException, IOException
        {
          System.err.println( "publicId = [" + publicId + "] systemId = [" + systemId + "]" );
          if ( systemId == null )
            return null;
          if ( !systemId.startsWith( "http:" ) && !systemId.startsWith( "https:" ) ) 
            return null;
          return new InputSource( new CharArrayReader( new char[] {} ) );
        }
      } );
      SAXSource saxsource = new SAXSource( sp.getXMLReader(), s );


      TransformerFactory tFactory = TransformerFactory.newInstance();
      System.err.println( "TransformerFactory = " + tFactory.getClass().toString() );
      // Cannot do this in ant because of Java security settings
      //tFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false );
      // Can do this in ant so long as using very recent JDK that has built in
      // transformer factory that supports it.
      tFactory.setFeature( "http://www.oracle.com/xml/jaxp/properties/enableExtensionFunctions", true );
      tFactory.setAttribute( "jdk.xml.extensionClassLoader", this.getClass().getClassLoader() );
      Transformer transformer = tFactory.newTransformer( new StreamSource( t ) );
      
      //transformer.setURIResolver( new NoNetworkURIResolver() );
      StreamResult result = new StreamResult( writer );
      transformer.transform( saxsource, result );
    }
    catch ( TransformerConfigurationException ex )
    {
      ex.printStackTrace();
      throw new IOException( "Invalid transform config.", ex );
    }
    catch ( TransformerException ex )
    {
      ex.printStackTrace();
      throw new IOException( "Transform failed.", ex );
    }
    catch ( ParserConfigurationException ex )
    {
      ex.printStackTrace();
      throw new IOException( "Parser config failed.", ex );
    }
    catch ( SAXException ex )
    {
      ex.printStackTrace();
      throw new IOException( "SAX failed.", ex );
    }
  }
}
