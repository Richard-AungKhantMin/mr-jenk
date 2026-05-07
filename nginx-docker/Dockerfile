FROM nginx:alpine

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Copy SSL certificates
COPY keystore/ssl/server.crt /etc/nginx/ssl/server.crt
COPY keystore/ssl/server.key /etc/nginx/ssl/server.key

# Create SSL directory if it doesn't exist
RUN mkdir -p /etc/nginx/ssl

# Verify nginx configuration syntax
RUN nginx -t

# Expose ports
EXPOSE 80 443

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost/health || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
