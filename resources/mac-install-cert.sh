#!/bin/sh

security add-trusted-cert -d -r trustRoot -k /Library/Keychains/System.keychain ca.cert.pem
