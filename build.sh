mkdir -p _build
cd _build
if [ ! -d "appinventor-sources" ]; then
git clone https://github.com/mit-cml/appinventor-sources
fi
cp -r ../src/com/kylecorry appinventor-sources/appinventor/components/src/com/
cd appinventor-sources/appinventor
ant extensions
cd ../..
mkdir -p dist
cp appinventor-sources/appinventor/components/build/extensions/com.kylecorry.ml4k.aix dist/ML4K.aix
