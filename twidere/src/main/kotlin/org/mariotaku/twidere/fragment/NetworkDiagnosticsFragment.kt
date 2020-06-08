package org.mariotaku.twidere.fragment

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock
import androidx.core.content.ContextCompat
import android.text.Selection
import android.text.SpannableString
import android.text.Spanned
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_network_diagnostics.*
import okhttp3.Dns
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.restfu.RestFuUtils
import org.mariotaku.restfu.annotation.method.GET
import org.mariotaku.restfu.http.HttpRequest
import org.mariotaku.restfu.http.HttpResponse
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.Constants.DEFAULT_TWITTER_API_URL_FORMAT
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.SharedPreferenceConstants.*
import org.mariotaku.twidere.extension.model.getEndpoint
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.extension.restfu.headers
import org.mariotaku.twidere.extension.restfu.set
import org.mariotaku.twidere.model.account.cred.OAuthCredentials
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.MicroBlogAPIFactory
import org.mariotaku.twidere.util.dagger.DependencyHolder
import org.mariotaku.twidere.util.net.SystemDnsFetcher
import org.mariotaku.twidere.util.net.TwidereDns
import java.io.IOException
import java.io.OutputStream
import java.lang.ref.WeakReference
import java.net.InetAddress
import java.util.*

/**
 * Network diagnostics
 * Created by mariotaku on 16/2/9.
 */
