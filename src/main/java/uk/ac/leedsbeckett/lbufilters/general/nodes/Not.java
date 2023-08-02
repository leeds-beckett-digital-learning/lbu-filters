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

import java.util.ArrayList;
import uk.ac.leedsbeckett.lbufilters.general.BooleanElement;

/**
 *
 * @author maber01
 */
public class Not implements BooleanElement
{
  ArrayList<BooleanElement> bools = new ArrayList<>();
  
  @Override
  public boolean getResult( String[] s )
  {
    BooleanElement bool = bools.get( 0 );
    return !bool.getResult( s );
  }
  
  public void addElement( BooleanElement bool )
  {
    if ( bools.size() == 1 )
      throw new IllegalArgumentException( "Not element can only contain one boolean element." );
    bools.add( bool );
  }
}
