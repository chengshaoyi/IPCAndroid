#include <jni.h>
#include <string>
#include <sys/mman.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <linux/ashmem.h>
#include <sys/ioctl.h>

int* map;
extern "C"
{
    jint Java_com_numericcal_serviceprovider_SharedMemoryService_nativeGetFD(JNIEnv *env, jobject, jstring n, jint numPages)
    {
        const char *name = (env)->GetStringUTFChars(n, NULL);

        jint fd = open("/dev/ashmem", O_RDWR);

        ioctl(fd, ASHMEM_SET_NAME, name);

        ioctl(fd, ASHMEM_SET_SIZE, numPages*PAGE_SIZE);

        map = (int *)mmap(0, numPages*PAGE_SIZE, PROT_READ|PROT_WRITE, MAP_SHARED, fd, 0);
        // fill some data for testing purpose
        map[0]=100;
        map[10]=188;

        (env)->ReleaseStringUTFChars(n, name);

        return fd;

    }
    jint Java_com_numericcal_serviceprovider_SharedMemoryService_nativeGetData(JNIEnv *env, jobject, jint ind)
    {
        return map[ind];
    }
}