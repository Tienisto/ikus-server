FROM node:14.18.3-alpine3.15 as stage-vue
WORKDIR /vue-build

# add vue dependencies
ADD src/main/vue/package.json .
ADD src/main/vue/package-lock.json .
RUN npm i

# build vue
ADD src/main/vue/ .
RUN npm run build

FROM gradle:7.4.1-jdk11-alpine as stage-spring
WORKDIR /build

# add vue build
COPY --from=stage-vue /vue-build/dist/ /build/src/main/vue/dist/

# build spring
ADD . ./
RUN gradle bootJar
RUN mv build/libs/*.jar /server.jar
RUN rm -r /build

# deploy
EXPOSE 8080
ENTRYPOINT ["java","-jar","/server.jar"]
