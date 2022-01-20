FROM nginx:latest
RUN mkdir -p /var/logs
RUN mkdir -p /var/www
COPY default.conf /etc/nginx/conf.d/default.conf
COPY dist/ /var/www/
EXPOSE 8080
CMD ["nginx","-g","daemon off;"]

