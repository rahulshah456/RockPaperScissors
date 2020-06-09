# Rock Paper Scissors
TensorflowLite Based Rock Paper Scissors App Implementation <br />
This app detects whether a hand gesture is Rock, Paper or Scissors by using a custom remote Tensorflow Lite model deployed in Firebase via ML Kit. You can play with this feature through the selecting single or multiple images from gallery of capture live from the camera.


# Screenshots
- This is the home page of the application which includes the basic info and types of methods how you can detect your given image as Rock Papers and Scissors directly

<img src="https://github.com/rahulshah456/RetailPulseAssignment/blob/master/screenshots/1.png" /> 


- This is the gallery method Implementation which includes <br />
**1.** Single image selection <br />
**2.** Multiple image selection <br />
**3.** Live camera image detection <br />

<img src="https://github.com/rahulshah456/RetailPulseAssignment/blob/master/screenshots/2.png" /> <img src="https://github.com/rahulshah456/RetailPulseAssignment/blob/master/screenshots/3.png" />
<img src="https://github.com/rahulshah456/RetailPulseAssignment/blob/master/screenshots/4.png" /> 

# Guidelines / Particulars
- The input size for the model is 300*300 Resize the image to the above dimentions and normalize it by dividing each pixel by 255.
- The model output is an array of 16 numbers. Something like this:
```ruby
[-3.042556345462799072e-04, 4.295762255787849426e-02, 3.900352120399475098e-01, -3.883016854524612427e-02,
-4.464706480503082275e-01, -2.026288211345672607e-01, -6.759560201317071915e-04, 6.938941031694412231e-02, 
3.022153675556182861e-01, 4.211977422237396240e-01, -2.605067193508148193e-01, -1.835217922925949097e-01, 
-6.375271826982498169e-02, -3.358602821826934814e-01, -3.338576853275299072e-01, -2.638743817806243896e-02]
```
- You will be provided with 32 pre calculated vectors(outputs) along with the respective labels [0, 1, 2]
- You need to compare the model output with these  32 vectors and assign a label based on the minimum euclidean distance.


# Files
- Model : [Tensorflow Lite Model](https://github.com/rahulshah456/RetailPulseAssignment/blob/master/model/rps_model.tflite)
- Labels : [TSV File](https://github.com/rahulshah456/RetailPulseAssignment/blob/master/app/src/main/assets/rps_labels.tsv)
- Output Vectors : [TSV File](https://github.com/rahulshah456/RetailPulseAssignment/blob/master/app/src/main/assets/rps_vecs.tsv)
- RPS Test Images : [Image Sets](https://github.com/rahulshah456/RockPaperScissors/tree/master/rps_test_sets)
