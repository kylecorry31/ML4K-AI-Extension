# TODO: Check if sources are download, if so just update them
git clone https://github.com/mit-cml/appinventor-sources
cp -r src/com/kylecorry appinventor-sources/appinventor/components/src/com/kylecorry
cd appinventor-sources
ant extensions
cd ..
mkdir _build
cp appinventor-sources/appinventor/components/build/externalComponents/com.kylecorry.ml4k.aix _build/ML4K.aix
