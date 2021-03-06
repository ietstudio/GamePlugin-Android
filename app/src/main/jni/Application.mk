APP_STL := gnustl_static
NDK_TOOLCHAIN_VERSION=clang

APP_CPPFLAGS := -frtti -DCC_ENABLE_CHIPMUNK_INTEGRATION=1 -std=c++11 -fsigned-char
APP_LDFLAGS := -latomic
APP_PLATFORM := android-10
APP_CPPFLAGS += -DCC_LUA_ENGINE_ENABLED=1

APP_ABI := armeabi
