# Terracotta Bank

A darned-vulnerable Spring-based Java web application - For educating on and practicing secure Java coding techniques

The project is set up to align with corresponding courses on Pluralsight.

Each course has a main branch that stands as a basis for the course.
From that branch are several other branches, generally one for each demonstration or family of demonstrations.

On each branch, are a sequence of commits that track the progress of each demonstration.

This approach allows for two things:

- First, Terracotta Bank can continue to evolve while released coursework can be based on a consistent snapshot
- Second, those following along in a demo can start at any relevant point by jumping to that commit
- Third, branches demonstrating different security features can be ad-hoc merged together to experiment with their composition

### Securing Java Web Applications Through Authentication


The base branch for this course is `authentication-authorization-and-audit`.
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

* *`authentication-audit`* - TBD for demos related to authorization vulnerabilities

