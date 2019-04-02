# wo-web-app-server
Wo is A simple, light-weight, high available, high reliable, asynchronous web application server similiar to Tomcat,Jetty ect. implemented 
by netty and integrated with Springboot .

## Features

- [x] Easy Use
- [x] Light Weight
- [x] User Transparent
- [x] Code Non-invasion
- [x] Asynchronous Handle Request
- [x] MVC Route
- [x] Fast Response
- [x] Hight Tonnage
- [x] High Available
- [x] High Reliable
- [x] Simple
- [x] Gapless Integrated with Springboot
- [x] Netty Based
- [x] More...

## Usage:

1. Start Wo web application server  *./build/wo-server-0.0.1-SNAPSHOT.jar* in command line as below:

   ```shell
   java -jar wo-server-0.0.1-SNAPSHOT.jar [--option]
   ```

   [--option]:

   ​	--port: wo server port,default *8080*;

   ​	--context.path: your application class path ending with path delimiter based on your operation 		

   ​		system,i.e. "\\" for Windows,"/" for linux or unix.

   ​		default *<wo-server-0.0.1-SNAPSHOT.jar's dir>/woWebApp/classes/*

2. To implement MVC route function, add the following annotations in your web interactive programs(similar with contorller in Spring MVC):

   - @Request：annotate a program interacting with web browser, function as @Controller in Spring MVC does.
   - @QueryMapping: mark down a method in @Request programs used as URI mapping working as Spring MVC @RequestMapping.
   - @QueryParam: a @QueryMapping method's parameter, used to bind web browser input value, similar to @RequestParam in Spring MVC.
   - @QueryVariable: annotate a URI variable in a @QueryMapping annotation, working as @RequestVariable in Spring MVC.  

   ## Dependencies

   Install all repositores in directory *./repository/* into your project repository, and add the following dependency in your project's pom:

   ```xml
   <dependency>
       <groupId>wo.app.server</groupId>
       <artifactId>annotation</artifactId>
       <version>1.0-SNAPSHOT</version>
   </dependency>
   ```

   

