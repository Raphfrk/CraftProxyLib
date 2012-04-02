CraftProxyLib
==========

Building
--------
SpoutProxyLib uses Maven for building.

You can build the project using `mvn clean install`.  This will install the project into your local Maven repository.

Using the Library
-------

### With Maven

If your software also uses Maven for dependency management, then you must add the following sections to your pom.xml file.


#### Repository Reference

    
      <repositories>
        <repository>
          <id>snaphot-repo</id>
          <url>https://raw.github.com/Raphfrk/maven-repo/master/snapshots</url>
        </repository>
      </repositories>
    

#### Project Dependency

      <dependencies>
        <dependency>
          <groupId>com.raphfrk</groupId>
          <artifactId>craftproxylib</artifactId>
          <version>1.0-SNAPSHOT</version>
          <scope>compile</scope>
        </dependency>
      </dependencies>

### Without Maven

You can generate the .jar file using the command `mvn clean package`.  This will create the jar file in the /target directory.  This file must be added to the class path when building your project.
