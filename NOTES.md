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

## GithubActions CI with maven release plugin

Prepare a release:

```sh
mvn -B release:prepare \
  -DreleaseVersion=${{ releaseVersion }} \
  -DdevelopmentVersion=${{ nextVersion }}
```

Deploy release:

```sh
mvn release:perform
```

Bypassing branch ruleset using deploy keys:
* gh support site --> https://github.com/orgs/community/discussions/25305#discussioncomment-10728028
* example github action --> https://github.com/sbellone/release-workflow-example/blob/main/.github/workflows/release.yml

In order to bypass branch rulesets in github actions:
1. Create a DEPLOY_KEY
  * This is a public/private key pair
2. Store the PRIVATE part as a secret
3. Add DEPLOY_KEY as a bypass item in the branch ruleset
4. Checkout the code in the gh action with the PRIVATE key:

```yaml
- name: Checkout code
  uses: actions/checkout@v4
  with:
    fetch-depth: 0
    ssh-key: ${{ secrets.GH_ACTION_DEPLOY_KEY }}
```

## Deploy to maven central

From chatgpt conversation --> https://chatgpt.com/c/6a0231ae-3c60-832f-8f62-ee7f738391d8

Add relevant dependencies to pom to generate javadocs, sign the artifact and publish to maven central:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>
...
    <build>
        <plugins>

            <!-- Java compiler -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.14.0</version>
            </plugin>

            <!-- Sources JAR -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-source-plugin</artifactId>
                <version>3.3.1</version>
                <executions>
                    <execution>
                        <id>attach-sources</id>
                        <goals>
                            <goal>jar</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- Javadocs JAR -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-javadoc-plugin</artifactId>
                <version>3.11.2</version>
                <executions>
                    <execution>
                        <id>attach-javadocs</id>
                        <goals>
                            <goal>jar</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- GPG signing -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-gpg-plugin</artifactId>
                <version>3.2.7</version>
                <executions>
                    <execution>
                        <id>sign-artifacts</id>
                        <phase>verify</phase>
                        <goals>
                            <goal>sign</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- Central publishing -->
            <plugin>
                <groupId>org.sonatype.central</groupId>
                <artifactId>central-publishing-maven-plugin</artifactId>
                <version>0.7.0</version>
                <extensions>true</extensions>
                <configuration>
                    <publishingServerId>central</publishingServerId>
                    <autoPublish>true</autoPublish>
                </configuration>
            </plugin>

        </plugins>
    </build>

</project>
```

Install any gpg tool
* Public key generation
* Gpg signature

Generate a gpg key `gpg --full-generate-key` with:
* (1) RSA and RSA
* 4096
* 0 = key does not expire

Extract the new key id: `gpg --list-secret-keys --keyid-format LONG`
* Format usually is `sec rsa4096/1234567890ABCDEF`
* The id is the `1234567890ABCDEF` part

Export the public key `gpg --armor --export 1234567890ABCDEF`
* Upload it to a public key server like https://keys.openpgp.org/

Create a user token in https://central.sonatype.com/
It yields:
* username
* password/token

Update the maven settings to add the maven central creds:
```xml
<servers>

    <server>
        <id>central</id>
        <username>YOUR_CENTRAL_USERNAME</username>
        <password>YOUR_CENTRAL_TOKEN</password>
    </server>

</servers>
```

Note that server#id must match the one specified in the pom's `<>central-publishing-maven-plugin</>` plugin config

Update the maven settings to add the gpg signature key data:
```xml
<profiles>
    <profile>
        <id>gpg</id>

        <properties>
            <gpg.keyname>1234567890ABCDEF</gpg.keyname>
            <gpg.passphrase>YOUR_GPG_PASSPHRASE</gpg.passphrase>
        </properties>
    </profile>
</profiles>

<activeProfiles>
    <activeProfile>gpg</activeProfile>
</activeProfiles>
```

Test it via `mvn clean verify`
It should generate these artifacts:
* .jar
* -sources.jar
* -javadoc.jar
* .asc signature files

Publish to maven central via `mvn clean deploy`

