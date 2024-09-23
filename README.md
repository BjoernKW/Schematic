# Schematic

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.bjoernkw/schematic/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.bjoernkw/schematic)

<img src="https://github.com/BjoernKW/Schematic/raw/main/src/main/resources/static/images/logo.png" alt="logo" width="240">

**Schematic** is a simple database management UI for Spring Boot.

## Getting Started

To use **Schematic**, you need to add the following Maven dependency to your project's `pom.xml`:

```xml
<dependency>
    <groupId>com.bjoernkw</groupId>
    <artifactId>schematic</artifactId>
    <version>0.2.3</version>
</dependency>
```

If you're using Spring Boot 2.7.x and Java 11 you can add this version of **Schematic** to your project's `pom.xml`:

```xml
<dependency>
    <groupId>com.bjoernkw</groupId>
    <artifactId>schematic</artifactId>
    <version>0.2.3.jre11</version>
</dependency>
```

After that, simply restart your Spring Boot application. **Schematic** will be available under http://localhost:8080/schematic/tables
and show the database tables for the database connection configured for your application.

## Configuration

In case you need to customise the root path of your application (the default is `/`) or the path
Schematic will run under (the default is `/schematic`), respectively, you can do so  by adding the
following properties to your `application.yml` (or `application.properties`) file:

```
schematic:
  path: custom-path-for-schematic
  root-path: /MyApplication
```

### Screenshots

![Schematic-screenshot-1.png](documentation/static-resources/Schematic-screenshot-1.png)
![Schematic-screenshot-2.png](documentation/static-resources/Schematic-screenshot-2.png)
![Schematic-screenshot-3.png](documentation/static-resources/Schematic-screenshot-3.png)

### Gitpod environment
[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/BjoernKW/Schematic)

### Prerequisites

* [Java 17](https://openjdk.org/projects/jdk/17/) / [Java 11](https://openjdk.org/projects/jdk/11/)
* [Maven](https://maven.apache.org/)
* a [Spring Boot](https://spring.io/projects/spring-boot/) application to install **Schematic** into

## Built With

* [Spring Boot](https://spring.io/projects/spring-boot/)
* [Maven](https://maven.apache.org/)
* [Thymeleaf](https://www.thymeleaf.org/)
* [Bootstrap](https://getbootstrap.com/)
* [Font Awesome](https://fontawesome.com/)
* [Mermaid](https://mermaid.js.org/)

## License

[MIT License](https://opensource.org/licenses/MIT)

## Authors

* **[Björn Wilmsmann](https://bjoernkw.com)**
