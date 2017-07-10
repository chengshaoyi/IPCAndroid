//
// Created by Shaoyi Cheng on 7/10/2017.
//
#include <jni.h>
#include <string>
#include <sys/mman.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <linux/ashmem.h>
#include <sys/ioctl.h>
#include <android/log.h>

#define LOG_TAG "sharedMem"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)




jint* map;

extern "C"
{
    void Java_com_numericcal_serviceuser_MainActivity_shmemInit(JNIEnv *env, jobject, jint fd, jint numPages)
    {

        map = (jint *)mmap(0, numPages*PAGE_SIZE, PROT_READ|PROT_WRITE, MAP_SHARED, fd, 0);
        LOGD("%d amount of memory\n",numPages*PAGE_SIZE);
    }
    void Java_com_numericcal_serviceuser_MainActivity_shmemSend(JNIEnv *env, jobject, jintArray imgByteArr)
    {

        int len = env->GetArrayLength(imgByteArr);
        env->GetIntArrayRegion (imgByteArr, 0, len, map);

    }
}
