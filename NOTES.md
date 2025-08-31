# Notes

## Json flattener library

https://github.com/wnameless/json-flattener

Flattening:
```java
Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);
System.out.println(flattenJson);
```

Unflatten:
```java
String nestedJson = JsonUnflattener.unflatten(jsonStr);
System.out.println(nestedJson);
```

## Publish a java library to github packages

https://www.baeldung.com/maven-publish-artifacts-github-packages

Publish with `mvn deploy`

Install locally by configuring the client project pom.xml
```xml
<repository>
    <id>github</id>
    <name>etrandafir93</name>
    <url>https://maven.pkg.github.com/etrandafir93/*</url>
</repository>

...

<dependency>
    <groupId>io.github.etrandafir93</groupId>
    <artifactId>utilitest</artifactId>
    <version>1.0.0-SNAPOSHOT</version>
    <scope>test</scope>
</dependency>
```

## Using env variables @ maven settings

https://stackoverflow.com/questions/31251259/how-to-pass-maven-settings-via-environment-vars

Use these constructs:
```xml
<servers>
    <server>
      <id>deploymentRepo</id>
      <username>${env.SERVER_USERNAME}</username>
      <password>${env.SERVER_PASSWORD}</password>
    </server>
</servers>
```

One can pass an ad-hoc `settings.xml` file using the `-s` option: `mvn deploy -s $GITHUB_WORKSPACE/settings.xml`

## Using env vars @ github actions

https://graphite.dev/guides/github-actions-env-variables

Secrets as env vars 👇

```yaml
steps:
  - name: Use secrets
    env:
      SENSITIVE_VAR: ${{ secrets.SECRET_NAME }}
    run: ./my_login_script $SENSITIVE_VAR
```

## Bump maven project version

https://readmedium.com/how-to-increment-versions-for-the-maven-build-java-project-a7596cc501c2

relies on these plugins
* build-helper-maven-plugin
* versions-maven-plugin

