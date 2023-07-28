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
package uk.ac.leedsbeckett.lbufilters.general.nodes;

import uk.ac.leedsbeckett.lbufilters.general.Processor;
import java.io.IOException;
import java.io.Writer;
import uk.ac.leedsbeckett.lbufilters.general.BooleanElement;

/**
 *
 * @author maber01
 */
public class When extends Processor implements BooleanElement
{
  BooleanElement be = null;
  Processor processor = null;
  
  @Override
  public void process( Writer writer, String[] s ) throws IOException
  {
    if ( processor != null )
      processor.process( writer, s );
  }

  @Override
  public boolean getResult( String[] s )
  {
    if ( be != null )
      return be.getResult( s );
    return false;
  }

  
  public void addElement( Processor p )
  {
    if ( processor != null )
      throw new IllegalArgumentException( "Already has a processor." );
    processor = p;
  }

  public void addElement( BooleanElement be )
  {
    if ( this.be != null )
      throw new IllegalArgumentException( "Already has a boolean element." );
    this.be = be;
  }

}
