/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util.stetho

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.util.Base64
import android.util.Base64InputStream
import android.util.Base64OutputStream
import com.bluelinelabs.logansquare.LoganSquare
import com.facebook.stetho.dumpapp.DumpException
import com.facebook.stetho.dumpapp.DumperContext
import com.facebook.stetho.dumpapp.DumperPlugin
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.TypeRef
import com.jayway.jsonpath.spi.json.JsonOrgJsonProvider
import com.jayway.jsonpath.spi.mapper.MappingProvider
import org.apache.commons.cli.GnuParser
import org.apache.commons.cli.Options
import org.json.JSONArray
import org.json.JSONObject
import org.mariotaku.ktextension.HexColorFormat
import org.mariotaku.ktextension.subArray
import org.mariotaku.ktextension.toHexColor
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.Utils
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Created by mariotaku on 2017/3/6.
 */

class AccountsDumper(val context: Context) : DumperPlugin {

    private val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    private val salt = byteArrayOf(0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8)

    override fun getName() = "accounts"

    override fun dump(dumpContext: DumperContext) {
        val parser = GnuParser()
        val argsAsList = dumpContext.argsAsList
        when (argsAsList.firstOrNull()) {
            "import" -> {
                val subCommandArgs = argsAsList.subArray(1..argsAsList.lastIndex)
                val options = Options().apply {
                    addRequiredOption("p", "password", true, "Account encryption password")
                    addRequiredOption("i", "input", true, "Accounts data file")
                }
                val commandLine = parser.parse(options, subCommandArgs)
                try {
                    val password = commandLine.getOptionValue("password")
                    File(commandLine.getOptionValue("input")).inputStream().use { input ->
                        importAccounts(password, input, dumpContext.stdout)
                    }
                } catch (e: Exception) {
                    e.printStackTrace(dumpContext.stderr)
                }
            }
            "export" -> {
                val subCommandArgs = argsAsList.subArray(1..argsAsList.lastIndex)
                val options = Options().apply {
                    addRequiredOption("p", "password", true, "Account encryption password")
                    addRequiredOption("o", "output", true, "Accounts data file")
                }
                val commandLine = parser.parse(options, subCommandArgs)
                try {
                    val password = commandLine.getOptionValue("password")
                    File(commandLine.getOptionValue("output")).outputStream().use { output ->
                        exportAccounts(password, output)
                    }
                } catch (e: Exception) {
                    e.printStackTrace(dumpContext.stderr)
                }
            }
            "list" -> {
                val keys = DataStoreUtils.getAccountKeys(context)
                keys.forEach {
                    dumpContext.stdout.println(it)
                }
            }
            "get" -> {
                val subCommandArgs = argsAsList.subArray(1..argsAsList.lastIndex)
                if (subCommandArgs.isEmpty()) {
                    throw DumpException("Account key required")
                }
                val docContext = try {
                    accountDocContext(subCommandArgs[0])
                } catch (e: Utils.NoAccountException) {
                    throw DumpException("Account not found")
                }
                if (subCommandArgs.size == 1) {
                    val result = docContext.read("$", Object::class.java)
                    dumpContext.stdout.println(result?.prettyPrint())
                } else for (i in 1..subCommandArgs.lastIndex) {
                    val result = docContext.read(subCommandArgs[i], Object::class.java)
                    dumpContext.stdout.println(result?.prettyPrint())
                }
            }
            "set" -> {
                val subCommandArgs = argsAsList.subArray(1..argsAsList.lastIndex)
                if (subCommandArgs.size != 3) {
                    throw DumpException("Usage: accounts set <account_key> <field> <value>")
                }
                val docContext = try {
                    accountDocContext(subCommandArgs[0])
                } catch (e: Utils.NoAccountException) {
                    throw DumpException("Account not found")
                }
                val value = subCommandArgs[2]
                val path = subCommandArgs[1]
                docContext.set(path, value)
                val details = docContext.read("$", Object::class.java)?.let {
                    LoganSquare.parse(it.toString(), AccountDetails::class.java)
                } ?: return
                val am = AccountManager.get(context)
                details.account.updateDetails(am, details)
                dumpContext.stdout.println("$path = ${docContext.read(path, Object::class.java)?.prettyPrint()}")
            }
            else -> {
                dumpContext.stderr.println("Usage: accounts [import|export] -p <password>")
            }
        }

    }

