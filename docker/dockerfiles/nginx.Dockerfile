FROM fabiocicerchia/nginx-lua:1.27.0-alpine

ENV EEARLY_KEYCLOAK_PROXY_HOST_HEADER=false

COPY docker/nginx/templates/default.conf.template /etc/nginx/templates/default.conf.template

COPY docker/nginx/nginx.conf /etc/nginx/nginx.conf
