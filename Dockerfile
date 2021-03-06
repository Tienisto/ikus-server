FROM node:14.16-alpine3.11 as stage-vue
WORKDIR /vue-build

# add vue dependencies
ADD src/main/vue/package.json .
ADD src/main/vue/package-lock.json .
RUN npm i

# build vue
ADD src/main/vue/ .
RUN npm run build

FROM gradle:jdk11 as stage-spring
WORKDIR /build

# add vue build
COPY --from=stage-vue /vue-build/dist/ /build/src/main/vue/dist/

# build spring
ADD . ./
RUN gradle assemble
RUN mv build/libs/*.jar /server.jar
RUN rm -r /build

# deploy
EXPOSE 8080
ENTRYPOINT ["java","-jar","/server.jar"]
