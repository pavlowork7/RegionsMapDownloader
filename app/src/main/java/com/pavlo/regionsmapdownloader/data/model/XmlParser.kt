package com.pavlo.regionsmapdownloader.data.model

import android.content.Context
import android.util.Xml
import org.xmlpull.v1.XmlPullParser

object XmlParser {
    private const val NAME_PLACEHOLDER = "\$name"
    private const val REGION_TAG = "region"
    private const val NAME_ATTR = "name"
    private const val MAP_ATTR = "map"
    private const val DOWNLOAD_PREFIX_ATTR = "download_prefix"
    private const val DOWNLOAD_SUFFIX_ATTR = "download_suffix"
    private const val INNER_DOWNLOAD_PREFIX = "inner_download_prefix"
    private const val INNER_DOWNLOAD_SUFFIX = "inner_download_suffix"
    private const val YES_VALUE = "yes"
    private data class DownloadContext(val prefix: String?, val suffix: String?)

    fun parseItemsFromAssets(context: Context, fileName: String): List<RegionItem> {
        val inputStream = context.assets.open(fileName)

        val parser = Xml.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var eventType = parser.eventType

        val rootRegions = mutableListOf<RegionItem>()
        val stack = ArrayDeque<RegionItem>()
        val contextStack = ArrayDeque<DownloadContext>()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == REGION_TAG) {
                        val name = parser.getAttributeValue(null, NAME_ATTR).orEmpty()
                        val parentContext = contextStack.lastOrNull() ?: DownloadContext(null, null)

                        val ownPrefix = parser.getAttributeValue(null, DOWNLOAD_PREFIX_ATTR) ?: parentContext.prefix
                        val ownSuffix = parser.getAttributeValue(null, DOWNLOAD_SUFFIX_ATTR) ?: parentContext.suffix

                        val innerPrefixAttr = parser.getAttributeValue(null, INNER_DOWNLOAD_PREFIX)
                        val innerSuffixAttr = parser.getAttributeValue(null, INNER_DOWNLOAD_SUFFIX)
                        val childPrefix = when (innerPrefixAttr) {
                            null -> parentContext.prefix
                            NAME_PLACEHOLDER -> name
                            else -> innerPrefixAttr
                        }
                        val childSuffix = innerSuffixAttr ?: parentContext.suffix

                        val item = RegionItem(
                            name = parser.getAttributeValue(null, NAME_ATTR),
                            map = parser.getAttributeValue(null, MAP_ATTR) == YES_VALUE,
                            downloadName = buildDownloadName(name, ownPrefix, ownSuffix),
                            regions = mutableListOf()
                        )

                        if (stack.isEmpty()) {
                            rootRegions.add(item)
                        }
                        else {
                            stack.last().regions?.add(item)
                        }
                        stack.addLast(item)
                        contextStack.addLast(DownloadContext(childPrefix, childSuffix))
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == REGION_TAG) {
                        stack.removeLast()
                        contextStack.removeLast()
                    }
                }
            }
            eventType = parser.next()
        }

        inputStream.close()
        return rootRegions
    }

    private fun buildDownloadName(name: String, prefix: String?, suffix: String?): String = when {
        prefix != null && suffix != null -> "${prefix}_${name}_${suffix}"
        suffix != null -> "${name}_${suffix}"
        prefix != null -> "${prefix}_${name}"
        else -> name
    }
}