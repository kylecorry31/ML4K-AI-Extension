# Using Project Examples

After importing the example project .aia file into App Inventor, you __MUST__ add your project's Machine Learning for Kids API key to the example in order to use the example. That means you must create your own project and add your own examples on the Machine Learning for Kids site (https://machinelearningforkids.co.uk). This API Key is not the IBM Watson API keys used to create your Machine Learning for Kids account. This key is created by Machine Learning for Kids and is specific to your project.   

The API key can be found on the App Inventor project page in the unique URL for your project.  
  
![](AppInProject.jpg)  
  
Or, the key can be found on the Python project page.  
  
![](PyProject.jpg)  
  
**__Do not use the keys displayed in the images above. Use the keys from your project pages.__**  

Copy and paste the API Key into the ML4K component’s “Key” property on the Designer screen or use the "set Key" block on the Blocks screen. Note: API Key must be set before you can use any of the ML4K extension blocks for classification. If you choose to set the key using the “set Key” block, be sure to set the key in the Screen.Initialize event or any time before you use a classification method (purple block).

![Set Key](set_key.png)  
