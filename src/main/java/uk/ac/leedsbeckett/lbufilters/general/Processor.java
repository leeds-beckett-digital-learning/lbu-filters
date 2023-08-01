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
package uk.ac.leedsbeckett.lbufilters.general;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 *
 * @author maber01
 */
public abstract class Processor implements Element
{
  int from = Integer.MIN_VALUE;
  int to = Integer.MAX_VALUE;
  int div = 0;
  int mod = 0;
  
  ArrayList<Processor> processors = new ArrayList<>();
  
  public void setFrom( String from )
  {
    this.from = Integer.parseInt( from );
  }

  public void setTo( String to )
  {
    this.to = Integer.parseInt( to );
  }

  public void setDiv( String div )
  {
    this.div = Integer.parseInt( div );
  }

  public void setMod( String mod )
  {
    this.mod = Integer.parseInt( mod );
  }

  public int getFrom()
  {
    return from;
  }

  public int getTo()
  {
    return to;
  }

  public int getDiv()
  {
    return div;
  }

  public int getMod()
  {
    return mod;
  }
  
  public boolean isIndexMatch( int i )
  {
    if ( i < from ) return false;
    if ( i > to ) return false;
    if ( div <=0 ) return true;
    int m = i % div;
    return m == mod;
  }

  public void addElement( Processor processor )
  {
    processors.add( processor );
  }
  
  public abstract void process( Writer writer, String[] s ) throws IOException;

  public void processChildren( Writer writer, String[] output ) throws IOException
  {
    for ( Processor p : processors )
    {
      ArrayList<String> list = new ArrayList<>();
      for ( int i=0; i<output.length; i++ )
        if ( p.isIndexMatch( i ) )
          list.add( output[i] );
      if ( !list.isEmpty() )
        p.process(writer, list.toArray(String[]::new) );
    }
  }
}
