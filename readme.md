sg-cdb
========================================================================


Overview
========================================================================

sg-cdb is a pure Java version of [D.J. Bernstein](http://cr.yp.to/)'s 
constant database (cdb) package.  Its API is the same as the one used in 
the original cdb library.  

Files created with sg-cdb can be read with the cdb library and
vice versa.  My goal was to provide a seamless way of porting cdb code
to a Java environment without the need for a JNI library.

More cdb information can be found at D.J. Bernstein's home page:
[http://cr.yp.to/cdb.html](http://cr.yp.to/cdb.html). Please do **not** send sg-cdb questions to
Mr. Bernstein.  He is not the maintainer of this implementation. A nice (with pictures) description
of the format exists on [UnixUser.org](http://www.unixuser.org/~euske/doc/cdbinternals/index.html).

See the [sg-cdb product page](http://www.strangeGizmo.com/products/sg-cdb/) 
on the strangeGizmo.com site for more information on the basis of this fork.

This fork improves on the original by using Memory mapped NIO.

Continuous integration builds are performed by [Travis-CI](https://travis-ci.org/duckAsteroid/sg-cdb) [![Build Status](https://travis-ci.org/duckAsteroid/sg-cdb.svg?branch=master)](https://travis-ci.org/duckAsteroid/sg-cdb)

Software metrics are tracked via [SonarQube](https://sonarcloud.io/dashboard?id=sg-cdb) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=com.asteroid.duck%3Asg-cdb&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.asteroid.duck%3Asg-cdb)

Security vulnerabilities via [Snyk](https://snyk.io/) [![Known Vulnerabilities](https://snyk.io/test/github/duckAsteroid/sg-cdb/badge.svg?targetFile=build.gradle)](https://snyk.io/test/github/duckAsteroid/sg-cdb?targetFile=build.gradle)

Released JAR files are available via [![](https://jitpack.io/v/duckAsteroid/sg-cdb.svg)](https://jitpack.io/#duckAsteroid/sg-cdb)

To add this to your `build.gradle`:

Add it in your root build.gradle at the end of repositories:
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Step 2. Add the dependency
```groovy
dependencies {
        implementation 'com.github.duckAsteroid:sg-cdb:v1.0.6'
}
```

Change List
========================================================================
1.0.6
-----
-   Build tools and release chain changes only

1.0.5
-----
-   Upgraded to use Gradle build tools.
-   Added a main method/class that simply redirects between the 
    `dump`/`make`/`get` tools.
-   Added metadata to make JAR executable
-   Added Travis-CI build script

1.0.4
-----

-   Fixed cdb.dump to avoid problems with charset conversion issues on
    some platforms (fixed by Ito Kazumitsu).
-   Fixed cdb.dump so that it outputs \\n as the record terminator on
    Windows platforms, which would otherwise output \\r\\n (fixed by Ito
    Kazumitsu).

1.0.3
-----

-   Fixed sign bit problem with binary keys (fixed by Eric Kampf).
-   Fixed findNext to correctly deal with multiple keys mapping to the
    same value (fixed by Eric Kampf).

1.0.2
-----

-   First public release.
