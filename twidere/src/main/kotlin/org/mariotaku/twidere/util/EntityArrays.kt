/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mariotaku.twidere.util

import java.util.*

/**
 * Class holding various entity data for HTML and XML - generally for use with
 * the CustomEmojiTranslator.
 * All Maps are generated using `java.util.Collections.unmodifiableMap()`.
 *
 * @since 1.0
 */
object EntityArrays {

    /**
     * A Map&lt;CharSequence, CharSequence&gt; to to escape
     * [ISO-8859-1](https://secure.wikimedia.org/wikipedia/en/wiki/ISO/IEC_8859-1)
     * characters to their named HTML 3.x equivalents.
     */
    val ISO8859_1_ESCAPE: Map<CharSequence, CharSequence> = mapOf(
            "\u00A0" to "&nbsp;", // non-breaking space
            "\u00A1" to "&iexcl;", // inverted exclamation mark
            "\u00A2" to "&cent;", // cent sign
            "\u00A3" to "&pound;", // pound sign
            "\u00A4" to "&curren;", // currency sign
            "\u00A5" to "&yen;", // yen sign = yuan sign
            "\u00A6" to "&brvbar;", // broken bar = broken vertical bar
            "\u00A7" to "&sect;", // section sign
            "\u00A8" to "&uml;", // diaeresis = spacing diaeresis
            "\u00A9" to "&copy;", // © - copyright sign
            "\u00AA" to "&ordf;", // feminine ordinal indicator
            "\u00AB" to "&laquo;", // left-pointing double angle quotation mark = left pointing guillemet
            "\u00AC" to "&not;", // not sign
            "\u00AD" to "&shy;", // soft hyphen = discretionary hyphen
            "\u00AE" to "&reg;", // ® - registered trademark sign
            "\u00AF" to "&macr;", // macron = spacing macron = overline = APL overbar
            "\u00B0" to "&deg;", // degree sign
            "\u00B1" to "&plusmn;", // plus-minus sign = plus-or-minus sign
            "\u00B2" to "&sup2;", // superscript two = superscript digit two = squared
            "\u00B3" to "&sup3;", // superscript three = superscript digit three = cubed
            "\u00B4" to "&acute;", // acute accent = spacing acute
            "\u00B5" to "&micro;", // micro sign
            "\u00B6" to "&para;", // pilcrow sign = paragraph sign
            "\u00B7" to "&middot;", // middle dot = Georgian comma = Greek middle dot
            "\u00B8" to "&cedil;", // cedilla = spacing cedilla
            "\u00B9" to "&sup1;", // superscript one = superscript digit one
            "\u00BA" to "&ordm;", // masculine ordinal indicator
            "\u00BB" to "&raquo;", // right-pointing double angle quotation mark = right pointing guillemet
            "\u00BC" to "&frac14;", // vulgar fraction one quarter = fraction one quarter
            "\u00BD" to "&frac12;", // vulgar fraction one half = fraction one half
            "\u00BE" to "&frac34;", // vulgar fraction three quarters = fraction three quarters
            "\u00BF" to "&iquest;", // inverted question mark = turned question mark
            "\u00C0" to "&Agrave;", // À - uppercase A, grave accent
            "\u00C1" to "&Aacute;", // Á - uppercase A, acute accent
            "\u00C2" to "&Acirc;", // Â - uppercase A, circumflex accent
            "\u00C3" to "&Atilde;", // Ã - uppercase A, tilde
            "\u00C4" to "&Auml;", // Ä - uppercase A, umlaut
            "\u00C5" to "&Aring;", // Å - uppercase A, ring
            "\u00C6" to "&AElig;", // Æ - uppercase AE
            "\u00C7" to "&Ccedil;", // Ç - uppercase C, cedilla
            "\u00C8" to "&Egrave;", // È - uppercase E, grave accent
            "\u00C9" to "&Eacute;", // É - uppercase E, acute accent
            "\u00CA" to "&Ecirc;", // Ê - uppercase E, circumflex accent
            "\u00CB" to "&Euml;", // Ë - uppercase E, umlaut
            "\u00CC" to "&Igrave;", // Ì - uppercase I, grave accent
            "\u00CD" to "&Iacute;", // Í - uppercase I, acute accent
            "\u00CE" to "&Icirc;", // Î - uppercase I, circumflex accent
            "\u00CF" to "&Iuml;", // Ï - uppercase I, umlaut
            "\u00D0" to "&ETH;", // Ð - uppercase Eth, Icelandic
            "\u00D1" to "&Ntilde;", // Ñ - uppercase N, tilde
            "\u00D2" to "&Ograve;", // Ò - uppercase O, grave accent
            "\u00D3" to "&Oacute;", // Ó - uppercase O, acute accent
            "\u00D4" to "&Ocirc;", // Ô - uppercase O, circumflex accent
            "\u00D5" to "&Otilde;", // Õ - uppercase O, tilde
            "\u00D6" to "&Ouml;", // Ö - uppercase O, umlaut
            "\u00D7" to "&times;", // multiplication sign
            "\u00D8" to "&Oslash;", // Ø - uppercase O, slash
            "\u00D9" to "&Ugrave;", // Ù - uppercase U, grave accent
            "\u00DA" to "&Uacute;", // Ú - uppercase U, acute accent
            "\u00DB" to "&Ucirc;", // Û - uppercase U, circumflex accent
            "\u00DC" to "&Uuml;", // Ü - uppercase U, umlaut
            "\u00DD" to "&Yacute;", // Ý - uppercase Y, acute accent
            "\u00DE" to "&THORN;", // Þ - uppercase THORN, Icelandic
            "\u00DF" to "&szlig;", // ß - lowercase sharps, German
            "\u00E0" to "&agrave;", // à - lowercase a, grave accent
            "\u00E1" to "&aacute;", // á - lowercase a, acute accent
            "\u00E2" to "&acirc;", // â - lowercase a, circumflex accent
            "\u00E3" to "&atilde;", // ã - lowercase a, tilde
            "\u00E4" to "&auml;", // ä - lowercase a, umlaut
            "\u00E5" to "&aring;", // å - lowercase a, ring
            "\u00E6" to "&aelig;", // æ - lowercase ae
            "\u00E7" to "&ccedil;", // ç - lowercase c, cedilla
            "\u00E8" to "&egrave;", // è - lowercase e, grave accent
            "\u00E9" to "&eacute;", // é - lowercase e, acute accent
            "\u00EA" to "&ecirc;", // ê - lowercase e, circumflex accent
            "\u00EB" to "&euml;", // ë - lowercase e, umlaut
            "\u00EC" to "&igrave;", // ì - lowercase i, grave accent
            "\u00ED" to "&iacute;", // í - lowercase i, acute accent
            "\u00EE" to "&icirc;", // î - lowercase i, circumflex accent
            "\u00EF" to "&iuml;", // ï - lowercase i, umlaut
            "\u00F0" to "&eth;", // ð - lowercase eth, Icelandic
            "\u00F1" to "&ntilde;", // ñ - lowercase n, tilde
            "\u00F2" to "&ograve;", // ò - lowercase o, grave accent
            "\u00F3" to "&oacute;", // ó - lowercase o, acute accent
            "\u00F4" to "&ocirc;", // ô - lowercase o, circumflex accent
            "\u00F5" to "&otilde;", // õ - lowercase o, tilde
            "\u00F6" to "&ouml;", // ö - lowercase o, umlaut
            "\u00F7" to "&divide;", // division sign
            "\u00F8" to "&oslash;", // ø - lowercase o, slash
            "\u00F9" to "&ugrave;", // ù - lowercase u, grave accent
            "\u00FA" to "&uacute;", // ú - lowercase u, acute accent
            "\u00FB" to "&ucirc;", // û - lowercase u, circumflex accent
            "\u00FC" to "&uuml;", // ü - lowercase u, umlaut
            "\u00FD" to "&yacute;", // ý - lowercase y, acute accent
            "\u00FE" to "&thorn;", // þ - lowercase thorn, Icelandic
            "\u00FF" to "&yuml;" // ÿ - lowercase y, umlaut
    )

