#include <jni.h>
#include <arpa/inet.h>
#include <string.h>

JNIEXPORT jint JNICALL
Java_org_mariotaku_twidere_util_net_InetAddressUtils_getInetAddressType(JNIEnv *env, jclass type,
                                                                        jstring input_) {
    if (input_ == NULL) return AF_UNSPEC;
    const char *input = (*env)->GetStringUTFChars(env, input_, 0);

    struct sockaddr addr;

    int addr_type = AF_UNSPEC;
    if (inet_pton(AF_INET, input, &addr) > 0) {
        addr_type = AF_INET;
    } else if (inet_pton(AF_INET6, input, &addr) > 0) {
        addr_type = AF_INET6;
    }
    (*env)->ReleaseStringUTFChars(env, input_, input);
    return addr_type;
}

JNIEXPORT jobject JNICALL
Java_org_mariotaku_twidere_util_net_InetAddressUtils_getResolvedIPAddress(JNIEnv *env, jclass type,
                                                                          jstring host_,
                                                                          jstring address_) {
    if (address_ == NULL) return NULL;

    const char *address = (*env)->GetStringUTFChars(env, address_, 0);

    jclass addressClass = (*env)->FindClass(env, "java/net/InetAddress");
    jmethodID getByAddressMethod = (*env)->GetStaticMethodID(env, addressClass, "getByAddress",
                                                             "(Ljava/lang/String;[B)Ljava/net/InetAddress;");

    void *bin_addr = malloc(16);
    memset(bin_addr, 0, 16);

    jbyteArray data = NULL;
    if (inet_pton(AF_INET, address, bin_addr) > 0) {
        data = (*env)->NewByteArray(env, 4);
        (*env)->SetByteArrayRegion(env, data, 0, 4, (jbyte *) bin_addr);
    } else if (inet_pton(AF_INET6, address, bin_addr) > 0) {
        data = (*env)->NewByteArray(env, 16);
        (*env)->SetByteArrayRegion(env, data, 0, 16, (jbyte *) bin_addr);
    }

    (*env)->ReleaseStringUTFChars(env, address_, address);
    free(bin_addr);

    if (data) {
        return (*env)->CallStaticObjectMethod(env, addressClass, getByAddressMethod, host_, data);
    }

    return NULL;

}