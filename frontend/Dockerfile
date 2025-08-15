# Build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install --legacy-peer-deps
COPY . .
# Inject REACT_APP_API_URL at build time if needed
ARG REACT_APP_API_URL
ENV REACT_APP_API_URL=$REACT_APP_API_URL
RUN npm run build

# Serve stage
FROM node:20-alpine
WORKDIR /app
COPY --from=build /app/build ./build
RUN npm install -g serve && \
    adduser -D appuser && \
    chown -R appuser:appuser /app
USER appuser
EXPOSE 3001
CMD ["serve", "-s", "build", "-l", "3001"]