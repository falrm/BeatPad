#include <jni.h>
#include "fluidsynth.h"

fluid_settings_t *settings;
fluid_synth_t *synth;
fluid_audio_driver_t *adriver;

extern "C"
JNIEXPORT void JNICALL Java_opensles_android_fluidsynth_fluidsynth_1android_1opensles_NativeLibJNI_init(
        JNIEnv *env,
        jobject /* this */,
        jstring sf2path) {
    // Init settings
    settings = new_fluid_settings();
    fluid_settings_setstr(settings, "audio.driver", "oboe");
    fluid_settings_setstr(settings, "audio.oboe.performance-mode", "LowLatency");
//    fluid_settings_setstr(settings, "audio.oboe.sharing-mode", "Exclusive");
//    fluid_settings_setint(settings, "audio.opensles.use-callback-mode", 1);
//    fluid_settings_setint(settings, "audio.period-size", 1024);
//    fluid_settings_setint(settings, "audio.realtime-prio", 99);

    synth = new_fluid_synth(settings);
//
//    // Init soundfont
    const char *nativeSf2Path = env->GetStringUTFChars(sf2path, NULL);
    fluid_synth_sfload(synth, nativeSf2Path, true);
//    env->ReleaseStringUTFChars(sf2path, nativeSf2Path);
    fluid_synth_set_gain(synth, 1);
//
    adriver = new_fluid_audio_driver(settings, synth);
}

extern "C"
JNIEXPORT void JNICALL Java_opensles_android_fluidsynth_fluidsynth_1android_1opensles_NativeLibJNI_noteOn(
        JNIEnv *env,
        jobject /* this */,
        jint channel,
        jint key,
        jint velocity) {
    fluid_synth_noteon(synth, channel, key, velocity);
}

extern "C"
JNIEXPORT void JNICALL Java_opensles_android_fluidsynth_fluidsynth_1android_1opensles_NativeLibJNI_noteOff(
        JNIEnv *env,
        jobject /* this */,
        jint channel,
        jint key) {
    fluid_synth_noteoff(synth, channel, key);
}

extern "C"
JNIEXPORT jboolean JNICALL Java_opensles_android_fluidsynth_fluidsynth_1android_1opensles_NativeLibJNI_programChange(
        JNIEnv *env,
        jobject /* this */,
        jint channel,
        jint programNumber) {
    return (jboolean) (FLUID_OK == fluid_synth_program_change(synth, (int) channel, (int) programNumber));
}

extern "C"
JNIEXPORT jboolean JNICALL Java_opensles_android_fluidsynth_fluidsynth_1android_1opensles_NativeLibJNI_controlChange(
        JNIEnv *env,
        jobject /* this */,
        jint channel,
        jint control,
        jint value) {
    return (jboolean) (FLUID_OK == fluid_synth_cc(synth, (int) channel, (int) control, (int) value));
}

extern "C"
JNIEXPORT void JNICALL Java_opensles_android_fluidsynth_fluidsynth_1android_1opensles_NativeLibJNI_destroy(
        JNIEnv *env,
        jobject /* this */) {
    delete_fluid_audio_driver(adriver);
    delete_fluid_synth(synth);
    delete_fluid_settings(settings);
}