class NetworkDiagnosticsFragment : BaseFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        startDiagnostics.setOnClickListener {
            logText.text = null
            DiagnosticsTask(this@NetworkDiagnosticsFragment).execute()
        }
        logText.movementMethod = ScrollingMovementMethod.getInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_network_diagnostics, container, false)
    }

    private fun appendMessage(message: LogText) {
        val activity = activity ?: return
        val coloredText = SpannableString.valueOf(message.message)
        when (message.state) {
            LogText.State.OK -> {
                coloredText.setSpan(ForegroundColorSpan(ContextCompat.getColor(activity,
                        R.color.material_light_green)), 0, coloredText.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            LogText.State.ERROR -> {
                coloredText.setSpan(ForegroundColorSpan(ContextCompat.getColor(activity,
                        R.color.material_red)), 0, coloredText.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            LogText.State.WARNING -> {
                coloredText.setSpan(ForegroundColorSpan(ContextCompat.getColor(activity,
                        R.color.material_amber)), 0, coloredText.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            LogText.State.DEFAULT -> {
            }
        }
        logText.append(coloredText)
        Selection.setSelection(logText.editableText, logText.length())
    }

    internal class DiagnosticsTask(fragment: NetworkDiagnosticsFragment) : AsyncTask<Any, LogText, Unit>() {

        private val fragmentRef = WeakReference(fragment)
        private val contextRef = WeakReference(fragment.activity?.applicationContext)

        override fun doInBackground(vararg params: Any) {
            val context = contextRef.get() ?: return
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            logPrintln("**** NOTICE ****", LogText.State.WARNING)
            logPrintln()
            logPrintln("Text below may have personal information, BE CAREFUL TO MAKE IT PUBLIC",
                    LogText.State.WARNING)
            logPrintln()
            val holder = DependencyHolder.get(context)
            val dns = holder.dns
            val prefs = holder.preferences
            logPrintln(("Network preferences"))
            logPrintln(("using_resolver: ${prefs.getBoolean(KEY_BUILTIN_DNS_RESOLVER, false)}"))
            logPrintln(("tcp_dns_query: ${prefs.getBoolean(KEY_TCP_DNS_QUERY, false)}"))
            logPrintln(("dns_server: ${prefs.getString(KEY_DNS_SERVER, null)}"))
            logPrintln()
            logPrintln(("System DNS servers"))


            val servers = SystemDnsFetcher.get(context)
            logPrintln(servers?.toString() ?: "null")
            logPrintln()

            for (accountKey in DataStoreUtils.getAccountKeys(context)) {
                val details = AccountUtils.getAccountDetails(AccountManager.get(context),
                        accountKey, true) ?: continue
                logPrintln(("Testing connection for account $accountKey"))
                logPrintln()
                logPrintln(("api_url_format: ${details.credentials.api_url_format}"))
                (details.credentials as? OAuthCredentials)?.let { creds ->
                    logPrintln(("same_oauth_signing_url: ${creds.same_oauth_signing_url}"))
                }
                logPrintln(("auth_type: " + details.credentials_type))

                logPrintln()

                logPrintln(("Testing DNS functionality"))
                logPrintln()
                val endpoint = details.credentials.getEndpoint(MicroBlog::class.java)
                val uri = Uri.parse(endpoint.url)
                val host = uri.host
                if (host != null) {
                    testDns(dns, host)
                    testNativeLookup(host)
                } else {
                    logPrintln("API URL format is invalid", LogText.State.ERROR)
                    logPrintln()
                }

                logPrintln()

                logPrintln(("Testing Network connectivity"))
                logPrintln()

                val baseUrl: String
                baseUrl = if (details.credentials.api_url_format != null) {
                    MicroBlogAPIFactory.getApiBaseUrl(details.credentials.api_url_format, "api")
                } else {
                    MicroBlogAPIFactory.getApiBaseUrl(DEFAULT_TWITTER_API_URL_FORMAT, "api")
                }
                val client = DependencyHolder.get(context).restHttpClient
                var response: HttpResponse? = null
                try {
                    logPrint("Connecting to $baseUrl...")
                    val builder = HttpRequest.Builder()
                    builder.method(GET.METHOD)
                    builder.url(baseUrl)
                    builder.headers {
                        this["Accept"] = "*/*"
                    }
                    val start = SystemClock.uptimeMillis()
                    response = client.newCall(builder.build()).execute()
                    logPrint(" OK (${SystemClock.uptimeMillis() - start} ms)")
                } catch (e: IOException) {
                    logPrint(" ERROR: ${e.message}", LogText.State.ERROR)
                }

                logPrintln()
                try {
                    if (response != null) {
                        logPrintln("Reading response...")
                        val start = SystemClock.uptimeMillis()
                        val os = CountOutputStream()
                        response.body.writeTo(os)
                        logPrintln(" ${os.total} bytes (${SystemClock.uptimeMillis() - start} ms)", LogText.State.OK)
                    }
                } catch (e: IOException) {
                    logPrintln("ERROR: ${e.message}", LogText.State.ERROR)
                } finally {
                    RestFuUtils.closeSilently(response)
                }
                logPrintln()

                logPrintln(("Testing API functionality"))
                logPrintln()
                when (details.type) {
                    AccountType.MASTODON -> {
                        val mastodon = details.newMicroBlogInstance(context, Mastodon::class.java)
                        testAPICall("verify_credentials", mastodon) {
                            verifyCredentials()
                        }
                        testAPICall("get_home_timeline", mastodon) {
                            getHomeTimeline(Paging().count(1))
                        }
                    }
                    else -> {
                        val microBlog = details.newMicroBlogInstance(context, MicroBlog::class.java)
                        testAPICall("verify_credentials", microBlog) {
                            verifyCredentials()
                        }
                        testAPICall("get_home_timeline", microBlog) {
                            getHomeTimeline(Paging().count(1))
                        }
                    }
                }
                logPrintln()
            }

            logPrintln()

            logPrintln(("Testing common host names"))
            logPrintln()

            testDns(dns, "www.google.com")
            testNativeLookup("www.google.com")
            logPrintln()
            testDns(dns, "github.com")
            testNativeLookup("github.com")
            logPrintln()
            testDns(dns, "twitter.com")
            testNativeLookup("twitter.com")

            logPrintln()

            logPrintln("Build information: ")
            logPrintln("version_code: ${BuildConfig.VERSION_CODE}")
            logPrintln("version_name: ${BuildConfig.VERSION_NAME}")
            logPrintln("flavor: ${BuildConfig.FLAVOR}")
            logPrintln("debug: ${BuildConfig.DEBUG}")
            logPrintln()
            logPrintln(("Basic system information: "))
            logPrintln(context.resources.configuration.toString())
            logPrintln()
            logPrintln(("Active network info: "))
            logPrintln((cm.activeNetworkInfo.toString()))
        }

        override fun onProgressUpdate(vararg values: LogText) {
            val fragment = fragmentRef.get() ?: return
            for (value in values) {
                fragment.appendMessage(value)
            }
        }

        override fun onPreExecute() {
            val fragment = fragmentRef.get() ?: return
            fragment.diagStart()
            super.onPreExecute()
        }

        override fun onPostExecute(u: Unit) {
            val fragment = fragmentRef.get() ?: return
            logPrintln()
            logPrintln(("Done. You can send this log to me, and I'll contact you to solve related issue."))
            fragment.logReady()
        }

        private fun testDns(dns: Dns, host: String) {
            testCall("builtin lookup $host") {
                if (dns is TwidereDns) {
                    logPrint((dns.lookupResolver(host).toString()))
                } else {
                    logPrint((dns.lookup(host).toString()))
                }
            }
        }

        private fun testNativeLookup(host: String) {
            testCall("native lookup $host") {
                logPrint(Arrays.toString(InetAddress.getAllByName(host)))
            }
        }

        private fun <T> testAPICall(name: String, api: T, test: T.() -> Unit) {
            testCall(name) { test(api) }
        }

        private inline fun testCall(name: String, test: () -> Unit) {
            logPrint("Testing $name...")
            try {
                val start = SystemClock.uptimeMillis()
                test()
                logPrint(" OK (${SystemClock.uptimeMillis() - start} ms)", LogText.State.OK)
            } catch (e: Exception) {
                logPrint(" ERROR: ${e.message}", LogText.State.ERROR)
            }

            logPrintln()
        }

        private fun logPrint(text: CharSequence, state: LogText.State = LogText.State.DEFAULT) {
            publishProgress(LogText(text, state))
        }

        private fun logPrintln(text: CharSequence = "", state: LogText.State = LogText.State.DEFAULT) {
            logPrint("$text\n", state)
        }

    }

    private fun diagStart() {
        startDiagnostics.setText(R.string.message_please_wait)
        startDiagnostics.isEnabled = false
    }

    private fun logReady() {
        startDiagnostics.setText(R.string.action_send)
        startDiagnostics.isEnabled = true
        startDiagnostics.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, "Twidere Network Diagnostics")
            intent.putExtra(Intent.EXTRA_TEXT, logText.text)
            startActivity(Intent.createChooser(intent, getString(R.string.action_send)))
        }
    }

    internal data class LogText(val message: CharSequence, var state: State = State.DEFAULT) {

        internal enum class State {
            DEFAULT, OK, ERROR, WARNING
        }

    }

    private class CountOutputStream : OutputStream() {
        var total: Long = 0
            private set

        @Throws(IOException::class)
        override fun write(oneByte: Int) {
            total++
        }
    }
}
