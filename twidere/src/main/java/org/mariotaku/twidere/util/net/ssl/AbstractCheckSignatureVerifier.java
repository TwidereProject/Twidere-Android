package org.mariotaku.twidere.util.net.ssl;

import android.util.Log;

import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.conn.util.InetAddressUtilsHC4;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

public abstract class AbstractCheckSignatureVerifier implements X509HostnameVerifier {

	/**
	 * This contains a list of 2nd-level domains that aren't allowed to have
	 * wildcards when combined with country-codes. For example: [*.co.uk].
	 * <p/>
	 * The [*.co.uk] problem is an interesting one. Should we just hope that
	 * CA's would never foolishly allow such a certificate to happen? Looks like
	 * we're the only implementation guarding against this. Firefox, Curl, Sun
	 * Java 1.4, 5, 6 don't bother with this check.
	 */
	private final static String[] BAD_COUNTRY_2LDS = { "ac", "co", "com", "ed", "edu", "go", "gouv", "gov", "info",
			"lg", "ne", "net", "or", "org" };

	static {
		// Just in case developer forgot to manually sort the array. :-)
		Arrays.sort(BAD_COUNTRY_2LDS);
	}

	private final static String TAG = "HttpClient";

	@Override
	public final boolean verify(final String host, final SSLSession session) {
		try {
			final Certificate[] certs = session.getPeerCertificates();
			final X509Certificate x509 = (X509Certificate) certs[0];
			verify(host, x509);
			return true;
		} catch (final SSLException e) {
			return false;
		}
	}

	@Override
	public final void verify(final String host, final SSLSocket ssl) throws IOException {
		if (host == null) throw new NullPointerException("host to verify is null");

		SSLSession session = ssl.getSession();
		if (session == null) {
			// In our experience this only happens under IBM 1.4.x when
			// spurious (unrelated) certificates show up in the server'
			// chain. Hopefully this will unearth the real problem:
			final InputStream in = ssl.getInputStream();
			in.available();
			/*
			 * If you're looking at the 2 lines of code above because you're
			 * running into a problem, you probably have two options:
			 * 
			 * #1. Clean up the certificate chain that your server is presenting
			 * (e.g. edit "/etc/apache2/server.crt" or wherever it is your
			 * server's certificate chain is defined).
			 * 
			 * OR
			 * 
			 * #2. Upgrade to an IBM 1.5.x or greater JVM, or switch to a
			 * non-IBM JVM.
			 */

			// If ssl.getInputStream().available() didn't cause an
			// exception, maybe at least now the session is available?
			session = ssl.getSession();
			if (session == null) {
				// If it's still null, probably a startHandshake() will
				// unearth the real problem.
				ssl.startHandshake();

				// Okay, if we still haven't managed to cause an exception,
				// might as well go for the NPE. Or maybe we're okay now?
				session = ssl.getSession();
			}
		}

		final Certificate[] certs = session.getPeerCertificates();
		final X509Certificate x509 = (X509Certificate) certs[0];
		verify(host, x509);
	}

	@Override
	public final void verify(final String host, final String[] cns, final String[] subjectAlts) throws SSLException {
		verify(host, cns, subjectAlts, null);
	}

	public abstract void verify(final String host, final String[] cns, final String[] subjectAlts,
			final X509Certificate cert) throws SSLException;

	@Override
	public final void verify(final String host, final X509Certificate cert) throws SSLException {
		final String[] cns = getCNs(cert);
		final String[] subjectAlts = getSubjectAlts(cert, host);
		verify(host, cns, subjectAlts, cert);
	}

	/**
	 * @deprecated (4.3.1) should not be a part of public APIs.
	 */
	@Deprecated
	public static boolean acceptableCountryWildcard(final String cn) {
		final String parts[] = cn.split("\\.");// it's
		// not an attempt to wildcard a 2TLD within a country code
		if (parts.length != 3 || parts[2].length() != 2) return true;
		return Arrays.binarySearch(BAD_COUNTRY_2LDS, parts[1]) < 0;
	}

