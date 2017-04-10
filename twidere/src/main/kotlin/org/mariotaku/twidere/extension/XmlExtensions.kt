package org.mariotaku.twidere.extension

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer
import java.io.*
import java.nio.charset.Charset

/**
 * Created by mariotaku on 2017/1/6.
 */

@Throws(IOException::class)
fun InputStream.newPullParser(charset: Charset = Charset.defaultCharset()): XmlPullParser {
    val parser = Xml.newPullParser()
    parser.setInput(InputStreamReader(this, charset))
    return parser
}

@Throws(IOException::class)
fun OutputStream.newSerializer(charset: Charset = Charset.defaultCharset(),
        indent: Boolean = true): XmlSerializer {
    val serializer = Xml.newSerializer()
    serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", indent)
    serializer.setOutput(OutputStreamWriter(this, charset))
    return serializer
}
