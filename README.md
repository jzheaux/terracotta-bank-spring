# Terracotta

![Terracotta Logo](https://github.com/jzheaux/terracotta-bank-spring/blob/master/terracotta.png "Thanks to https://logomakr.com for the donation!")

A darned-vulnerable Spring-based Java web application - For educating on and practicing secure Java coding techniques.

## Running the Application

Terracotta can be run from the command-line like other Spring Boot applications:

```bash
./gradlew bootRun
```

And then simply browse to `http://localhost:8080`.

I would tell you a login, but I suspect you'll find a number of ways to get in on your own.

There are a number of ways to benefit from this application.

## Pen Testing

Like other intentionally-vulnerable applications, like DWVA, it is a candidate for pen-testing tools. Since Terracotta is a simple application, it's pretty easy to point ZAP, sqlmap, or other pentesting tools at it to learn more about pen testing.

As a hint for how to log in, consider pointing a pen testing tool at the login page to see what kinds of vulnerabilities it reveals.

## Mitigation

Terracotta is built with mitigation in mind.

### Unit Tests

First, Terracotta is equipped with dozens of unit tests intended to verify security best practices.

And, since Terracotta is terribly insecure, these unit tests in Terracotta fail by default.

The unit tests are a way to approach learning about secure coding in an exercise-based way. Take a look at a unit test and try to make it pass, making Terracotta more secure through your efforts.

Most of the tests are in `src/test/java`. But, some of the unit tests are JMH benchmarks, which are  n`src/jmh/java`.
 
### Commit Tracks

There are a number of permanent branches in the repo. These branches are intended to demonstrate a general aspect of secure coding, like Enumeration defense. Each commit on the branch makes the application a bit more secure.

The commit tracks are something of an answer key to the exercises, and are another way to learn about writing secure code.

This is by design as each of the tests is trying to confirm some security best practice. And since Terracotta has none of these by default, most of the tests fail out of the box.

## Pluralsight

The project is set up to align with corresponding courses on Pluralsight.

Each course has a main branch that stands as a basis for the course.

Off of this main branch are several of the aforementioned commit tracks.

In addition to the benefits these commit tracks provide, they help with the Pluralsight coursework in the following three ways:

- First, Terracotta can continue to evolve independently from the course branch, while released coursework can be based on a consistent snapshot
- Second, those following along in a demo can start at any relevant point by jumping to that commit
- Third, branches demonstrating different security features can be ad-hoc merged together to experiment with their composition

### Securing Java Web Applications Through Authentication

The base branch for this course is `authentication`.
All demo branches are based on this branch.

The demo branches for this course are:

* *`authentication-enumeration`* - For the family of demos related to mitigating enumeration attacks

* *`authentication-bruteforce`* - For the family of demos related to mitigating brute-force attacks

* *`authentication-tls`* - For the TLS demo in "Mitigating Plaintext Vulnerabilties with TLS"
* *`authentication-oauth2`* - For the Spring Security OAuth2 demo in "Mitigating Plaintext Vulnerabilities with Tokens"
* *`authentication-saml`* - For the SAML demo in "Mitigating Plaintext Vulnerabilities through Federation"

* *`authentication-password-complexity`* - For the demos related to supporting complex passwords
* *`authentication-password-storage`* - For the demos related to strong password storage techniques
* *`authentication-password-update`* - For the demos related to secure forgot and change password flows
* *`authentication-password-upgrade-plaintext`* - For the demos related to upgrading plaintext passwords to hashed ones
* *`authentication-password-upgrade-hashed`* - For the demos related to upgrading hashed passwords to stronger hashes
* *`authentication-logging`* - For the demos related to introducing security logging

### Securing Java Web Appilcation Data

Coming soon!
