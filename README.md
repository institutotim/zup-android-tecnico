Zup Técnico
==========================

# Lista de Build Types
* debug
* release

# Principais comandos gradlew (Está na pasta do projeto o arquivo **gradlew** ou **gradlew.bat**)
* ``assemble``
* ``clean``
* ``install``
* ``publishApk`` 

# Comandos para o uso no projeto
* ``./gradlew clean assemble<Flavor><BuildType>`` (Ex: ``./gradlew clean assembleUnicefRelease``):  Limpa o projeto e gera apk com o flavor e o build type na pasta **/build/outputs/apk/**. Se o somente o flavor for fornecido, ele gera todos os apks daquele flavor. Se nem o flavor for fornecido, ele gera todas as apks possíveis.
* ``./gradlew assemble<Flavor><BuildType> crashlyticsUploadDistribution<Flavor><BuildType>`` (Ex: ``./gradlew assembleUnicefRelease crashlyticsUploadDistributionUnicefRelease``): Gera o apk e envia a apk ao Crashlytics de acordo com o Flavor dado (cada flavor tem um pacote identificador que será relacionado ao projeto do Fabric.io) e convida os usuários definidos em FABRIC_EMAILS para usar o app.
* ``./gradlew publishApk<Flavor><BuildType>`` (Ex: ``./gradlew publishApkUnicefRelease``): Publica o apk na Play Store

# Dados de autenticação/configuração Crashlytics e keystore
* Arquivo ``gradle.properties``