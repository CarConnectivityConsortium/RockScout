#!/bin/sh

CN='CN=Developer'
DEVID='<your developer ID here>'

CERTGENPATH='<path to the certificate generator>/certificategenerator-linux-64.jar'
KEYSTOREPATH='<path to your keystore>/keystore.jks'
KEYSTOREPASS='<your keystore password>'
CERTXMLPATH='certificatexml/xml.xml'
BUILDPATH='../builds'
APPPATH='app/build/outputs/apk'

mkdir -p "$BUILDPATH"

rm -f "$BUILDPATH"/app-tmp.apk "$BUILDPATH"/app-devcert.apk

cp app/build/outputs/apk/app-release-unsigned.apk "$BUILDPATH"/


echo $KEYSTOREPASS | jarsigner -verbose -keystore "$KEYSTOREPATH" "$BUILDPATH"/app-release-unsigned.apk ccc

java -jar "$CERTGENPATH" generate-apk  "$BUILDPATH"/app-release-unsigned.apk "$CERTXMLPATH" "$CN" "$BUILDPATH"/app-tmp.apk "$DEVID"
echo  $KEYSTOREPASS | jarsigner -verbose -keystore "$KEYSTOREPATH" "$BUILDPATH"/app-tmp.apk ccc

java -jar "$CERTGENPATH" generate-apk "$BUILDPATH"/app-tmp.apk "$CERTXMLPATH" "$CN" "$BUILDPATH"/app-devcert.apk "$DEVID"
echo  $KEYSTOREPASS | jarsigner -verbose -keystore "$KEYSTOREPATH" "$BUILDPATH"/app-devcert.apk ccc

