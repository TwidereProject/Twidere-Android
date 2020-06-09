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

import android.accounts.AccountManager
import android.content.Context
import android.util.Base64
import android.util.Base64InputStream
import android.util.Base64OutputStream
import com.facebook.stetho.dumpapp.DumpException
import com.facebook.stetho.dumpapp.DumperContext
import com.facebook.stetho.dumpapp.DumperPlugin
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.TypeRef
import com.jayway.jsonpath.spi.json.JsonOrgJsonProvider
import com.jayway.jsonpath.spi.mapper.MappingProvider
import org.apache.commons.cli.*
import org.json.JSONArray
import org.json.JSONObject
import org.mariotaku.ktextension.subArray
import org.mariotaku.twidere.exception.NoAccountException
import org.mariotaku.twidere.extension.model.updateDetails
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.JsonSerializer
import java.io.InputStream
import java.io.OutputStream
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

class AccountsDumperPlugin(val context: Context) : DumperPlugin {

    override fun getName() = "accounts"

    override fun dump(dumpContext: DumperContext) {
        val argsAsList = dumpContext.argsAsList
        val subCommands = listOf(ExportCommand(context), ImportCommand(context),
                ListCommand(context), GetCommand(context), SetCommand(context),
                SetJsonCommand(context))
        val subCommandName = argsAsList.firstOrNull()
        val subCommand = subCommands.find { it.name == subCommandName } ?: run {
            throw DumpException("Usage: accounts <${subCommands.joinToString("|", transform = SubCommand::name)}> <args>")
        }
        subCommand.execute(dumpContext, argsAsList.subArray(1..argsAsList.lastIndex))
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

    abstract class SubCommand(val name: String) {
        abstract fun execute(dumpContext: DumperContext, args: Array<String>)
    }

    class ExportCommand(val context: Context) : CmdLineSubCommand("export") {
        override val options = Options().apply {
            addRequiredOption("p", "password", true, "Account encryption password")
        }
        override val syntax: String = "[-p]"
        override fun execute(dumpContext: DumperContext, commandLine: CommandLine) {
            val am = AccountManager.get(context)
            try {
                val password = commandLine.getOptionValue("password")
                am.exportAccounts(password, dumpContext.stdout)
            } catch (e: Exception) {
                e.printStackTrace(dumpContext.stderr)
            }
        }
    }

    abstract class CmdLineSubCommand(name: String) : SubCommand(name) {

        protected abstract val options: Options
        protected abstract val syntax: String

        final override fun execute(dumpContext: DumperContext, args: Array<String>) {
            val commandLine = try {
                GnuParser().parse(options, args)
            } catch (e: ParseException) {
                val formatter = HelpFormatter()
                formatter.printHelp(dumpContext.stderr, "$name $syntax", options)
                return
            }
            execute(dumpContext, commandLine)
        }

        abstract fun execute(dumpContext: DumperContext, commandLine: CommandLine)

    }

    class ImportCommand(val context: Context) : CmdLineSubCommand("import") {

        override val options: Options = Options().apply {
            addRequiredOption("p", "password", true, "Account encryption password")
            addOption("t", "test", false, "Dry-run without actual import")
        }
        override val syntax: String = "[-pt]"

        override fun execute(dumpContext: DumperContext, commandLine: CommandLine) {
            val am = AccountManager.get(context)
            try {
                val password = commandLine.getOptionValue("password")
                val isTest = commandLine.hasOption("test")
                val accounts = readAccounts(password, dumpContext.stdin)
                if (isTest) {
                    accounts.forEach { dumpContext.stdout.println(it.key) }
                } else {
                    am.importAccounts(accounts)
                }
            } catch (e: Exception) {
                e.printStackTrace(dumpContext.stderr)
            }
        }
    }

    class ListCommand(val context: Context) : SubCommand("list") {
        override fun execute(dumpContext: DumperContext, args: Array<String>) {
            val keys = DataStoreUtils.getAccountKeys(context)
            keys.forEach {
                dumpContext.stdout.println(it)
            }
        }
    }

    class GetCommand(val context: Context) : SubCommand("get-value") {
        override fun execute(dumpContext: DumperContext, args: Array<String>) {
            if (args.isEmpty()) {
                throw DumpException("Usage: accounts $name <account_key> [value1] [value2] ...")
            }
            val am = AccountManager.get(context)
            val docContext = try {
                am.docContext(args[0])
            } catch (e: NoAccountException) {
                throw DumpException("Account not found")
            }
            if (args.size == 1) {
                val result = docContext.read("$", Object::class.java)
                dumpContext.stdout.println(result?.prettyPrint())
            } else for (i in 1..args.lastIndex) {
                val result = docContext.read(args[i], Object::class.java)
                dumpContext.stdout.println(result?.prettyPrint())
            }
        }

    }

    class SetCommand(val context: Context) : SubCommand("set-value") {
        override fun execute(dumpContext: DumperContext, args: Array<String>) {
            if (args.size != 3) {
                throw DumpException("Usage: accounts $name <account_key> <field> <value>")
            }
            val am = AccountManager.get(context)
            val docContext = try {
                am.docContext(args[0])
            } catch (e: NoAccountException) {
                throw DumpException("Account not found")
            }
            val value = args[2]
            val path = args[1]
            docContext.set(path, value)
            val details = docContext.read("$", Object::class.java)?.let {
                JsonSerializer.parse(it.toString(), AccountDetails::class.java)
            } ?: return
            details.account.updateDetails(am, details)
            dumpContext.stdout.println("$path = ${docContext.read(path, Object::class.java)?.prettyPrint()}")
        }

    }

    class SetJsonCommand(val context: Context) : SubCommand("set-json") {
        override fun execute(dumpContext: DumperContext, args: Array<String>) {
            if (args.size != 3) {
                throw DumpException("Usage: accounts $name <account_key> <field> <json>")
            }
            val am = AccountManager.get(context)
            val docContext = try {
                am.docContext(args[0])
            } catch (e: NoAccountException) {
                throw DumpException("Account not found")
            }
            val value = args[2]
            val path = args[1]
            if (value.startsWith("{")) {
                docContext.set(path, JSONObject(value))
            } else if (value.startsWith("[")) {
                docContext.set(path, JSONArray(value))
            }
            val details = docContext.read("$", Object::class.java)?.let {
                JsonSerializer.parse(it.toString(), AccountDetails::class.java)
            } ?: return
            details.account.updateDetails(am, details)
            dumpContext.stdout.println("$path = ${docContext.read(path, Object::class.java)?.prettyPrint()}")
        }

    }

    companion object {

        private val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        private val salt = byteArrayOf(0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8)

        private fun AccountManager.exportAccounts(password: String, output: OutputStream) {
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
            val accounts = AccountUtils.getAllAccountDetails(this, true).toList()
            JsonSerializer.serialize(accounts, gz, AccountDetails::class.java)
        }

        private fun readAccounts(password: String, input: InputStream): List<AccountDetails> {
            val base64 = Base64InputStream(input, Base64.NO_CLOSE)

            val ivSize = ByteArray(4).apply { base64.read(this) }.toInt()
            val iv = ByteArray(ivSize).apply { base64.read(this) }

            val secret = generateSecret(password)

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secret, IvParameterSpec(iv))
            val gz = GZIPInputStream(CipherInputStream(base64, cipher))
            return JsonSerializer.parseList(gz, AccountDetails::class.java)
        }

        private fun AccountManager.importAccounts(allDetails: List<AccountDetails>) {
            val usedAccounts = AccountUtils.getAccounts(this)
            allDetails.forEach { details ->
                val account = details.account
                if (account !in usedAccounts) {
                    this.addAccountExplicitly(account, null, null)
                }
                account.updateDetails(this, details)
            }
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

        private fun AccountManager.docContext(forKey: String): DocumentContext {
            val accountKey = UserKey.valueOf(forKey)
            val details = AccountUtils.getAccountDetails(this, accountKey, true) ?: throw NoAccountException()
            val configuration = Configuration.builder()
                    .jsonProvider(JsonOrgJsonProvider())
                    .mappingProvider(AsIsMappingProvider())
                    .build()
            return JsonPath.parse(JsonSerializer.serialize(details), configuration)
        }

        private fun Any.prettyPrint() = when (this) {
            is JSONObject -> toString(4)
            is JSONArray -> toString(4)
            else -> toString()
        }

    }
}
