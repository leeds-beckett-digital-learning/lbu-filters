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

import java.io.IOException;
import java.io.Writer;
import uk.ac.leedsbeckett.lbufilters.general.Element;
import uk.ac.leedsbeckett.lbufilters.general.Processor;

/**
 *
 * @author maber01
 */
public class Index implements Element
{
  Processor processor = null;
  String from;
  String to;

  public void setFrom( String from )
  {
    this.from = from;
  }

  public void setTo( String to )
  {
    this.to = to;
  }
  
  
  
  public void addElement( Processor p )
  {
    if ( processor != null )
      throw new IllegalArgumentException( "Already has a processor." );
  }
}
