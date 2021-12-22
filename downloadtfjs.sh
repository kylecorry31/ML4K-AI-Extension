#!/bin/bash

if [ ! -f "../src/com/kylecorry/ml4k/assets/model.json" ]; then
    echo "Downloading tfjs files..."
    curl https://storage.googleapis.com/tfjs-models/tfjs/mobilenet_v1_0.25_224/model.json -o ../src/com/kylecorry/ml4k/assets/model.json
    for i in $(seq 1 55)
    do
        curl https://storage.googleapis.com/tfjs-models/tfjs/mobilenet_v1_0.25_224/group$i-shard1of1 -o ../src/com/kylecorry/ml4k/assets/group$i-shard1of1
    done
    npm install @tensorflow/tfjs@3.12.0
    cp node_modules/@tensorflow/tfjs/dist/tf.min.js ../src/com/kylecorry/ml4k/assets/.
    npm install @ricokahler/pool
    cp node_modules/@ricokahler/pool/dist/index.js ../src/com/kylecorry/ml4k/assets/promisepool.js
fi