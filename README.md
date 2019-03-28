# photoLoader
A java application to store photos

needs https://repository.apache.org/content/repositories/snapshots/org/apache/commons/commons-imaging/1.0-SNAPSHOT/commons-imaging-1.0-20180908.110935-119.jar

work in progress to :
- use config file (for inputDirectory, outputDirectory, log file path, MD5...)
- manage file extension
- better log management
- ...

#Release notes
v2019.03.28 :
Manage pictures in D:\Perso\photoIn
For each photo, calculate MD5 (will be the target filename) and determine photo date (with EXIF metadata or by default file lastmodification date)
Try to move the photo to D:\Perso\PhotoOut\[year]\[month]\[MD5].jpg
If the target file exists, the photo is move to D:\Perso\PhotoIn\error\ (it means there was twice the same photo into the source directory but with differents names)
