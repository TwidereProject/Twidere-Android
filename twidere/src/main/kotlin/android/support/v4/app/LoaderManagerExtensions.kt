package android.support.v4.app

fun LoaderManager.hasRunningLoadersSafe(): Boolean {
    if (this !is LoaderManagerImpl) return false
    return try {
        hasRunningLoaders()
    } catch (e: Exception) {
        false
    }
}