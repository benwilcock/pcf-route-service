# Cloud Foundry Route Service Example

This project is an example of a [Cloud Foundry Route Service][r] written with [Spring Boot][b].  This application does the following to each request:

1. Intercepts an incoming request
2. Logs information about that incoming request
3. Transforms the incoming request to an outgoing request
4. Logs information about that outgoing request
5. Forwards the request and response

## Requirements
### Java, Gradle
The application is written in Java 8 and packaged as a self executable JAR file. This enables it to run anywhere that Java is available.

## Deployment
_The following instructions assume that you have [installed the `cf` command line tool][i]._

In order to automate the deployment process as much as possible, the project contains a Cloud Foundry [manifest.yml][y].  To deploy run the following commands:

```bash
$ ./gradlew clean assemble
$ cf push
```

Next, create a user provided service that contains the route service configuration information.  To do this, run the following command, substituting the address that the route service is listening on:

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

Finally, start making requests against your test application.  The route service's logs should start returning results that look similar to the following:

```text
INFO [route-service,cea238f04e523fa7,ab82afdd7270d4d6,true] 14 --- [nio-8080-exec-2] o.c.example.RequestLoggingInterceptor    : REQUEST -> URI: https://cover-service.cfapps.pez.pivotal.io/covers METHOD: GET BODY: []
INFO [route-service,cea238f04e523fa7,fd480242bb272149,true] 14 --- [nio-8080-exec-2] o.c.example.RequestLoggingInterceptor    : RESPONSE -> S.CODE: 200 S.TEXT: OK BODY: No Cover, Auto Cover, Home Cover, Holiday Cover, Pet Cover, Duvet Cover
```



## License
The project is released under version 2.0 of the [Apache License][a].


[a]: http://www.apache.org/licenses/LICENSE-2.0
[b]: http://projects.spring.io/spring-boot/
[c]: https://console.run.pivotal.io/register
[i]: http://docs.run.pivotal.io/devguide/installcf/install-go-cli.html
[j]: http://www.jetbrains.com/idea/
[r]: http://docs.cloudfoundry.org/services/route-services.html
[y]: manifest.yml
