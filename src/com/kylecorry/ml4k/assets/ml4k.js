// ---------------------------------------------------------------------

var _ML4K_IMG_WIDTH = 224;
var _ML4K_IMG_HEIGHT = 224;

var _ML4K_MODEL_TYPE = 'images';

// ---------------------------------------------------------------------

var _ml4kBaseModel;
var _ml4kTransferModel;

var _ml4kUsingRestoredModel = false;

// ---------------------------------------------------------------------

function _ml4kFetchJson(urlString) {
    return fetch(urlString)
        .then(function (resp) {
            if (resp.ok) {
                return resp.json();
            }
            else {
                console.log(resp);
                throw new Error('Error in communicating with Machine Learning for Kids server');
            }
        });
}

function _ml4kFetchData(urlString) {
    return fetch(urlString)
        .then(function (resp) {
            if (resp.ok) {
                return resp.arrayBuffer();
            }
            else {
                console.log(resp);
                throw new Error('Error in downloading image data');
            }
        });
}

// ---------------------------------------------------------------------

function _ml4kGetModelDbLocation(modeltype, scratchkey) {
    return 'indexeddb://ml4k-models-' +
            modeltype + '-' +
            scratchkey.replace(/-/g, '');
}

function _ml4kLoadModel(modeltype, scratchkey) {
    console.log('loading model');
    var savelocation = _ml4kGetModelDbLocation(modeltype, scratchkey);
    return tf.loadLayersModel(savelocation)
        .then(function (resp) {
            console.log('loaded model');
            ML4KJavaInterface.setModelStatus('Available', 100);
            _ml4kUsingRestoredModel = true;
            return resp;
        })
        .catch(function (err) {
            console.log('failed to load model');
            console.log(err);
        });
}

function _ml4kSaveModel(modeltype, scratchkey, transferModel) {
    console.log('saving model');
    var savelocation = _ml4kGetModelDbLocation(modeltype, scratchkey);
    return transferModel.save(savelocation)
        .catch(function (err) {
            console.log('failed to save model');
            console.log(err);
        });
}

function _ml4kDeleteModel(modeltype, scratchkey) {
    console.log('deleting model');
    var savelocation = _ml4kGetModelDbLocation(modeltype, scratchkey);
    return tf.io.removeModel(savelocation)
        .catch(function (err) {
            console.log('failed to delete model');
            console.log(err);
        });
}

// ---------------------------------------------------------------------


function _ml4kPrepareMobilenet() {
    return tf.loadLayersModel('https://machinelearningforkids.co.uk/appinventor-assets/model.json')
        .then(function (pretrainedModel) {
            var activationLayer = pretrainedModel.getLayer('conv_pw_13_relu');
            return tf.model({
                inputs : pretrainedModel.inputs,
                outputs: activationLayer.output
            });
        })
        .catch(function (err) {
            console.log('failed to prepare mobilenet');
            console.log(err);
            throw err;
        });
}

function _ml4kPrepareTransferLearningModel(modifiedMobilenet, numClasses) {
    var model = tf.sequential({
        layers : [
            tf.layers.flatten({
                inputShape : modifiedMobilenet.outputs[0].shape.slice(1)
            }),
            tf.layers.dense({
                units : 100,
                activation : 'relu',
                kernelInitializer : 'varianceScaling',
                useBias : true
            }),
            tf.layers.dense({
                units : numClasses,
                activation : 'softmax',
                kernelInitializer : 'varianceScaling',
                useBias : false
            })
        ]
    });

    model.compile({
        optimizer : tf.train.adam(0.0001),
        loss : 'categoricalCrossentropy'
    });

    return model;
}

