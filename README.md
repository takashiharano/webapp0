# About

webapp0 is Java Web Application Template.  
This template already implements common logic and patterns.  
It enables rapid web application development, and you just have to imitate it according to your purpose.

# Getting started

## Project files

1. Download ZIP and unzip.
2. Import to the Eclipse.
3. Rename the package name from `com.takashiharano.webapp0` to your own package name.
4. Grep and replace whole 'webapp0' in the source code with your own app name. (case-sensitive)
5. Modify the pom.xml to suite your app.
6. Once build with command `mvn clean package` so that download dependencies.
7. Right click on your app project > Maven > Update Project

## Properties

1. Create a folder.  
   Linux: `/home/<USER>/webapphome/<APPNAME>`  
   Windows: `C:/Users/<USER>/webapphome/<APPNAME>`
2. Copy the following files in `<Project>/webapphome/webapp0` to the folder you created.
   - app.properties
   - users.txt
   - userspw.txt

# Run

Right click on your app project > Run As > Run on Server  
You can login with the default user account `admin` and the password is the same as the username.  

# Development and debugging

1. Remove the sample screen definition `screen1`, `screen2`, `_template` from `menu.jsp`.
2. Delete .jsp and .js files of the above screen.
3. Copy `_template.jsp` and `_template.js` with your new screen id.
4. Replace all `_template` in those files with new screen id.
5. Add screen definition of the new screen in `menu.jsp`.
6. Write your code in the new .jsp and .js.

# WAR building

```sh
mvn clean package
```
# License

webapp0 is licensed under the **[MIT License](https://github.com/takashiharano/webapp0/blob/main/LICENSE)**.
