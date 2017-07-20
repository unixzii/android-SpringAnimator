# SpringAnimator
[![](https://travis-ci.org/unixzii/android-SpringAnimator.svg?branch=master)](https://travis-ci.org/unixzii/android-SpringAnimator)
<br/>A framer.js DHO and RK4 spring animation port for Android.

### Features
* Extends from `Animator`, providing a familiar API to use
* Provides **DHO** and **RK4** algorithm from Framer.js
* Bundled playground app, fine-tuning made easy

## Demo
Checkout the playground here: [playground.apk](https://github.com/unixzii/android-SpringAnimator/releases/download/0.1.0-alpha1/playground.apk)

<img src="https://github.com/unixzii/android-SpringAnimator/blob/master/art/screencast.gif?raw=true" alt="Screencast" height="500" />

## Requirements
SpringAnimator requires API 16 or higher.

## Download
Gradle:
```groovy
repositories {
    jcenter()
}

dependencies {
    compile 'me.cyandev:springanimator:0.1.0-alpha1'
}
```

Find out more releases [here](https://github.com/unixzii/android-SpringAnimator/releases). 

## Get Started
```java
DhoSpringAnimator animator = new DhoSpringAnimator();
animator.setStiffness(200);
animator.setDamping(10);
animator.setMass(1);
animator.setVelocity(0);
animator.addUpdateListener(new AbsSpringAnimator.AnimatorUpdateListener() {
    @Override
    public void onAnimationUpdate(AbsSpringAnimator animation) {
        // Do something cool here...
    }
});
animator.start();
```

Parameters are fully matched with Frame.js.

## Acknowledgement
Thanks [koenbok](https://github.com/koenbok)/[Framer](https://github.com/koenbok/Framer) for providing original algorithms.

Thanks [MartinRGB](https://github.com/MartinRGB) for designing user interface of the playground app and providing graphical resources.

## License
```
Copyright 2017 Cyandev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