function _ml4kTrainModel(baseModel, transferModel, scratchkey, trainingdata) {
    ML4KJavaInterface.setModelStatus('Training', 0);

    var modelClasses = trainingdata.labels;

    var xs;
    var ys;

    for (var i=0; i < trainingdata.imagedata.length; i++) {
        var trainingdataitem = trainingdata.imagedata[i];
        // { id : imageid, label : imagelabel, tensor : tensorData }

        var labelIdx = modelClasses.indexOf(trainingdataitem.label);

        var xval = baseModel.predict(trainingdataitem.tensor);
        var yval = tf.tidy(function () {
            return tf.oneHot(tf.tensor1d([ labelIdx ]).toInt(), modelClasses.length);
        });

        if (i === 0) {
            xs = xval;
            ys = yval;
        }
        else {
            var oldxs = xs;
            var oldys = ys;
            xs = oldxs.concat(xval, 0);
            ys = oldys.concat(yval, 0);

            oldxs.dispose();
            oldys.dispose();
        }
    }

    var epochs = 10;
    if (trainingdata.imagedata.length > 55) {
        epochs = 15;
    }

    transferModel.fit(xs, ys, {
        batchSize : 10,
        epochs : epochs,
        callbacks : {
            onEpochEnd : function (epoch, logs) {
                console.log('epoch ' + epoch + ' loss ' + logs.loss);
                if (epochs === 15) {
                    ML4KJavaInterface.setModelStatus('Training', (epoch + 1) * 7);
                }
                else {
                    ML4KJavaInterface.setModelStatus('Training', (epoch + 1) * 10);
                }
            },
            onTrainEnd : function () {
                return _ml4kSaveModel(_ML4K_MODEL_TYPE, scratchkey, transferModel)
                    .then(function () {
                        console.log('training complete');
                        ML4KJavaInterface.setModelStatus('Available', 100);

                        _ml4kUsingRestoredModel = false;
                    });
            }
        }
    });
}

// ---------------------------------------------------------------------

function _ml4kCreateTensorForImageData(imageid, imagedata, imagelabel) {
    return new Promise(function (resolve, reject) {
        var imageDataBlob = URL.createObjectURL(new Blob([ imagedata ]));

        var hiddenImg = document.createElement('img');
        hiddenImg.id = '_ml4k_' + imageid;
        hiddenImg.width = _ML4K_IMG_WIDTH;
        hiddenImg.height = _ML4K_IMG_HEIGHT;
        hiddenImg.onerror = function (err) {
            console.log('[ml4kimages] Failed to load image', imageid);
            return reject(err);
        };
        hiddenImg.onload = function () {
            var tensorData = tf.tidy(function () {
                return tf.browser.fromPixels(hiddenImg)
                            .expandDims(0)
                            .toFloat()
                            .div(127)
                            .sub(1);
            });

            resolve({ id : imageid, label : imagelabel, tensor : tensorData });

            URL.revokeObjectURL(imageDataBlob);
        };

        hiddenImg.src = imageDataBlob;
    });
}

function _ml4kGetImageData(imageid, imageurl, imagelabel) {
    console.log('getImageData : ' + imageid + ' : ' + imageurl);
    return _ml4kFetchData(imageurl)
        .then(function (imagedata) {
            return _ml4kCreateTensorForImageData(imageid, imagedata, imagelabel);
        });
}

function _ml4kGetTrainingImages(scratchkey) {
    console.log('getting training images');
    var labels = new Set();
    return _ml4kFetchJson('https://machinelearningforkids.co.uk/api/scratch/' + scratchkey + '/train')
        .then(function (imagesList) {
            // return Promise.all(imagesList.map(function (imageInfo) {
            //     return _ml4kGetImageData(imageInfo.id, imageInfo.imageurl, imageInfo.label);
            // }));
            return pool({
                collection: imagesList,
                maxConcurrency: 10,
                task: function (imageInfo) {
                    labels.add(imageInfo.label);
                    return _ml4kGetImageData(imageInfo.id, imageInfo.imageurl, imageInfo.label);
                }
            });
        })
        .then(function (trainingimages) {
            return { imagedata : trainingimages, labels : Array.from(labels) };
        });
}

// ---------------------------------------------------------------------



function ml4kTrainNewModel(scratchkey) {
    return _ml4kGetTrainingImages(scratchkey)
        .then(function (trainingdata) {
            if (_ml4kUsingRestoredModel || !_ml4kTransferModel) {
                _ml4kTransferModel = _ml4kPrepareTransferLearningModel(_ml4kBaseModel, trainingdata.labels.length);
            }
            return _ml4kTrainModel(_ml4kBaseModel, _ml4kTransferModel, scratchkey, trainingdata);
        })
        .then(function () {
            ML4KJavaInterface.setModelReady(true);
        })
        .catch(function (err) {
            console.log('model training failure');
            console.log(err);
            ML4KJavaInterface.setModelStatus('Failed', 0);
        });
}




function ml4kOnStart(scratchkey) {
    if (tf && tf.enableProdMode) {
        tf.enableProdMode();
    }
    console.log(tf.version);

    return _ml4kPrepareMobilenet()
        .then(function (preparedBaseModel) {
            _ml4kBaseModel = preparedBaseModel;

            if (scratchkey) {
                return _ml4kLoadModel(_ML4K_MODEL_TYPE, scratchkey);
            }
        })
        .then(function (loadedmodel) {
            if (loadedmodel) {
                _ml4kTransferModel = loadedmodel;
            }

            ML4KJavaInterface.setReady(true);
        });
}
