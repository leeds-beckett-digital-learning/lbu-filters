<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:jst="http://www.oracle.com/XSL/Transform/java/uk.ac.leedsbeckett.lbufilters.StringTransforms">
  <xsl:output method="text" encoding="ISO-8859-1"/>
  
  <patterns id="findreplace">
    <pattern>
      <from>course</from>
      <to>module</to>
    </pattern>
    <pattern>
      <from>Course</from>
      <to>Module</to>
    </pattern>
  </patterns>
  

  <xsl:variable name="nl" select="'&#xa;'"/>



  <xsl:template match="/">
    <xsl:apply-templates select="*"/>
  </xsl:template>    

  <xsl:template match="properties">
    <xsl:apply-templates select="*"/>
  </xsl:template>    

  <!-- todo break comment into lines and add '#' to every one -->
  <xsl:template match="comment">
    <xsl:value-of select="jst:propertyComment( text() )"/>
  </xsl:template>
    
  <xsl:template match="entry">    
    <xsl:value-of select="jst:propertyKey( @key )"/>
    <xsl:value-of select="'='"/>
    <xsl:value-of select="jst:propertyValue( jst:multiReplace( text(), document('')/*/patterns[@id = 'findreplace'] ) )"/>
    <xsl:value-of select="$nl"/>
  </xsl:template>
    
</xsl:stylesheet>
