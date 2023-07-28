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
package uk.ac.leedsbeckett.lbufilters.general.parse;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import uk.ac.leedsbeckett.lbufilters.general.BooleanElement;
import uk.ac.leedsbeckett.lbufilters.general.Element;
import uk.ac.leedsbeckett.lbufilters.general.Processor;
import uk.ac.leedsbeckett.lbufilters.general.nodes.Tokenise;

/**
 *
 * @author maber01
 */
public class Handler extends DefaultHandler
{
  Tokenise root = null;
  Stack<Element> stack = new Stack<>();
  
  @Override
  public void endElement( String uri, String localName, String qName ) throws SAXException
  {
    if ( !stack.isEmpty() )
      stack.pop();
  }

  @Override
  public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException
  {
    if ( root == null )
    {
      if ( !"tokenise".equals( localName ) )
        throw new SAXException( "Root element must be tokenise." );
      root = new Tokenise();
      stack.push( root );
      return;
    }
    
    String className = "uk.ac.leedsbeckett.lbufilters.general.nodes." + capitalise( localName );
    Class c;
    Constructor con;
    Object o;
    try
    {
      c = Class.forName( className );
      con = c.getConstructor();
      o = con.newInstance();
    }
    catch ( ClassNotFoundException ex )
    {
      throw new SAXException( "Unknown element name " + localName );
    }
    catch ( NoSuchMethodException ex )
    {
      throw new SAXException( "No constructor for name " + localName );      
    }
    catch ( InstantiationException | 
            IllegalAccessException | 
            IllegalArgumentException |
            InvocationTargetException ex )
    {
      throw new SAXException( "Unable to create element " + localName );      
    }

    if ( !(o instanceof Element) )
      throw new SAXException( "New element doesn't implement 'Element' " + localName );      

    for ( int i=0; i<attributes.getLength(); i++ )
    {
      String setterName = "set" + capitalise( attributes.getLocalName( i ) );
      try
      {
        Method setter = c.getMethod( setterName, String.class );
        setter.invoke( o, attributes.getValue( i ) );
      }
      catch ( NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex )
      {
        throw new SAXException( "Class " + c.toString() + " doesn't have method " + setterName );      
      }
    }
    
    Element current = (Element)o;
    Element parent = stack.peek();
    Class pclass = parent.getClass();
    Method adder = null;
    
    ArrayList<Class> testlist = new ArrayList<>();
    testlist.add( current.getClass() );
    if ( current instanceof Processor )
      testlist.add( Processor.class );
    if ( current instanceof BooleanElement )
      testlist.add( BooleanElement.class );
    
    for ( Class testclass : testlist )
    {
      try
      {
        adder = pclass.getMethod( "addElement", testclass );
      }
      catch ( NoSuchMethodException | SecurityException ex )
      {
        adder = null;
      }
      if ( adder != null )
        break;
    }
    
    if ( adder == null )
      throw new SAXException( "Cannot put " + localName + " here." );
    
    try
    {
      adder.invoke( parent, current );
    }
    catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException ex )
    {
      throw new SAXException( "Failed to add " + localName + " here." );
    }
    
    stack.push( current );
  }

  public Tokenise getTokenise()
  {
    return root;
  }
  
  public static String capitalise( String s )
  {
    char[] c = s.toCharArray();
    if ( c.length > 0 )
      c[0] = Character.toUpperCase( c[0] );
    return new String( c );
  }
}
