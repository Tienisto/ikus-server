FROM node:16.14.2-alpine3.15 as stage-vue
WORKDIR /vue-build

# add vue dependencies
COPY src/main/vue/package.json .
COPY src/main/vue/package-lock.json .
RUN npm i

# build vue
COPY src/main/vue/ .
RUN npm run build

FROM gradle:8.5-jdk17-alpine as stage-spring
WORKDIR /build

# add vue build
COPY --from=stage-vue /vue-build/dist/ /build/src/main/vue/dist/

# build spring
COPY . ./
RUN gradle bootJar
RUN mv build/libs/*.jar /server.jar
RUN rm -r /build

# deploy
EXPOSE 8080
ENTRYPOINT ["java","-jar","/server.jar"]
