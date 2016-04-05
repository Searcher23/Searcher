#include <string.h>
#include "7za.h"
#include "com_free_p7zip_Andro7za.h"

#define  ARGC 7
static const char *test_args[ARGC + 1] =
		{ "7za", 
		"x", 
		"/mnt/sdcard/7za123456789.7z",
		"-o/mnt/sdcard/extractarchiveandroid",
		"-aos", 
		"", 
		"",
		0 };
		
JNIEXPORT jint JNICALL Java_com_free_p7zip_Andro7za_a7zaCommand(
		JNIEnv *env, jobject obj, 
		jstring _command, 
		jstring _pathArchive,
		jstring _type,
		jstring _password,
		jstring _outputDirOrCompressionLevel,
		jstring _fList4CompressOrExtract) {

	const char *command = env->GetStringUTFChars(_command, 0);
	const char *pathArchive = env->GetStringUTFChars(_pathArchive, 0);
	const char *type = env->GetStringUTFChars(_type, 0);
	const char *password = env->GetStringUTFChars(_password, 0);
	const char *outputDirOrCompressionLevel = env->GetStringUTFChars(_outputDirOrCompressionLevel, 0);
	const char *fList4CompressOrExtract = env->GetStringUTFChars(_fList4CompressOrExtract, 0);
		
	test_args[1] = command;
	test_args[2] = pathArchive;
	test_args[3] = type;
	test_args[4] = password;
	test_args[5] = outputDirOrCompressionLevel;
	test_args[6] = fList4CompressOrExtract;
	
	jint ret;
	try {
		ret = main(ARGC, test_args);
	} catch (...) {
		ret = 2;
	}

	// Release strings
	env->ReleaseStringUTFChars(_command, command);
    env->ReleaseStringUTFChars(_pathArchive, pathArchive);
    env->ReleaseStringUTFChars(_type, type);
	env->ReleaseStringUTFChars(_password, password);
    env->ReleaseStringUTFChars(_outputDirOrCompressionLevel, outputDirOrCompressionLevel);
    env->ReleaseStringUTFChars(_fList4CompressOrExtract, fList4CompressOrExtract);
	
	return ret;
}
