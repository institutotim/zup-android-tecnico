FROM java:8-jdk

ENV SDK_VERSION 24.4.1
ENV ANDROID_HOME /opt/android-sdk-linux
ENV PATH ${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools
ENV PATH ${PATH}:/opt/tools

RUN dpkg --add-architecture i386 && apt-get update && apt-get install -y --force-yes expect git wget libc6-i386 lib32stdc++6 lib32gcc1 lib32ncurses5 lib32z1 python curl

RUN cd /opt && wget --output-document=android-sdk.tgz --quiet \
      http://dl.google.com/android/android-sdk_r${SDK_VERSION}-linux.tgz \
      && tar xzf android-sdk.tgz && rm -f android-sdk.tgz \
      && chown -R root.root android-sdk-linux

COPY accept-license.sh /opt/accept-license.sh

RUN chmod +x /opt/accept-license.sh

RUN ["/opt/accept-license.sh", "android update sdk --all --force --no-ui --filter platform-tools,tools,build-tools-21,build-tools-21.0.1,build-tools-21.0.2,build-tools-21.1,build-tools-21.1.1,build-tools-21.1.2,build-tools-22,build-tools-22.0.1,build-tools-23.0.2,build-tools-23.0.3,build-tools-24,build-tools-24.0.0,android-21,android-22,android-23,android-24,addon-google_apis_x86-google-21,extra-android-support,extra-android-m2repository,extra-google-m2repository,extra-google-google_play_services,sys-img-armeabi-v7a-android-21"]

WORKDIR /tmp
