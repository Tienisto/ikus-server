FROM alpine:3.12
RUN apk add --update npm openjdk11

WORKDIR /build/src/main/vue

# add vue dependencies
ADD src/main/vue/package.json .
ADD src/main/vue/package-lock.json .
RUN npm i

# build vue
ADD src/main/vue/ .
RUN npm run build

WORKDIR /build

# build spring
ADD . ./
RUN sh ./gradlew assemble
RUN mv build/libs/ikus-*.jar /ikus.jar
RUN rm -r /build

WORKDIR /
ADD Procfile /Procfile
ADD start.sh /start.sh

EXPOSE 8080
