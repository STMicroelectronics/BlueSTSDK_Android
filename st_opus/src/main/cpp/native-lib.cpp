#include <jni.h>
#include <string>
//#include <android/log.h>
#include "opus/include/opus.h"


OpusDecoder *decoder;
OpusEncoder *encoder;


extern "C" JNIEXPORT jint JNICALL
Java_com_st_BlueSTSDK_Features_Audio_Opus_OpusManager_OpusDecInit(
        JNIEnv* env,
        jobject thiz,
        jint sampFreq,
        jint channels
        ) {
   jint result = 0;

   decoder = opus_decoder_create(sampFreq, channels, &result);
    //__android_log_print(ANDROID_LOG_ERROR, "OpusDecInit", "%d result", result);
   return result;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_st_BlueSTSDK_Features_Audio_Opus_OpusManager_OpusDecode(
                JNIEnv* env,
                jobject thiz,
                jbyteArray input,
                jint in_length,
                jint frameSizePcm
                ) {

    jint num_byte;
    jbyte decoded[frameSizePcm*2];

    jbyteArray result = env->NewByteArray(frameSizePcm*2);
    jbyte *encoded = env->GetByteArrayElements(input, nullptr);

    num_byte = opus_decode(decoder, (unsigned char *)encoded, (opus_int32)in_length, (opus_int16 *)decoded, frameSizePcm, 0);
    env->SetByteArrayRegion(result, 0, frameSizePcm*2, decoded);

    //__android_log_print(ANDROID_LOG_ERROR, "OpusDecode", "....");

    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_st_BlueSTSDK_Features_Audio_Opus_OpusManager_OpusEncInit(JNIEnv* env,
                jobject thiz,
                jint sampFreq,
                jint channels,
                jint app,
                jint bitrate,
                jboolean cvbr,
                jint complexity) {
    jint result = 0;
   // __android_log_print(ANDROID_LOG_ERROR, "OpusEncInit", "%d sampFreq, %d channels, %d app,%d bitrate, %d cvbr, %d complexity", sampFreq,channels,app,bitrate,cvbr,complexity);

    encoder = opus_encoder_create(sampFreq,channels, app, &result);
    result = opus_encoder_ctl(encoder, OPUS_SET_BITRATE(bitrate));
    result = opus_encoder_ctl(encoder, OPUS_SET_VBR(cvbr));
    result = opus_encoder_ctl(encoder, OPUS_SET_COMPLEXITY(complexity));

   // __android_log_print(ANDROID_LOG_ERROR, "OpusEncInit", "%d result", result);

    return result;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_st_BlueSTSDK_Features_Audio_Opus_OpusManager_OpusEncode(JNIEnv* env,
        jobject thiz,
        jshortArray input,
        jint encodedFrameSize,
        jint frameSizePcm,
        jint channels) {

    jint num_byte;
    //jbyteArray encoded = env->NewByteArray(frameSizePcm);
    jbyte encoded[encodedFrameSize];

    jshort *audioIn = env->GetShortArrayElements(input,nullptr);

    //__android_log_print(ANDROID_LOG_ERROR, "OpusEncode", " %d encodedFrameSize, %d frameSizePcm, %d channels",encodedFrameSize,frameSizePcm,channels);

    //Returns The length of the encoded packet (in bytes) on success or a negative error code (see Error codes) on failure.
    num_byte = opus_encode(encoder, (opus_int16 *)audioIn, frameSizePcm, (unsigned char *)encoded, (opus_int32)encodedFrameSize);

    //__android_log_print(ANDROID_LOG_ERROR, "OpusEncode", ".....");

    jbyteArray output = env->NewByteArray(num_byte);//the output
    env->SetByteArrayRegion( output, 0, num_byte, encoded);

    return output;
}
