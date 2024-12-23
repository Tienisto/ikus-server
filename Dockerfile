FROM node:22.12.0-slim as stage-vue
WORKDIR /vue-build

# setup pnpm
RUN npm i -g pnpm

# add vue dependencies
COPY src/main/vue/package.json .
COPY src/main/vue/pnpm-lock.yaml .
RUN pnpm i

# build vue
COPY src/main/vue/ .
RUN pnpm generate

FROM gradle:8.5-jdk17 as stage-spring
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
