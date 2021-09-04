var _ml4kBaseModel;

var _ml4kScratchKey = 'not-ready-yet';
var _ml4kNumClasses = 2;


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





function ml4kOnStart() {
    if (tf && tf.enableProdMode) {
        tf.enableProdMode();
    }
    console.log(tf.version);
    ML4KJavaInterface.setReady(true);
}

function ml4kTrainNewModel() {
    _ml4kPrepareMobilenet()
        .then(function (preparedBaseModel) {
            _ml4kBaseModel = preparedBaseModel;

            return _ml4kPrepareTransferLearningModel(_ml4kBaseModel, _ml4kNumClasses);
        })
        .then(function (transferModel) {
            ML4KJavaInterface.setModelReady(true);
        });
}



