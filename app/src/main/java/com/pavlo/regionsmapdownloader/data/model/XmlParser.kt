package com.pavlo.regionsmapdownloader.data.model

import android.content.Context
import android.util.Xml
import org.xmlpull.v1.XmlPullParser

object XmlParser {
    fun parseItemsFromAssets(context: Context, fileName: String): List<RegionItem> {
        val inputStream = context.assets.open(fileName)

        val parser = Xml.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var eventType = parser.eventType

        val rootRegions = mutableListOf<RegionItem>()
        val stack = ArrayDeque<RegionItem>()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "region") {
                        val item = RegionItem(
                            name = parser.getAttributeValue(null, "name"),
                            map = parser.getAttributeValue(null, "map").toBoolean(),
                            regions = mutableListOf()
                        )

                        if (stack.isEmpty()) {
                            rootRegions.add(item)
                        }
                        else {
                            stack.last().regions?.add(item)
                        }
                        stack.addLast(item)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "region") {
                        stack.removeLast()
                    }
                }
            }
            eventType = parser.next()
        }

        inputStream.close()
        return rootRegions
    }
}