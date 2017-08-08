package android.support.v4.app

/**
 * Created by mariotaku on 2016/11/26.
 */

fun LoaderManager.hasRunningLoadersSafe(): Boolean {
    if (this !is LoaderManagerImpl) return false
    var loadersRunning = false
    val count = mLoaders.size()
    for (i in 0 until count) {
        val li = mLoaders.valueAt(i) ?: continue
        loadersRunning = loadersRunning or (li.mStarted && !li.mDeliveredData)
    }
    return loadersRunning
}