    /**
     * Reverse of [.ISO8859_1_ESCAPE] for unescaping purposes.
     */
    val ISO8859_1_UNESCAPE: Map<CharSequence, CharSequence> = ISO8859_1_ESCAPE.invert()

    /**
     * A Map&lt;CharSequence, CharSequence&gt; to escape additional
     * [character entity references](http://www.w3.org/TR/REC-html40/sgml/entities.html).
     * Note that this must be used with [.ISO8859_1_ESCAPE] to get the full list of
     * HTML 4.0 character entities.
     */
    val HTML40_EXTENDED_ESCAPE: Map<CharSequence, CharSequence> = mapOf(
            // <!-- Latin Extended-B -->
            "\u0192" to "&fnof;", // latin small f with hook = function= florin, U+0192 ISOtech -->
            // <!-- Greek -->
            "\u0391" to "&Alpha;", // greek capital letter alpha, U+0391 -->
            "\u0392" to "&Beta;", // greek capital letter beta, U+0392 -->
            "\u0393" to "&Gamma;", // greek capital letter gamma,U+0393 ISOgrk3 -->
            "\u0394" to "&Delta;", // greek capital letter delta,U+0394 ISOgrk3 -->
            "\u0395" to "&Epsilon;", // greek capital letter epsilon, U+0395 -->
            "\u0396" to "&Zeta;", // greek capital letter zeta, U+0396 -->
            "\u0397" to "&Eta;", // greek capital letter eta, U+0397 -->
            "\u0398" to "&Theta;", // greek capital letter theta,U+0398 ISOgrk3 -->
            "\u0399" to "&Iota;", // greek capital letter iota, U+0399 -->
            "\u039A" to "&Kappa;", // greek capital letter kappa, U+039A -->
            "\u039B" to "&Lambda;", // greek capital letter lambda,U+039B ISOgrk3 -->
            "\u039C" to "&Mu;", // greek capital letter mu, U+039C -->
            "\u039D" to "&Nu;", // greek capital letter nu, U+039D -->
            "\u039E" to "&Xi;", // greek capital letter xi, U+039E ISOgrk3 -->
            "\u039F" to "&Omicron;", // greek capital letter omicron, U+039F -->
            "\u03A0" to "&Pi;", // greek capital letter pi, U+03A0 ISOgrk3 -->
            "\u03A1" to "&Rho;", // greek capital letter rho, U+03A1 -->
            // <!-- there is no Sigmaf, and no U+03A2 character either -->
            "\u03A3" to "&Sigma;", // greek capital letter sigma,U+03A3 ISOgrk3 -->
            "\u03A4" to "&Tau;", // greek capital letter tau, U+03A4 -->
            "\u03A5" to "&Upsilon;", // greek capital letter upsilon,U+03A5 ISOgrk3 -->
            "\u03A6" to "&Phi;", // greek capital letter phi,U+03A6 ISOgrk3 -->
            "\u03A7" to "&Chi;", // greek capital letter chi, U+03A7 -->
            "\u03A8" to "&Psi;", // greek capital letter psi,U+03A8 ISOgrk3 -->
            "\u03A9" to "&Omega;", // greek capital letter omega,U+03A9 ISOgrk3 -->
            "\u03B1" to "&alpha;", // greek small letter alpha,U+03B1 ISOgrk3 -->
            "\u03B2" to "&beta;", // greek small letter beta, U+03B2 ISOgrk3 -->
            "\u03B3" to "&gamma;", // greek small letter gamma,U+03B3 ISOgrk3 -->
            "\u03B4" to "&delta;", // greek small letter delta,U+03B4 ISOgrk3 -->
            "\u03B5" to "&epsilon;", // greek small letter epsilon,U+03B5 ISOgrk3 -->
            "\u03B6" to "&zeta;", // greek small letter zeta, U+03B6 ISOgrk3 -->
            "\u03B7" to "&eta;", // greek small letter eta, U+03B7 ISOgrk3 -->
            "\u03B8" to "&theta;", // greek small letter theta,U+03B8 ISOgrk3 -->
            "\u03B9" to "&iota;", // greek small letter iota, U+03B9 ISOgrk3 -->
            "\u03BA" to "&kappa;", // greek small letter kappa,U+03BA ISOgrk3 -->
            "\u03BB" to "&lambda;", // greek small letter lambda,U+03BB ISOgrk3 -->
            "\u03BC" to "&mu;", // greek small letter mu, U+03BC ISOgrk3 -->
            "\u03BD" to "&nu;", // greek small letter nu, U+03BD ISOgrk3 -->
            "\u03BE" to "&xi;", // greek small letter xi, U+03BE ISOgrk3 -->
            "\u03BF" to "&omicron;", // greek small letter omicron, U+03BF NEW -->
            "\u03C0" to "&pi;", // greek small letter pi, U+03C0 ISOgrk3 -->
            "\u03C1" to "&rho;", // greek small letter rho, U+03C1 ISOgrk3 -->
            "\u03C2" to "&sigmaf;", // greek small letter final sigma,U+03C2 ISOgrk3 -->
            "\u03C3" to "&sigma;", // greek small letter sigma,U+03C3 ISOgrk3 -->
            "\u03C4" to "&tau;", // greek small letter tau, U+03C4 ISOgrk3 -->
            "\u03C5" to "&upsilon;", // greek small letter upsilon,U+03C5 ISOgrk3 -->
            "\u03C6" to "&phi;", // greek small letter phi, U+03C6 ISOgrk3 -->
            "\u03C7" to "&chi;", // greek small letter chi, U+03C7 ISOgrk3 -->
            "\u03C8" to "&psi;", // greek small letter psi, U+03C8 ISOgrk3 -->
            "\u03C9" to "&omega;", // greek small letter omega,U+03C9 ISOgrk3 -->
            "\u03D1" to "&thetasym;", // greek small letter theta symbol,U+03D1 NEW -->
            "\u03D2" to "&upsih;", // greek upsilon with hook symbol,U+03D2 NEW -->
            "\u03D6" to "&piv;", // greek pi symbol, U+03D6 ISOgrk3 -->
            // <!-- General Punctuation -->
            "\u2022" to "&bull;", // bullet = black small circle,U+2022 ISOpub -->
            // <!-- bullet is NOT the same as bullet operator, U+2219 -->
            "\u2026" to "&hellip;", // horizontal ellipsis = three dot leader,U+2026 ISOpub -->
            "\u2032" to "&prime;", // prime = minutes = feet, U+2032 ISOtech -->
            "\u2033" to "&Prime;", // double prime = seconds = inches,U+2033 ISOtech -->
            "\u203E" to "&oline;", // overline = spacing overscore,U+203E NEW -->
            "\u2044" to "&frasl;", // fraction slash, U+2044 NEW -->
            // <!-- Letterlike Symbols -->
            "\u2118" to "&weierp;", // script capital P = power set= Weierstrass p, U+2118 ISOamso -->
            "\u2111" to "&image;", // blackletter capital I = imaginary part,U+2111 ISOamso -->
            "\u211C" to "&real;", // blackletter capital R = real part symbol,U+211C ISOamso -->
            "\u2122" to "&trade;", // trade mark sign, U+2122 ISOnum -->
            "\u2135" to "&alefsym;", // alef symbol = first transfinite cardinal,U+2135 NEW -->
            // <!-- alef symbol is NOT the same as hebrew letter alef,U+05D0 although the
            // same glyph could be used to depict both characters -->
            // <!-- Arrows -->
            "\u2190" to "&larr;", // leftwards arrow, U+2190 ISOnum -->
            "\u2191" to "&uarr;", // upwards arrow, U+2191 ISOnum-->
            "\u2192" to "&rarr;", // rightwards arrow, U+2192 ISOnum -->
            "\u2193" to "&darr;", // downwards arrow, U+2193 ISOnum -->
            "\u2194" to "&harr;", // left right arrow, U+2194 ISOamsa -->
            "\u21B5" to "&crarr;", // downwards arrow with corner leftwards= carriage return, U+21B5 NEW -->
            "\u21D0" to "&lArr;", // leftwards double arrow, U+21D0 ISOtech -->
            // <!-- ISO 10646 does not say that lArr is the same as the 'is implied by'
            // arrow but also does not have any other character for that function.
            // So ? lArr canbe used for 'is implied by' as ISOtech suggests -->
            "\u21D1" to "&uArr;", // upwards double arrow, U+21D1 ISOamsa -->
            "\u21D2" to "&rArr;", // rightwards double arrow,U+21D2 ISOtech -->
            // <!-- ISO 10646 does not say this is the 'implies' character but does not
            // have another character with this function so ?rArr can be used for
            // 'implies' as ISOtech suggests -->
            "\u21D3" to "&dArr;", // downwards double arrow, U+21D3 ISOamsa -->
            "\u21D4" to "&hArr;", // left right double arrow,U+21D4 ISOamsa -->
            // <!-- Mathematical Operators -->
            "\u2200" to "&forall;", // for all, U+2200 ISOtech -->
            "\u2202" to "&part;", // partial differential, U+2202 ISOtech -->
            "\u2203" to "&exist;", // there exists, U+2203 ISOtech -->
            "\u2205" to "&empty;", // empty set = null set = diameter,U+2205 ISOamso -->
            "\u2207" to "&nabla;", // nabla = backward difference,U+2207 ISOtech -->
            "\u2208" to "&isin;", // element of, U+2208 ISOtech -->
            "\u2209" to "&notin;", // not an element of, U+2209 ISOtech -->
            "\u220B" to "&ni;", // contains as member, U+220B ISOtech -->
            // <!-- should there be a more memorable name than 'ni'? -->
            "\u220F" to "&prod;", // n-ary product = product sign,U+220F ISOamsb -->
            // <!-- prod is NOT the same character as U+03A0 'greek capital letter pi'
            // though the same glyph might be used for both -->
            "\u2211" to "&sum;", // n-ary summation, U+2211 ISOamsb -->
            // <!-- sum is NOT the same character as U+03A3 'greek capital letter sigma'
            // though the same glyph might be used for both -->
            "\u2212" to "&minus;", // minus sign, U+2212 ISOtech -->
            "\u2217" to "&lowast;", // asterisk operator, U+2217 ISOtech -->
            "\u221A" to "&radic;", // square root = radical sign,U+221A ISOtech -->
            "\u221D" to "&prop;", // proportional to, U+221D ISOtech -->
            "\u221E" to "&infin;", // infinity, U+221E ISOtech -->
            "\u2220" to "&ang;", // angle, U+2220 ISOamso -->
            "\u2227" to "&and;", // logical and = wedge, U+2227 ISOtech -->
            "\u2228" to "&or;", // logical or = vee, U+2228 ISOtech -->
            "\u2229" to "&cap;", // intersection = cap, U+2229 ISOtech -->
            "\u222A" to "&cup;", // union = cup, U+222A ISOtech -->
            "\u222B" to "&int;", // integral, U+222B ISOtech -->
            "\u2234" to "&there4;", // therefore, U+2234 ISOtech -->
            "\u223C" to "&sim;", // tilde operator = varies with = similar to,U+223C ISOtech -->
            // <!-- tilde operator is NOT the same character as the tilde, U+007E,although
            // the same glyph might be used to represent both -->
            "\u2245" to "&cong;", // approximately equal to, U+2245 ISOtech -->
            "\u2248" to "&asymp;", // almost equal to = asymptotic to,U+2248 ISOamsr -->
            "\u2260" to "&ne;", // not equal to, U+2260 ISOtech -->
            "\u2261" to "&equiv;", // identical to, U+2261 ISOtech -->
            "\u2264" to "&le;", // less-than or equal to, U+2264 ISOtech -->
            "\u2265" to "&ge;", // greater-than or equal to,U+2265 ISOtech -->
            "\u2282" to "&sub;", // subset of, U+2282 ISOtech -->
            "\u2283" to "&sup;", // superset of, U+2283 ISOtech -->
            // <!-- note that nsup, 'not a superset of, U+2283' is not covered by the
            // Symbol font encoding and is not included. Should it be, for symmetry?
            // It is in ISOamsn -->,
            "\u2284" to "&nsub;", // not a subset of, U+2284 ISOamsn -->
            "\u2286" to "&sube;", // subset of or equal to, U+2286 ISOtech -->
            "\u2287" to "&supe;", // superset of or equal to,U+2287 ISOtech -->
            "\u2295" to "&oplus;", // circled plus = direct sum,U+2295 ISOamsb -->
            "\u2297" to "&otimes;", // circled times = vector product,U+2297 ISOamsb -->
            "\u22A5" to "&perp;", // up tack = orthogonal to = perpendicular,U+22A5 ISOtech -->
            "\u22C5" to "&sdot;", // dot operator, U+22C5 ISOamsb -->
            // <!-- dot operator is NOT the same character as U+00B7 middle dot -->
            // <!-- Miscellaneous Technical -->
            "\u2308" to "&lceil;", // left ceiling = apl upstile,U+2308 ISOamsc -->
            "\u2309" to "&rceil;", // right ceiling, U+2309 ISOamsc -->
            "\u230A" to "&lfloor;", // left floor = apl downstile,U+230A ISOamsc -->
            "\u230B" to "&rfloor;", // right floor, U+230B ISOamsc -->
            "\u2329" to "&lang;", // left-pointing angle bracket = bra,U+2329 ISOtech -->
            // <!-- lang is NOT the same character as U+003C 'less than' or U+2039 'single left-pointing angle quotation
            // mark' -->
            "\u232A" to "&rang;", // right-pointing angle bracket = ket,U+232A ISOtech -->
            // <!-- rang is NOT the same character as U+003E 'greater than' or U+203A
            // 'single right-pointing angle quotation mark' -->
            // <!-- Geometric Shapes -->
            "\u25CA" to "&loz;", // lozenge, U+25CA ISOpub -->
            // <!-- Miscellaneous Symbols -->
            "\u2660" to "&spades;", // black spade suit, U+2660 ISOpub -->
            // <!-- black here seems to mean filled as opposed to hollow -->
            "\u2663" to "&clubs;", // black club suit = shamrock,U+2663 ISOpub -->
            "\u2665" to "&hearts;", // black heart suit = valentine,U+2665 ISOpub -->
            "\u2666" to "&diams;", // black diamond suit, U+2666 ISOpub -->

            // <!-- Latin Extended-A -->
            "\u0152" to "&OElig;", // -- latin capital ligature OE,U+0152 ISOlat2 -->
            "\u0153" to "&oelig;", // -- latin small ligature oe, U+0153 ISOlat2 -->
            // <!-- ligature is a misnomer, this is a separate character in some languages -->
            "\u0160" to "&Scaron;", // -- latin capital letter S with caron,U+0160 ISOlat2 -->
            "\u0161" to "&scaron;", // -- latin small letter s with caron,U+0161 ISOlat2 -->
            "\u0178" to "&Yuml;", // -- latin capital letter Y with diaeresis,U+0178 ISOlat2 -->
            // <!-- Spacing Modifier Letters -->
            "\u02C6" to "&circ;", // -- modifier letter circumflex accent,U+02C6 ISOpub -->
            "\u02DC" to "&tilde;", // small tilde, U+02DC ISOdia -->
            // <!-- General Punctuation -->
            "\u2002" to "&ensp;", // en space, U+2002 ISOpub -->
            "\u2003" to "&emsp;", // em space, U+2003 ISOpub -->
            "\u2009" to "&thinsp;", // thin space, U+2009 ISOpub -->
            "\u200C" to "&zwnj;", // zero width non-joiner,U+200C NEW RFC 2070 -->
            "\u200D" to "&zwj;", // zero width joiner, U+200D NEW RFC 2070 -->
            "\u200E" to "&lrm;", // left-to-right mark, U+200E NEW RFC 2070 -->
            "\u200F" to "&rlm;", // right-to-left mark, U+200F NEW RFC 2070 -->
            "\u2013" to "&ndash;", // en dash, U+2013 ISOpub -->
            "\u2014" to "&mdash;", // em dash, U+2014 ISOpub -->
            "\u2018" to "&lsquo;", // left single quotation mark,U+2018 ISOnum -->
            "\u2019" to "&rsquo;", // right single quotation mark,U+2019 ISOnum -->
            "\u201A" to "&sbquo;", // single low-9 quotation mark, U+201A NEW -->
            "\u201C" to "&ldquo;", // left double quotation mark,U+201C ISOnum -->
            "\u201D" to "&rdquo;", // right double quotation mark,U+201D ISOnum -->
            "\u201E" to "&bdquo;", // double low-9 quotation mark, U+201E NEW -->
            "\u2020" to "&dagger;", // dagger, U+2020 ISOpub -->
            "\u2021" to "&Dagger;", // double dagger, U+2021 ISOpub -->
            "\u2030" to "&permil;", // per mille sign, U+2030 ISOtech -->
            "\u2039" to "&lsaquo;", // single left-pointing angle quotation mark,U+2039 ISO proposed -->
            // <!-- lsaquo is proposed but not yet ISO standardized -->
            "\u203A" to "&rsaquo;", // single right-pointing angle quotation mark,U+203A ISO proposed -->
            // <!-- rsaquo is proposed but not yet ISO standardized -->
            "\u20AC" to "&euro;" // -- euro sign, U+20AC NEW -->
    )

