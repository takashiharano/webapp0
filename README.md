# About

webapp0 is Java Web Application Template.  
This template already implements common logic and patterns.  
It enables rapid web application development, and you just have to imitate it according to your purpose.

# Getting started

1. Download ZIP and unzip.
2. Import to the Eclipse.
3. Rename the package name from `com.takashiharano.webapp0` to your own package name.
4. Grep and replace whole 'webapp0' in the source code with your own app name. (case-sensitive)
5. Modify the pom.xml to suite your app.
6. Once build with command `mvn clean package` so that download dependencies.
7. Right click on your app project > Maven > Update Project

# Run

Right click on your app project > Run As > Run on Server

# WAR Building

```sh
mvn clean package
```
# License

webapp0 is licensed under the **[MIT License](https://github.com/takashiharano/webapp0/blob/main/LICENSE)**.