	/**
	 * Counts the number of dots "." in a string.
	 * 
	 * @param s string to count dots from
	 * @return number of dots
	 */
	public static int countDots(final String s) {
		int count = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '.') {
				count++;
			}
		}
		return count;
	}

	public static String[] getCNs(final X509Certificate cert) {
		final LinkedList<String> cnList = new LinkedList<String>();
		/*
		 * Sebastian Hauer's original StrictSSLProtocolSocketFactory used
		 * getName() and had the following comment:
		 * 
		 * Parses a X.500 distinguished name for the value of the "Common Name"
		 * field. This is done a bit sloppy right now and should probably be
		 * done a bit more according to <code>RFC 2253</code>.
		 * 
		 * I've noticed that toString() seems to do a better job than getName()
		 * on these X500Principal objects, so I'm hoping that addresses
		 * Sebastian's concern.
		 * 
		 * For example, getName() gives me this:
		 * 1.2.840.113549.1.9.1=#16166a756c6975736461766965734063756362632e636f6d
		 * 
		 * whereas toString() gives me this: EMAILADDRESS=juliusdavies@cucbc.com
		 * 
		 * Looks like toString() even works with non-ascii domain names! I
		 * tested it with "&#x82b1;&#x5b50;.co.jp" and it worked fine.
		 */

		final String subjectPrincipal = cert.getSubjectX500Principal().toString();
		final StringTokenizer st = new StringTokenizer(subjectPrincipal, ",+");
		while (st.hasMoreTokens()) {
			final String tok = st.nextToken().trim();
			if (tok.length() > 3) {
				if (tok.substring(0, 3).equalsIgnoreCase("CN=")) {
					cnList.add(tok.substring(3));
				}
			}
		}
		if (!cnList.isEmpty()) {
			final String[] cns = new String[cnList.size()];
			cnList.toArray(cns);
			return cns;
		} else
			return null;
	}

	/**
	 * Extracts the array of SubjectAlt DNS names from an X509Certificate.
	 * Returns null if there aren't any.
	 * <p/>
	 * Note: Java doesn't appear able to extract international characters from
	 * the SubjectAlts. It can only extract international characters from the CN
	 * field.
	 * <p/>
	 * (Or maybe the version of OpenSSL I'm using to test isn't storing the
	 * international characters correctly in the SubjectAlts?).
	 * 
	 * @param cert X509Certificate
	 * @return Array of SubjectALT DNS names stored in the certificate.
	 */
	public static String[] getDNSSubjectAlts(final X509Certificate cert) {
		return getSubjectAlts(cert, null);
	}

	public static final boolean verify(final String host, final String[] cns, final String[] subjectAlts,
			final boolean strictWithSubDomains) {

		// Build the list of names we're going to check. Our DEFAULT and
		// STRICT implementations of the HostnameVerifier only use the
		// first CN provided. All other CNs are ignored.
		// (Firefox, wget, curl, Sun Java 1.4, 5, 6 all work this way).
		final LinkedList<String> names = new LinkedList<String>();
		if (cns != null && cns.length > 0 && cns[0] != null) {
			names.add(cns[0]);
		}
		if (subjectAlts != null) {
			for (final String subjectAlt : subjectAlts) {
				if (subjectAlt != null) {
					names.add(subjectAlt);
				}
			}
		}

		if (names.isEmpty()) return false;

		// StringBuilder for building the error message.
		final StringBuilder buf = new StringBuilder();

		// We're can be case-insensitive when comparing the host we used to
		// establish the socket to the hostname in the certificate.
		final String hostName = normaliseIPv6Address(host.trim().toLowerCase(Locale.US));
		boolean match = false;
		for (final Iterator<String> it = names.iterator(); it.hasNext();) {
			// Don't trim the CN, though!
			String cn = it.next();
			cn = cn.toLowerCase(Locale.US);
			// Store CN in StringBuilder in case we need to report an error.
			buf.append(" <");
			buf.append(cn);
			buf.append('>');
			if (it.hasNext()) {
				buf.append(" OR");
			}

			// The CN better have at least two dots if it wants wildcard
			// action. It also can't be [*.co.uk] or [*.co.jp] or
			// [*.org.uk], etc...
			final String parts[] = cn.split("\\.");
			final boolean doWildcard = parts.length >= 3 && parts[0].endsWith("*") && validCountryWildcard(cn)
					&& !isIPAddress(host);

			if (doWildcard) {
				final String firstpart = parts[0];
				if (firstpart.length() > 1) { // e.g. server*
					// e.g. server
					final String prefix = firstpart.substring(0, firstpart.length() - 1);
					// skip wildcard part from cn
					final String suffix = cn.substring(firstpart.length());// skip
					// wildcard part from host
					final String hostSuffix = hostName.substring(prefix.length());
					match = hostName.startsWith(prefix) && hostSuffix.endsWith(suffix);
				} else {
					match = hostName.endsWith(cn.substring(1));
				}
				if (match && strictWithSubDomains) {
					// If we're in strict mode, then [*.foo.com] is not
					// allowed to match [a.b.foo.com]
					match = countDots(hostName) == countDots(cn);
				}
			} else {
				match = hostName.equals(normaliseIPv6Address(cn));
			}
			if (match) {
				break;
			}
		}
		return match;
	}

	/**
	 * Extracts the array of SubjectAlt DNS or IP names from an X509Certificate.
	 * Returns null if there aren't any.
	 * 
	 * @param cert X509Certificate
	 * @param hostname
	 * @return Array of SubjectALT DNS or IP names stored in the certificate.
	 */
	private static String[] getSubjectAlts(final X509Certificate cert, final String hostname) {
		final int subjectType;
		if (isIPAddress(hostname)) {
			subjectType = 7;
		} else {
			subjectType = 2;
		}

		final LinkedList<String> subjectAltList = new LinkedList<String>();
		Collection<List<?>> c = null;
		try {
			c = cert.getSubjectAlternativeNames();
		} catch (final CertificateParsingException cpe) {
		}
		if (c != null) {
			for (final List<?> aC : c) {
				final List<?> list = aC;
				final int type = ((Integer) list.get(0)).intValue();
				if (type == subjectType) {
					final String s = (String) list.get(1);
					subjectAltList.add(s);
				}
			}
		}
		if (!subjectAltList.isEmpty()) {
			final String[] subjectAlts = new String[subjectAltList.size()];
			subjectAltList.toArray(subjectAlts);
			return subjectAlts;
		} else
			return null;
	}

	private static boolean isIPAddress(final String hostname) {
		return hostname != null
				&& (InetAddressUtilsHC4.isIPv4Address(hostname) || InetAddressUtilsHC4.isIPv6Address(hostname));
	}

	/*
	 * Check if hostname is IPv6, and if so, convert to standard format.
	 */
	private static String normaliseIPv6Address(final String hostname) {
		if (hostname == null || !InetAddressUtilsHC4.isIPv6Address(hostname)) return hostname;
		try {
			final InetAddress inetAddress = InetAddress.getByName(hostname);
			return inetAddress.getHostAddress();
		} catch (final UnknownHostException uhe) { // Should not happen, because
													// we check for IPv6 address
													// above
			Log.e(TAG, "Unexpected error converting " + hostname, uhe);
			return hostname;
		}
	}

	static boolean validCountryWildcard(final String cn) {
		final String parts[] = cn.split("\\.");
		// it's not an attempt to wildcard a 2TLD within a country code
		if (parts.length != 3 || parts[2].length() != 2) return true;
		return Arrays.binarySearch(BAD_COUNTRY_2LDS, parts[1]) < 0;
	}
}