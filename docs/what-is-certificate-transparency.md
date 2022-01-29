# What is Certificate Transparency

> Certificate Transparency helps eliminate these flaws by providing an
open framework for monitoring and auditing SSL certificates in nearly
real time. Specifically, Certificate Transparency makes it possible to
detect SSL certificates that have been mistakenly issued by a
certificate authority or maliciously acquired from an otherwise
unimpeachable certificate authority. It also makes it possible to
identify certificate authorities that have gone rogue and are
maliciously issuing certificates. [https://www.certificate-transparency.org](https://www.certificate-transparency.org)

Certificate transparency works by having a network of publicly
accessible log servers that provide cryptographic evidence when a
certificate authority issues new certificates for any domain. These log
servers can then be monitored to look out for suspicious certificates as
well as audited to prove the logs are working as expected.

These log servers help achieve the three main goals:

- Make it hard to issue certificates without the domain owners knowledge
- Provide auditing and monitoring to spot mis-issued certificates
- Protect users from mis-issued certificates

When you submit a certificate to a log server, the server responds with
a signed certificate timestamp (SCT), which is a promise that the
certificate will be added to the logs within 24 hours (the maximum merge
delay). User agents, such as web browsers and mobile apps, use this SCT
to verify the validity of a domain.

For a more detailed overview of certificate transparency, please watch
the excellent video
[The Very Best of Certificate Transparency (2011-)](https://www.facebook.com/atscaleevents/videos/1904853043121124/)
from Networking @Scale 2017.

More details about how the verification works in the library can be
found at [Android Security: Certificate Transparency](https://medium.com/@appmattus/android-security-certificate-transparency-601c18157c44)
