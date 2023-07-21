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

import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *
 * @author maber01
 */
public class StringTransforms
{

  public static String jsonString( String s )
  {
    StringBuilder hex = new StringBuilder();
    StringBuilder sb = new StringBuilder();
    sb.append( "\"" );
    char[] a = s.toCharArray();
    for ( int i=0; i<a.length; i++ )
    {
      if ( a[i] == '\\' )
        sb.append( "\\\\" );
      else if ( a[i] == '"' )
        sb.append( "\"" );
      else if ( a[i] >= ' ' && a[i] <= '~')
        sb.append( a[i] );
      else if ( a[i] >= '\u00a1' )
        sb.append( a[i] );
      else if ( a[i] == '\b' )
        sb.append( "\\b" );
      else if ( a[i] == '\f' )
        sb.append( "\\f" );
      else if ( a[i] == '\n' )
        sb.append( "\\n" );
      else if ( a[i] == '\r' )
        sb.append( "\\r" );
      else if ( a[i] == '\t' )
        sb.append( "\\t" );
      else
      {
        // hex escape needed
        hex.setLength( 0 );
        hex.append( Integer.toHexString( (int)a[i] ) );
        while ( hex.length() < 4 )
          hex.insert( 0, '0' );
        sb.append( "\\u" );
        sb.append(  hex.toString() );
      }
    }
    sb.append( "\"" );
    return sb.toString();
  }
  
  public static String multiReplace( String source, NodeList o )
  {
    if ( o == null ) return "Can't find replace. No patterns object provided.";
    if ( o.getLength() != 1 ) return "Can't find replace. Need single item node list.";
    Node n = o.item( 0 );
    if ( n.getNodeType() != Node.ELEMENT_NODE ) return "Can't find replace. Node is not an element.";
    NodeList nl = n.getChildNodes();
    ArrayList<Element> children=new ArrayList<>();
    for ( int i=0; i<nl.getLength(); i++ )
    {
      if ( nl.item( i ).getNodeType() == Node.ELEMENT_NODE )
        children.add( (Element)nl.item( i ) );
    }
    if ( children.size() == 0 ) return "Can't find replace. Node does not contain any pattern elements.";
    
    String current=source;
    String from, to;
    for ( Element pattern : children )
    {
      nl = pattern.getElementsByTagName( "from" );
      if ( nl.getLength() != 1 ) return "Can't find replace. Pattern must contain one from element.";
      from = nl.item( 0 ).getTextContent();
      if ( from == null || from.length() == 0 ) return "Can't find replace. Element from must contain some text.";
      nl = pattern.getElementsByTagName( "to" );
      if ( nl.getLength() != 1 ) return "Can't find replace. Pattern must contain one to element.";
      to = nl.item( 0 ).getTextContent();
      if ( to == null || to.length() == 0 ) return "Can't find replace. Element to must contain some text.";
      current = current.replace( from, to );
    }
    
    return current;
  }
  
  public static String propertyComment( String in )
  {
    return escapePropertyString( in, true, false, true, false );
  }

  public static String propertyKey( String in )
  {
    return escapePropertyString( in, false, true, true, true );
  }

  public static String propertyValue( String in )
  {
    return escapePropertyString( in, false, false, true, false );
  }

  private static String escapePropertyString( String in,
          boolean comment, 
          boolean escapeSpace, 
          boolean escapeUnicode,
          boolean escapeSpecial )
  {
    int len = in.length();
    int bufLen = len * 2;
    if ( bufLen < 0 )
    {
      bufLen = Integer.MAX_VALUE;
    }
    StringBuilder builder = new StringBuilder( bufLen );

    boolean needshash = comment && in.trim().length() > 0;
    char[] a = in.toCharArray();
    if ( needshash )
      builder.append( "# " );
    for ( int i = 0; i < a.length; i++ )
      escapePropertyCharacter( builder, a[ i ], i == 0, i == (a.length-1), true, needshash, escapeSpace, escapeUnicode, escapeSpecial );      
    return builder.toString();
  }

  private static void escapePropertyCharacter( 
          StringBuilder outBuffer, 
          char aChar, 
          boolean first, 
          boolean last, 
          boolean comment, 
          boolean needshash, 
          boolean escapeSpace, 
          boolean escapeUnicode,
          boolean escapeSpecial )
  {
    // Handle common case first, selecting largest block that
    // avoids the specials below
    if ( ( aChar > 61 ) && ( aChar < 127 ) )
    {
      if ( aChar == '\\' )
      {
        outBuffer.append( '\\' );
        outBuffer.append( '\\' );
        return;
      }
      outBuffer.append( aChar );
      return;
    }
    switch ( aChar )
    {
      case ' ':
        if ( (first || escapeSpace) && !comment )
        {
          outBuffer.append( '\\' );
        }
        outBuffer.append( ' ' );
        break;
      case '\t':
        outBuffer.append( '\\' );
        outBuffer.append( 't' );
        break;
      case '\n':
        if ( !comment )
        {
          outBuffer.append( '\\' );
          outBuffer.append( 'n' );
        }
        else
          outBuffer.append( aChar );
        if ( needshash && !last )
         outBuffer.append( "# " );      
        break;
      case '\r':
        if ( !comment )
        {
          outBuffer.append( '\\' );
          outBuffer.append( 'r' );
        }
        else
          outBuffer.append( aChar );
        break;
      case '\f':
        outBuffer.append( '\\' );
        outBuffer.append( 'f' );
        break;
      case '=': // Fall through
      case ':': // Fall through
      case '#': // Fall through
      case '!':
        if ( escapeSpecial )
        {
          outBuffer.append( '\\' );
          outBuffer.append( aChar );
          break;
        }
      default:
        if ( ( ( aChar < 0x0020 ) || ( aChar > 0x007e ) ) & escapeUnicode )
        {
          outBuffer.append( "\\u" );
          String number = Integer.toHexString( aChar ).toUpperCase();
          for ( int i=number.length(); i<4; i++ )
            outBuffer.append( "0" );
          outBuffer.append( number );
        }
        else
        {
          outBuffer.append( aChar );
        }
    }
  }
}