    private fun accountDocContext(forKey: String): DocumentContext {
        val accountKey = UserKey.valueOf(forKey)
        val am = AccountManager.get(context)
        val details = AccountUtils.getAccountDetails(am, accountKey, true) ?: throw Utils.NoAccountException()
        val configuration = Configuration.builder()
                .jsonProvider(JsonOrgJsonProvider())
                .mappingProvider(AsIsMappingProvider())
                .build()
        return JsonPath.parse(LoganSquare.serialize(details), configuration)
    }

    private fun Any.prettyPrint() = if (this is JSONObject) {
        toString(4)
    } else if (this is JSONArray) {
        toString(4)
    } else {
        toString()
    }

    private fun exportAccounts(password: String, output: OutputStream) {
        val secret = generateSecret(password)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secret)

        val base64 = Base64OutputStream(output, Base64.NO_CLOSE)

        val iv = cipher.parameters.getParameterSpec(IvParameterSpec::class.java).iv
        // write IV size
        base64.write(iv.size.toByteArray())
        // write IV
        base64.write(iv)

        val gz = GZIPOutputStream(CipherOutputStream(base64, cipher))
        // write accounts
        val am = AccountManager.get(context)
        val accounts = AccountUtils.getAllAccountDetails(am, true).toList()
        LoganSquare.serialize(accounts, gz, AccountDetails::class.java)
    }

    private fun importAccounts(password: String, input: InputStream, output: PrintStream) {
        val base64 = Base64InputStream(input, Base64.NO_CLOSE)

        val ivSize = ByteArray(4).apply { base64.read(this) }.toInt()
        val iv = ByteArray(ivSize).apply { base64.read(this) }

        val secret = generateSecret(password)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secret, IvParameterSpec(iv))
        val gz = GZIPInputStream(CipherInputStream(base64, cipher))
        val am = AccountManager.get(context)
        val usedAccounts = AccountUtils.getAccounts(am)
        val allDetails = LoganSquare.parseList(gz, AccountDetails::class.java)
        allDetails.forEach { details ->
            val account = details.account
            if (account !in usedAccounts) {
                am.addAccountExplicitly(account, null, null)
            }
            account.updateDetails(am, details)
        }
        output.println("Done.")
    }

    private fun Account.updateDetails(am: AccountManager, details: AccountDetails) {
        am.setUserData(this, ACCOUNT_USER_DATA_KEY, details.key.toString())
        am.setUserData(this, ACCOUNT_USER_DATA_TYPE, details.type)
        am.setUserData(this, ACCOUNT_USER_DATA_CREDS_TYPE, details.credentials_type)

        am.setUserData(this, ACCOUNT_USER_DATA_ACTIVATED, true.toString())
        am.setUserData(this, ACCOUNT_USER_DATA_COLOR, toHexColor(details.color, format = HexColorFormat.RGB))

        am.setUserData(this, ACCOUNT_USER_DATA_USER, LoganSquare.serialize(details.user))
        am.setUserData(this, ACCOUNT_USER_DATA_EXTRAS, details.extras?.let { LoganSquare.serialize(it) })
        am.setAuthToken(this, ACCOUNT_AUTH_TOKEN_TYPE, LoganSquare.serialize(details.credentials))
    }

    fun ByteArray.toInt(): Int {
        val bb = ByteBuffer.wrap(this)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        return bb.int
    }

    fun Int.toByteArray(): ByteArray {
        val bb = ByteBuffer.allocate(Integer.SIZE / java.lang.Byte.SIZE)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        bb.putInt(this)
        return bb.array()
    }

    private fun generateSecret(password: String): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }

    internal class AsIsMappingProvider : MappingProvider {
        override fun <T : Any?> map(source: Any?, type: Class<T>, configuration: Configuration): T {
            @Suppress("UNCHECKED_CAST")
            return source as T
        }

        override fun <T : Any?> map(source: Any?, type: TypeRef<T>, configuration: Configuration): T {
            @Suppress("UNCHECKED_CAST")
            return source as T
        }

    }

}
