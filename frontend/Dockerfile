FROM nginx:alpine
# Clear default Nginx files to avoid serving the welcome page
RUN rm -rf /usr/share/nginx/html/vocalkord/*
# Copy React build files
COPY build/ /usr/share/nginx/html/vocalkord
# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80