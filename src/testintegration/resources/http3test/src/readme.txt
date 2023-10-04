openssl req -x509 -days 7300 -nodes -newkey rsa:2048 -outform PEM -subj "/CN=localhost" -config openssl.cnf -extensions v3.req -keyout ../localhost.key -out ../localhost.crt
