<div id="top">

<!-- HEADER STYLE: CONSOLE -->
<div align="center">

```console
██████ ██████ ██████  ████  ██████ ██████ ████   ████   ██████  ████  ██      ████  ██  ██ ████   
██  ██ ██       ██   ██     ██  ██   ██   ██  ██ ██  ██ ██   █ ██     ██     ██  ██ ██  ██ ██  ██ 
██████ ████     ██   ██ ███ ██████   ██   ██  ██ ██  ██ ██████ ██     ██     ██  ██ ██  ██ ██  ██ 
██     ██       ██   ██  ██ ██ ██    ██   ██  ██ ██  ██ ██   █ ██     ██     ██  ██ ██  ██ ██  ██ 
██     ██       ██    ████  ██  ██ ██████ ████   ████   ██████  ████  ██████  ████   ████  ████   


```

</div>

<em>Built with the tools and technologies:</em>

<img src="https://img.shields.io/badge/Spring-000000.svg?style=flat&logo=Spring&logoColor=white" alt="Spring">
<img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat&logo=spring-boot&logoColor=white" >
<img src="https://img.shields.io/badge/Thymeleaf-%23005C0F.svg?style=flat&logo=Thymeleaf&logoColor=white" >
<img src="https://img.shields.io/badge/Chart%20js-FF6384?style=flat&logo=chartdotjs&logoColor=white" alt="Chartjs">
<img src="https://img.shields.io/badge/Bootstrap-563D7C?style=flat&logo=bootstrap&logoColor=white" >
<img src="https://img.shields.io/badge/JavaScript-F7DF1E.svg?style=flat&logo=JavaScript&logoColor=black" alt="JavaScript">
<img src="https://img.shields.io/badge/Apache%20Maven-C71A36.svg?style=flat&logo=Apache-Maven&logoColor=white">
<span>GridDB Cloud<span>

</div>
<br>

## Overview

A Personal Finance Tracker web with minimal features, just enough to be useful.

---

## Development

Update GridDB Cloud database connection in `application.properties`.

Run the application:

```sh
 .\mvnw spring-boot:run
```

Format source code:

```sh
.\mvnw spotless:apply
```

---

## Build

The application can be built using the following command:

```
mvnw clean package
```

Start your application with the following command - here with the profile `production`:

```
java -Dspring.profiles.active=production -jar ./target/pftgriddbcloud-0.0.1-SNAPSHOT.jar
```

Windows Powershell:

```
java "-Dspring.profiles.active=production" -jar ./target/pftgriddbcloud-0.0.1-SNAPSHOT.jar
```

---

## Features

|      | Component       | Details                              |
| :--- | :-------------- | :----------------------------------- |
| ⚙️  | **Architecture**  | <ul><li>Follows a **Technical** architecture. Each technical type has its own package like controller, domain, model, rest, service, repos</li><li>Utilizes **Spring Boot** for backend services</li><li>Frontend built with **Spring MVC**, **Thymeleaf** and **Bootstrap 5 CSS**</li></ul> |
| 🔩 | **Code Quality**  | <ul><li>Adheres to **Google Java Style Guide**</li><li>Uses **Spotless** for code formatting</li><li>Includes **error-handling-spring-boot-starter** for robust error handling</li></ul> |
| 🧩 | **Modularity**    | <ul><li>Organized into separate **modules** for frontend and backend</li><li>**Component-based** architecture for reusability</li></ul> |
| 🧪 | **Testing**       | <ul><li>Includes **unit tests** using **JUnit** and **Mockito** for backend services</li></ul> |
| 📦 | **Dependencies**  | <ul><li>Includes various **Maven** dependencies for backend</li><li>Key dependencies: **Spring Boot**, **Thymeleaf**, **Bootstrap 5 CSS**</li></ul> |

---

## Further readings

* [Maven docs](https://maven.apache.org/guides/index.html)  
* [Spring Boot reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)  
* [Spring Data JPA reference](https://docs.spring.io/spring-data/jpa/reference/jpa.html)
* [Thymeleaf docs](https://www.thymeleaf.org/documentation.html)  
* [Learn Spring Boot with Thymeleaf](https://www.wimdeblauwe.com/books/taming-thymeleaf/)  
* [Spring Boot skeleton project](https://bootify.io/next-steps/).  
