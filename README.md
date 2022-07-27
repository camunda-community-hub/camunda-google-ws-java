[![Community Extension](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)
[![](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)

# Camunda Google Workspace

This project is designed to simplify GDrive and GMail integration in Camunda (or any other project).

# How to use this library

If you want to modify the default Google configs (scope, name, token directory, etc.), you can do it that way :

```java
GoogleWsConfig googleWsConfig = new GoogleWsConfig();
googleWsConfig.setApplicationName("test");
...
GoogleAuthUtils.configure(googleWsConfig);
```

This will impact DriveUtils and GmailUtils.

## GDrive

```java
String driveId = DriveUtils.storeInDrive(new File("C:\\Users\\ChristopheDame\\workspace\\camunda-google-ws-java\\src\\test\\resources\\mockument.pdf"));

DriveUtils.getFromDrive(driveId, "myLocaleFileName")    
```
## GMail

```java
String htmlBody = MailBuilderUtils.buildMailBody("mailTemplate", Map.of("key", "value"), Locale.ENGLISH);
        Mail mail = new Mail.Builder().from("from").to("to1", "to2").subject("subject").body(htmlBody).attachments(new File("file.pdf"), new File("file.jpg")).build();
GmailUtils.sendEmail(mail); 
```

The MailBuilderUtils.buildMailBody is using Thymeleaf. It expects to find a mailTemplate-en.html file resources/templates. 

```html
<!DOCTYPE html>
<html lang="en" xmlns="http://www.w3.org/1999/xhtml" xmlns:th="http://www.thymeleaf.org">
  <body>
    <h4>Hi <span th:text="${key}"></span></h4>
    <p>Please enjoy the content of this message.<p/>
  </body>
</html>
```

The templates expressions are evaluated with FEEL so you can use expressions like ${now()+duration("P3M")}

This can be changed by modifying the ThymeleafConfig encoding, prefix, expression language, date formatting patterns, etc.

# use it in your project
You can import it to your maven or gradle project as a dependency

```xml
<dependency>
	<groupId>io.camunda</groupId>
	<artifactId>camunda-google-ws-java</artifactId>
	<version>1.1.0</version>
</dependency>
```

# Note
This is still a draft and PRs are welcome.
