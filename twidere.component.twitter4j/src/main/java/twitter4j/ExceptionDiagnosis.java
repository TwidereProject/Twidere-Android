/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.3
 */
final class ExceptionDiagnosis {
	int stackLineHash;
	int lineNumberHash;
	String hexString = "";
	Throwable th;

	ExceptionDiagnosis(final Throwable th) {
		this(th, new String[] {});
	}

	ExceptionDiagnosis(final Throwable th, final String[] inclusionFilter) {
		this.th = th;

		final StackTraceElement[] stackTrace = th.getStackTrace();
		stackLineHash = 0;
		lineNumberHash = 0;
		for (int i = stackTrace.length - 1; i >= 0; i--) {
			final StackTraceElement line = stackTrace[i];
			for (final String filter : inclusionFilter) {
				if (line.getClassName().startsWith(filter)) {
					final int hash = line.getClassName().hashCode() + line.getMethodName().hashCode();
					stackLineHash = 31 * stackLineHash + hash;
					lineNumberHash = 31 * lineNumberHash + line.getLineNumber();
					break;
				}
			}
		}
		hexString += toHexString(stackLineHash) + "-" + toHexString(lineNumberHash);
		if (th.getCause() != null) {
			hexString += " " + new ExceptionDiagnosis(th.getCause(), inclusionFilter).asHexString();
		}

	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final ExceptionDiagnosis that = (ExceptionDiagnosis) o;

		if (lineNumberHash != that.lineNumberHash) return false;
		if (stackLineHash != that.stackLineHash) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = stackLineHash;
		result = 31 * result + lineNumberHash;
		return result;
	}

	@Override
	public String toString() {
		return "ExceptionDiagnosis{" + "stackLineHash=" + stackLineHash + ", lineNumberHash=" + lineNumberHash + '}';
	}

	private String toHexString(final int value) {
		final String str = "0000000" + Integer.toHexString(value);
		return str.substring(str.length() - 8, str.length());
	}

	String asHexString() {
		return hexString;
	}

	int getLineNumberHash() {
		return lineNumberHash;
	}

	String getLineNumberHashAsHex() {
		return toHexString(lineNumberHash);
	}

	int getStackLineHash() {
		return stackLineHash;
	}

	String getStackLineHashAsHex() {
		return toHexString(stackLineHash);
	}
}
