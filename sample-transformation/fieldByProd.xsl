<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output omit-xml-declaration="yes"/>

<xsl:template match="Row">

  <doc>

      <xsl:for-each select="*">

         <field>

           <xsl:attribute name="name"><xsl:value-of select="name(.)"/> </xsl:attribute>

           <xsl:value-of select="." />

         </field>

      </xsl:for-each>

  </doc>

</xsl:template>

</xsl:stylesheet>
