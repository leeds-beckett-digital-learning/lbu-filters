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
import java.util.ArrayList;

/**
 *
 * @author maber01
 */
public class Choose extends Processor
{
  ArrayList<When> whens = new ArrayList<>();
  Otherwise otherwise = null;
  
  @Override
  public void process( Writer writer, String[] s ) throws IOException
  {
    for ( When w : whens )
    {
      if ( w.getResult( s ) )
      {
        w.process( writer, s );
        return;
      }
    }
    if ( otherwise != null )
      otherwise.process( writer, s );
  }

  public void addElement( When when )
  {
    whens.add( when );
  }

  public void addElement( Otherwise otherwise )
  {
    if ( this.otherwise != null )
      throw new IllegalArgumentException( "Only one otherwise element allowed." );
    this.otherwise = otherwise;
  }  
}
