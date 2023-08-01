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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.Parameterizable;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import uk.ac.leedsbeckett.lbufilters.general.nodes.Tokenise;
import uk.ac.leedsbeckett.lbufilters.general.parse.Handler;
import uk.ac.leedsbeckett.lbufilters.util.ChunkedFilterReader;

/**
 *
 * @author maber01
 */
public class GeneralFilter extends ChunkedFilterReader implements Parameterizable
{    
  int count = 0;

  String spec;
  
  Tokenise tokenise;
  
  public GeneralFilter( Reader in )
  {
    super( in );
  }

  @Override
  public void setParameters( Parameter... prmtrs )
  {
    for ( Parameter p : prmtrs )
    {
      if ( "specification".equals( p.getName() ) )
        spec = p.getValue();
    }
    
  }  
  
  @Override
  public void firstChunk( Writer writer ) throws IOException
  {
    if ( tokenise == null )
    {
      if ( spec == null )
        throw new IOException( "No specification parameter was specified." );
      File file = new File( spec );
      if ( !file.exists() )
        throw new IOException( "Specied spec file does not exist." );
      if ( !file.isFile() )
        throw new IOException( "Specied spec is not a file." );
      setSpec( new FileReader( file ) );
    }
    tokenise.setReader( this.in );
  }

  @Override
  public void nextChunk( Writer writer ) throws IOException
  {
    if ( !tokenise.next( writer ) )
      setEnded();
    
    count++;
  }
  
  private void setSpec( Reader reader )
  {
      Handler handler = new Handler();
      
    try
    {
      SAXParserFactory spf = SAXParserFactory.newInstance();    
      spf.setNamespaceAware(true);
      SAXParser saxParser = spf.newSAXParser();
      XMLReader xmlReader = saxParser.getXMLReader();
      xmlReader.setContentHandler( handler );
      xmlReader.parse( new InputSource( reader ) );
    }
    catch ( ParserConfigurationException | SAXException | IOException ex )
    {
      Logger.getLogger( GeneralFilter.class.getName() ).log( Level.SEVERE, null, ex );
    }
    
    tokenise = handler.getTokenise();
  }
  
  public static void main( String[] args )
  {
    StringWriter writer = new StringWriter();
    try ( StringReader specreader = new StringReader( TESTSPEC );
          StringReader textreader = new StringReader( TESTTEXT ); )
    {
      GeneralFilter gf = new GeneralFilter( textreader );
      gf.setSpec( specreader );
      int c;
      while ( ( c = gf.read() ) >= 0 )
        writer.write( c );
    }
    catch ( IOException ex )
    {
      Logger.getLogger( GeneralFilter.class.getName() ).log( Level.SEVERE, null, ex );
    }
    System.out.println( writer.toString() );
  }
  
  private static final String TESTSPEC = 
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<tokenise>\n" +
"  <delimiter value=\"&#x0d;&#x0a;\"/>\n" +
"  <delimiter value=\"&#x0d;\"/>\n" +
"  <delimiter value=\"&#x0a;\"/>\n" +
"  <choose div=\"2\" mod=\"0\">\n" +
"    <when>\n" +
"      <or><matches spec=\"^[ \t]*#.*$\"/><matches spec=\"^[ \t]*$\"/></or>\n" +
"      <upper><output/></upper>\n" +
"    </when>\n" +
"    <otherwise>\n" +
"      <choose>\n" +
"        <when>\n" +
"          <matches index=\"0\" spec=\"thingy[ ]*.*\"/>" +
"          <output/>"+
"        </when>\n" +          
"        <otherwise>\n" +          
"          <split spec=\"((?&lt;=\\=))\" limit=\"2\">\n" +
"            <output from=\"0\" to=\"0\"/>\n" +
"            <literal>{</literal>\n" +
"            <replace from=\"1\" to=\"1\" match=\"=\" with=\"\\\\=\">"+ 
"              <replace match=\"m\" with=\"M\">"+ 
"                <output/>"+
"              </replace>\n" +
"            </replace>\n" +
"            <literal>}</literal>\n" +
"          </split>\n" +
"        </otherwise>\n" +          
"      </choose>\n" +          
          
"    </otherwise>\n" +
"  </choose>\n" +
"  <output div=\"2\" mod=\"1\"/>\n" +
"</tokenise>\n" +
"";
  
  private static final String TESTTEXT = 
"# comment\n" +
" # comment = with equals \n\n\n" +
"\r" +
"thingy=whatsit\r\n" +
"doodah =thingumy=sommat\n" +
"";
  
}