    /**
     * Reverse of [.HTML40_EXTENDED_ESCAPE] for unescaping purposes.
     */
    val HTML40_EXTENDED_UNESCAPE: Map<CharSequence, CharSequence> = HTML40_EXTENDED_ESCAPE.invert()

    /**
     * A Map&lt;CharSequence, CharSequence&gt; to escape the basic XML and HTML
     * character entities.
     *
     * Namely: `" & < >`
     */
    val BASIC_ESCAPE: Map<CharSequence, CharSequence> = mapOf(
            "\"" to "&quot;", // " - double-quote
            "&" to "&amp;",  // & - ampersand
            "<" to "&lt;", // < - less-than
            ">" to "&gt;"    // > - greater-than
    )

    /**
     * Reverse of [.BASIC_ESCAPE] for unescaping purposes.
     */
    val BASIC_UNESCAPE: Map<CharSequence, CharSequence> = BASIC_ESCAPE.invert()

    /**
     * A Map&lt;CharSequence, CharSequence&gt; to escape the apostrophe character to
     * its XML character entity.
     */
    val APOS_ESCAPE: Map<CharSequence, CharSequence> = mapOf(
            "'" to "&apos;" // XML apostrophe
    )

    /**
     * Reverse of [.APOS_ESCAPE] for unescaping purposes.
     */
    val APOS_UNESCAPE: Map<CharSequence, CharSequence> = APOS_ESCAPE.invert()

    /**
     * Used to invert an escape Map into an unescape Map.
     * @param this@invert Map&lt;String, String&gt; to be inverted
     * @return Map&lt;String, String&gt; inverted array
     */
    private fun <K, V> Map<K, V>.invert(): Map<V, K> {
        val newMap = HashMap<V, K>()
        val it = entries.iterator()
        while (it.hasNext()) {
            val pair = it.next()
            newMap[pair.value] = pair.key
        }
        return newMap
    }

}
