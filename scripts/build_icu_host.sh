#set HOST
#start in build/

export ICU_SOURCES=$PWD/Source/icu
cd icu_host
export CFLAGS="-Os -DU_USING_ICU_NAMESPACE=1 -fno-short-enums \
-DUEXPORT2=__attribute__\(\(visibility\(\"default\"\)\)\) \
-DU_HAVE_NL_LANGINFO_CODESET=0 -D__STDC_INT64__ -DU_TIMEZONE=0 \
-DUCONFIG_NO_LEGACY_CONVERSION=1 -DUCONFIG_NO_BREAK_ITERATION=1 \
-DUCONFIG_NO_COLLATION=0 -DUCONFIG_NO_FORMATTING=0 -DUCONFIG_NO_TRANSLITERATION=0 \
-DUCONFIG_NO_REGULAR_EXPRESSIONS=1"
export CPPFLAGS="-Os -DU_USING_ICU_NAMESPACE=1 -fno-short-enums \
-DUEXPORT2=__attribute__\(\(visibility\(\"default\"\)\)\) \
-DU_HAVE_NL_LANGINFO_CODESET=0 -D__STDC_INT64__ -DU_TIMEZONE=0 \
-DUCONFIG_NO_LEGACY_CONVERSION=1 -DUCONFIG_NO_BREAK_ITERATION=1 \
-DUCONFIG_NO_COLLATION=0 -DUCONFIG_NO_FORMATTING=0 -DUCONFIG_NO_TRANSLITERATION=0 \
-DUCONFIG_NO_REGULAR_EXPRESSIONS=1"

sh $ICU_SOURCES/source/runConfigureICU $HOST --prefix=$PWD/icu_build --enable-extras=yes \
--enable-strict=no --enable-static --enable-shared=no --enable-tests=yes \
--enable-samples=no --enable-dyload=no
make -j8
make install


