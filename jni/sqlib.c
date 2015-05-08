#include <jni.h>
#include <string.h>
#include <stdio.h>
#include "md5.h"

/**
 * GET PASSWORD BY JNI MD5
 * 
 * author  YZ Li
 * since   2015.5.4
 */

 /**
    Calculate the MD5 of str
 */
jstring* getMD5(JNIEnv *env, jstring *str)
{
    char* szText = (char*)(*env)->GetStringUTFChars(env, str, 0);
    MD5_CTX context = { 0 };
	MD5Init(&context);
	MD5Update(&context, szText, strlen(szText));
	unsigned char dest[16] = { 0 };
	MD5Final(dest, &context);
    (*env)->ReleaseStringUTFChars(env, str, szText);
    
	int i = 0;
	char szMd5[32] = { 0 };
	for (i = 0; i < 16; i++)
	{
		sprintf(szMd5, "%s%02x", szMd5, dest[i]);
	}

	return (*env)->NewStringUTF(env, szMd5);
}

/**
*  Get the apk signature's hashcode
*/
int get_apk_signature(JNIEnv *env, jobject obj) 
{
    jclass cls = (*env)->FindClass(env, "android/content/ContextWrapper");
    //this.getPackageManager();
    jmethodID mid = (*env)->GetMethodID(env, cls, "getPackageManager",
            "()Landroid/content/pm/PackageManager;");
    if (mid == NULL) {
        return -1;
    }

    jobject pm = (*env)->CallObjectMethod(env, obj, mid);
    if (pm == NULL) {
        return -2;
    }

    //this.getPackageName();
    mid = (*env)->GetMethodID(env, cls, "getPackageName", "()Ljava/lang/String;");
    if (mid == NULL) {
        return -3;
    }

    jstring packageName = (jstring)(*env)->CallObjectMethod(env, obj, mid);

    // packageManager->getPackageInfo(packageName, GET_SIGNATURES);
    cls = (*env)->GetObjectClass(env, pm);
    mid  = (*env)->GetMethodID(env, cls, "getPackageInfo", "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    jobject packageInfo = (*env)->CallObjectMethod(env, pm, mid, packageName, 0x40); //GET_SIGNATURES = 64;
    cls = (*env)->GetObjectClass(env, packageInfo);
    jfieldID fid = (*env)->GetFieldID(env, cls, "signatures", "[Landroid/content/pm/Signature;");
    jobjectArray signatures = (jobjectArray)(*env)->GetObjectField(env, packageInfo, fid);
    jobject sig = (*env)->GetObjectArrayElement(env, signatures, 0);

    cls = (*env)->GetObjectClass(env, sig);
    mid = (*env)->GetMethodID(env, cls, "hashCode", "()I");
    int sig_value = (int)(*env)->CallIntMethod(env, sig, mid);
    return sig_value;
}

/**
 *  itoa JNI version
*/
jstring itojstr(JNIEnv* env, jobject mContext, jint num)
{
    char buf[64];
    if (num < 0)
        num = 6581;
    sprintf(buf, "%d", num);
    return (*env)->NewStringUTF(env, buf);
}

/**
 *  Get Android_ID
 */
jstring get_AID(JNIEnv* env, jobject mContext, jobject actObj)
{
    if (!mContext) {
        return NULL;
    }

    jclass resCls = (*env)->FindClass(env, "android/content/Context");
    jmethodID getMethod = (*env)->GetMethodID(env, resCls, "getContentResolver", "()Landroid/content/ContentResolver;");
    jobject resolver = (*env)->CallObjectMethod(env, actObj, getMethod);
    if (resolver == NULL) {
        return NULL;
    }

    jclass cls_context = (*env)->FindClass(env, "android/provider/Settings$Secure");
    if (cls_context == NULL) {
        return NULL;
    }

    jmethodID getStringMethod = (*env)->GetStaticMethodID(env, cls_context, "getString", "(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;");
    if (getStringMethod == NULL) {
        return NULL;
    }

    jfieldID ANDROID_ID = (*env)->GetStaticFieldID(env, cls_context, "ANDROID_ID", "Ljava/lang/String;");
    jstring str = (jstring)((*env)->GetStaticObjectField(env, cls_context, ANDROID_ID));
    jstring jID = (jstring)((*env)->CallStaticObjectMethod(env, cls_context, getStringMethod, resolver, str));
    
    return jID;
}    

/**
 *  initSQL KEY JNI Version
 */
jstring
Java_com_contact_ContactDbAdapter_initSQL(JNIEnv* env, jobject mContext, jobject actObj)
{
    char *concat, *coded;
    const jbyte *AID, *Sig;
    jstring jconcat, jcoded, jret;
    
    jstring jAID = get_AID(env, mContext, actObj);
    jstring jSig = itojstr(env, mContext, (jint)get_apk_signature(env, actObj));
    
    AID = (*env)->GetStringUTFChars(env, jAID, 0);
    Sig = (*env)->GetStringUTFChars(env, jSig, 0);
    if (jAID == NULL)
        concat = malloc(strlen("E328") + strlen(Sig) + 1);
    else
        concat = malloc(strlen(AID) + strlen(Sig) + 1);
    strcpy(concat, AID);
    strcat(concat, Sig);
    jconcat = (*env)->NewStringUTF(env, concat);
    
    (*env)->ReleaseStringUTFChars(env, jAID, AID);
    (*env)->ReleaseStringUTFChars(env, jSig, Sig);
    free(concat);
    
    jcoded = getMD5(env, jconcat);
    coded = (*env)->GetStringUTFChars(env, jcoded, 0);
    *(coded+10) = '\0';
    jret = (*env)->NewStringUTF(env, coded);
    (*env)->ReleaseStringUTFChars(env, jcoded, coded);
    
    return jret;
}