# What is ImageLoaderPro?
This library is base on [Android-Universal-Image-Loader](https://github.com/nostra13/Android-Universal-Image-Loader), in order to decrease code and save developer's time, it easier to use than orginal Universal-Image-Loade.

# Why ImageLoaderPro?
In the oringal Universal-Image-Loader project, there are many kinds of display method, developers use complicated flow and lots of code to display image, in addition, the usability of cache feature is not enough, so this is what ImageLoaderPro solve for.
  - Short and simple code
  - Process cache automatically
  - Support blur

# Usage
**1. Gradle dependency** (recommended)
  -  Add the following to your project level `build.gradle`:
 
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

  -  Add this to your app `build.gradle`:
      - [You can find universal-image-loader-1.9.5.jar here](https://github.com/nostra13/Android-Universal-Image-Loader)
 
```gradle
dependencies {
    compile 'com.android.support:support-annotations:25.1.1'
    compile 'com.github.SUKI-Huang:android_image_loader_pro:1.0.0'
    compile files('libs/universal-image-loader-1.9.5.jar')
}
```



**2. In your Java code**

  -  Sample display (with memory cache and fade effect):
```
    String url = "http://xx.xx.xx/xx.jpg";
    ImageLoaderPro.load(imageView, url);
```

  -  Advanced display:
```
String url = "http://xx.xx.xx/xx.jpg";
String cacheUri = "data/data/packageName/xx.jpg";
String defaultUri;
defaultUri = ImageLoaderPro.getDrawableUri(R.drawable.icon);
defaultUri = ImageLoaderPro.getAssetsUri("img/xx.jpg");
defaultUri = ImageLoaderPro.getFileUri("data/data/packageName/default.jpg");
ImageLoaderPro.load(
        imageView,
        url,
        new ImageLoaderPro.Options()
                //if load failed it will display image from default uri
                .setDefaultUri(defaultUri)
                //set local cache, if exist it will not request from internet
                .setCacheUri(cacheUri)
                //set local cache expired duration (millisecond)
                .setCacheExpiredDuration(6000)
                //set fade duration
                .setFade(500)
                //set blur (0-25)
                .setBlur(25)
);

```
 

## License


    Copyright (C) 2017 Suki Huang
    Copyright (C) 2011 The Android Open Source Project

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

