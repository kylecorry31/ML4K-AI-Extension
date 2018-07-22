# ML4K AppInventor Extension
Use machine learning in AppInventor, with easy training using text or images through the [Machine Learning for Kids](https://machinelearningforkids.co.uk/) website.

## Example
![ML4K Text Example](examples/ml4k-text.PNG)

## Installation
Download the .aix file from the [releases](https://github.com/kylecorry31/ML4K-AI-Extension/releases) page and follow the "2. How to use extensions components" section of [this website](http://ai2.appinventor.mit.edu/reference/other/extensions.html).

## Guide
1. After installing the extension, you need to get an API key, which can be obtained from [Machine Learning for Kids](https://machinelearningforkids.co.uk/).

2. Use the "set Key" block with the API key you obtained to initialize the extension.

3. Classify the text or image.
  If using ML4K to classify text, use the "ClassifyText" block with the text to classify.
  If using ML4K to classify images, use the "ClassifyImage" block with the image path to classify.

4. Use the "GotClassification" block to retrieve the classification once it is completed.

### Handling Errors
Upon an error, the "GotClassification" block will set the classification to ERROR and the confidence to 0. This will be changed soon to use an error catch block such as "GotClassificationError".

## License
This project is licensed under the MIT License.

## Credits
The Machine Learning for Kids repository can be found here https://github.com/IBM/taxinomitis

## Contribute
Please feel free to contribute to this extension, or if you find an issue be sure to report it under issues.
