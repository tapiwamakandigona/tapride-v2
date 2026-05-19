package io.tapiwa.tapride.util

import android.util.Log
import io.tapiwa.tapride.BuildConfig

/**
 * Thin wrapper around [Log] that emits messages only in debug builds.
 *
 * Use this instead of [Log] directly so that no log output leaks
 * into production APKs.
 */
object TapLogger {

    /**
     * Logs a debug-level message.
     *
     * @param tag   Log tag (typically the class name).
     * @param msg   Message to log.
     */
    fun d(tag: String, msg: String) {
        if (BuildConfig.DEBUG) Log.d(tag, msg)
    }

    /**
     * Logs an error-level message with an optional [Throwable].
     *
     * @param tag   Log tag.
     * @param msg   Error description.
     * @param cause Optional exception to include in the log output.
     */
    fun e(tag: String, msg: String, cause: Throwable? = null) {
        if (BuildConfig.DEBUG) Log.e(tag, msg, cause)
    }

    /**
     * Logs a warning-level message.
     *
     * @param tag Log tag.
     * @param msg Warning message.
     */
    fun w(tag: String, msg: String) {
        if (BuildConfig.DEBUG) Log.w(tag, msg)
    }
}
