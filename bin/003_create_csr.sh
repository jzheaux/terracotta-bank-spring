###
# Generate a Certificate Signing Request (CSR).
#
# Once we generate this, then we can give the CSR to a CA which will
# use it to sign an SSL certficate
###

keytool -certreq -alias terracotta -keystore terracotta.jks -file terracotta.csr

