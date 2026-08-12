package com.pavlo.regionsmapdownloader.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.kxml2.io.KXmlParser
import java.io.ByteArrayInputStream

class XmlParserTest {

    private fun parse(xml: String): List<RegionItem> =
        XmlParser.parse(ByteArrayInputStream(xml.toByteArray()), parserFactory = ::KXmlParser)

    @Test
    fun `download name without prefix or suffix stays as name`() {
        val xml = """
            <regions_list>
                <region name="switzerland" map="yes"/>
            </regions_list>
        """.trimIndent()

        assertEquals("switzerland", parse(xml).single().downloadName)
    }

    @Test
    fun `child inherits inner_download_suffix from continent`() {
        val xml = """
            <regions_list>
                <region name="europe" type="continent" inner_download_suffix="europe">
                    <region name="estonia" map="yes"/>
                </region>
            </regions_list>
        """.trimIndent()

        val estonia = parse(xml).single().regions!!.single()

        assertEquals("estonia_europe", estonia.downloadName)
    }

    @Test
    fun `inner_download_prefix dollar-name placeholder resolves to the parent's own name`() {
        val xml = """
            <regions_list>
                <region name="europe" type="continent" inner_download_suffix="europe">
                    <region name="denmark" inner_download_prefix="${'$'}name">
                        <region name="capital-region" map="yes"/>
                    </region>
                </region>
            </regions_list>
        """.trimIndent()

        val capitalRegion = parse(xml).single().regions!!.single().regions!!.single()

        assertEquals("denmark_capital-region_europe", capitalRegion.downloadName)
    }

    @Test
    fun `suffix is inherited two levels down from the grandparent`() {
        val xml = """
            <regions_list>
                <region name="europe" type="continent" inner_download_suffix="europe">
                    <region name="denmark">
                        <region name="capital-region" map="yes"/>
                    </region>
                </region>
            </regions_list>
        """.trimIndent()

        val capitalRegion = parse(xml).single().regions!!.single().regions!!.single()

        assertEquals("capital-region_europe", capitalRegion.downloadName)
    }

    @Test
    fun `map attribute controls isMap independently of a continent's type`() {
        val xml = """
            <regions_list>
                <region name="europe" type="continent">
                    <region name="estonia" map="yes"/>
                </region>
            </regions_list>
        """.trimIndent()

        val europe = parse(xml).single()
        val estonia = europe.regions!!.single()

        assertFalse(europe.map ?: false)
        assertTrue(estonia.map ?: false)
    }

    @Test
    fun `leaf regions have no children`() {
        val xml = """
            <regions_list>
                <region name="europe" type="continent">
                    <region name="estonia" map="yes"/>
                </region>
            </regions_list>
        """.trimIndent()

        val estonia = parse(xml).single().regions!!.single()

        assertTrue(estonia.regions.orEmpty().isEmpty())
    }
}
