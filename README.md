# Cloud Foundry Route Service Example

This project is an example of a [Cloud Foundry Route Service][r] written with [Spring Boot][b].  This application does the following to each request:

1. Intercepts an incoming request
2. Looks for a header `x-auth-token` containing 3 strings separated by colons (e.g. 3:10:YUMA)
3. Takes this data and creates a RSA 512 signed JWT token
4. Adds this JWT token to the onward HTTP Request Header wuth the attribute name `x-auth-user`
5. Forwards the request to the destination URL

## Requirements
### Java, Gradle
The application is written in Java 8 and packaged as a self executable JAR file. This enables it to run anywhere that Java is available.

## Deployment
_The following instructions assume that you have [installed the `cf` command line tool][i]._

In order to automate the deployment process as much as possible, the project contains a Cloud Foundry [manifest.yml][y].  To build the JAR and deploy it to PCF run the following commands:

```bash
$ ./gradlew clean check assemble
$ cf push
```

Next, create a user provided service on PCF that contains the route service configuration information.  To do this, run the following command, substituting the address that the route service is listening on:

```bash
$ cf create-user-provided-service test-route-service -r https://<ROUTE-SERVICE-ADDRESS>
```

The next step assumes that you have an application already running that you'd like to bind this route service to.  To do this, run the following command, substituting the domain and hostname bound to that application:

```bash
$ cf bind-route-service <APPLICATION-DOMAIN> test-route-service --hostname <APPLICATION-HOST>
```

In order to view the interception of the requests, you will need to stream the logs of the route service.  To do this, run the following command:

```bash
$ cf logs route-service
```

Finally, start making requests directly against your test (target) application which contain the HTTP header `x-auth-token` with a value of `"3:10:YUMA"` (or whatever 3 strings with : separators that you like).  The route will intercept these requests and the route service's logs should start logging entries that contain `x-auth-user` HTTP headers like this:

```text
2018-02-20T19:31:53.25+0100 [APP/PROC/WEB/0] OUT 2018-02-20 19:31:53.259  INFO [pcf-route-service,fb2b3963d5d50b1b,7923ed92c6ab5ae9,true] 19 --- [nio-8080-exec-5] i.p.tokenservice.CatchAllController      : Outgoing Request: <GET https://http-header-logger.apps.pcf-t01-we.rabobank.nl/health,{host=[edge-router-service.apps.pcf-t01-we.rabobank.nl], user-agent=[curl/7.54.0], accept=[*/*], x-auth-user=[eyJraWQiOiJmNjZlMjZiMy1mYmQwLTRjMGUtODExMy1kMGM3ZjkxMzk3OGMiLCJhbGciOiJSUzUxMiJ9.eyJzaWViZWxDdXN0b21lclJlbGF0aW9uSWQiOiIwMDAwMDAwMTExMTExMTEiLCJzb3VyY2VzIjpbIlJBU1MiLCJUQSJdLCJhdXRoVXNlcklkIjoidXNlci1pZCIsImVkb0tsaWQiOiIxMCIsImF1dGhVc2VyTGV2ZWwiOiJCRU4iLCJzaWViZWxVc2VyUmVsYXRpb25JZCI6IjAwMDAwMDAxMTExMTExMSIsImVkb0FncmVlbWVudElkIjoiMDAwMDAxMTQzIiwiZXhwIjoxNTE5MTUxNjkzLCJhdXRoVGlja2V0IjoiMjNmYWRmMjMwOWFvaWlqYXNzZWdnIiwiaWF0IjoxNTE5MTUxMzkzLCJhdXRoVXNlclR5cGUiOiJDVVNUT01FUiIsImVkb1VzZXJJZCI6IkNPT0wifQ.QsBBUtYyeEcyTqfYjqTm072dzVzDyjNza-u4ZmvUTX4BUFh1eyfVDqSY3e5swmlLtYXnwOLdwA_Zn8HavWWH5NVdghlChdZVI5Z7Pw5j6NOqx20rRl_THhtJOLeNLsUIdsgXz_fc3IS8jKMvWpL_BKPrjJQ-OlFLuLEM4Ogz90x9LEAgWMokv2ojAddxS-bSxmbEpnuTevLNvJ43Y94Xr2PCb1MfYczwJM3Y7j_Jb--dLiYnBnNQgdRlgCmzqxdUfAju-osvPLOIMQq8LkwxjC3PMDJqJZGqY9rRpV_X4Tsbpg3V2dhKuBS32TO8CUta85LLxa8ezVx9zdfJKKMVJw]}>
```
Unfortunately I haven't included the private key or the public JKS keys I used in this repo, but feel free to generate your own. Take a look in the code for the filenames expected. [Use JWT.io for guidance.][z]

## Extra Credit

This application also demonstrates how to configure JMX so that you can monitor an app while it is running on CloudFoundry.

The process is simple but assumes that your PCF instance allows SSH. Pivotal Web Services and PCF-Dev do allow SSH.

1. Add an environment property to the manifest to ask the Java buildpack to make the necessary configuration changes when building the droplet. You can do this manually via the `cf cli` or by adding this setting to your app's `manifest.yml` file:

    ````yml
    env:
        JBP_CONFIG_JMX: '{enabled: true}'
    ````

2. Open an SSH tunnel to port 5000 on localhost.

    ```bash
    cf ssh -N -T -L 5000:localhost:5000 wiretap-route-service
    ```

3. Start your monitoring tool. Java comes with Java Mission Control out of the box. You can start mission control with the following command:

    ```bash
    jmc
    ```

4. Add `localhost:5000` to Java Mission Control so that it appears in the list on the left, and then connect to your app's MBeans to start to see the metrics from the server.

## License
The project is released under version 2.0 of the [Apache License][a].

[a]: http://www.apache.org/licenses/LICENSE-2.0
[b]: http://projects.spring.io/spring-boot/
[c]: https://console.run.pivotal.io/register
[i]: http://docs.run.pivotal.io/devguide/installcf/install-go-cli.html
[j]: http://www.jetbrains.com/idea/
[r]: http://docs.cloudfoundry.org/services/route-services.html
[y]: manifest.yml
[z]: https://jwt.io
