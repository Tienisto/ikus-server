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

# build spring
WORKDIR /build
ADD . ./
RUN sh ./gradlew assemble
RUN mv build/libs/ikus-*.jar /server.jar
RUN rm -r /build

# deploy
EXPOSE 8080
ENTRYPOINT ["java","-jar","/server.jar"]
