# wo-web-app-server
Wo is A simple, light-weight, high available, high reliable, asynchronous web application server similiar to Tomcat,Jetty ect. implemented 
by netty and integrated with Springboot .

## Figures

- [x] Easy Use
- [x] Light Weight
- [x] User Transparent
- [x] Code Non-invasion
- [x] Asynchronous Handle Request
- [x] Fast Response
- [x] High Available
- [x] High Reliable
- [x] Simple
- [x] Gapless Integrated with Springboot
- [x] Netty Based
- [x] More

## Usage:

1. Start Wo web application server  ./build/wo-server-0.0.1-SNAPSHOT.jar in command line as below:

   ```shell
   java -jar wo-server-0.0.1-SNAPSHOT.jar [--option]
   ```

   [--option]:

   --port: wo server port,default *8080*;

   --context.path: your application class path ending with path delimiter based on your operation system,i.e. "\" for Windows,"/" for linux or unix.default *<wo-server-0.0.1-SNAPSHOT.jar's dir>/woWebApp/classes*/

   