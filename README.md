# file-download-service-lib   A library to download file from a remote url.

Goal Of the API - To design a resuable and extensible lib download file from http and ftp protocol.
What is extensible ?
User should be able to support implementation of any protocol at run time.
sample -
http://www.w3.org/TR/PNG/iso_8859-1.txt
ftp://speedtest.tele2.net/10MB.zip

Pre-requisite -
This lib needs spring version 4.3 and later.

Design Pattern Used -
We are using SPI pattern to plugin implementation of any new protocol. For that we have defined service provider interface
IFileDownloadManagerProvider

How to implement a new protocol -
1) Implement interface IFileDownloadManagerProvider
2) create a file called com.agoda.dm.spi.IFileDownloadManagerProvider and add an entry - Fully qualified name of implementation
3) Keep this file in resources/META-INF/service directory and prepare a jar
4) Drop this jar in class path. Done


