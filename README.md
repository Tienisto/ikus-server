# Welcome at OVGU
The server for the Welcome at OVGU app written in Vue and Spring

## Getting Started

Let's run this server on your (local) machine.

1. Install Java 11 and Node (12 or greater)
2. Build the vue frontend
    1. `cd <project root>/src/main/vue`
    2. `npm i`
    3. `npm run build`
3. Build the spring server, it will use the compiled files by vue
    1. `cd <project root>`
    2. `./gradle build -x test` or `gradlew.bat build -x test`
4. Run the .jar
    1. `cd <project root>/build/libs`
    2. `java -jar ikus-0.9.0.jar --db.url="the url to the db"`
    
## Build the Docker Image

1. Build the vue frontent (see step 2 of [Getting Started](#getting-started))
2. `./gradle bootBuildImage` or `gradlew.bat bootBuildImage`

One-Liner:

`cd <project root>/src/main/vue && npm i && npm run build && cd <project root> && ./gradle bootBuildImage`
    
## Environment Variables

Rename if you set it via (docker) environment variables. E.g. `storage.path` will be `STORAGE_PATH`.

Otherwise, leave the keys as is: `java -jar ikus-0.9.0.jar --storage.path="/my/path"`

Key|Description|Default
---|---|---
`storage.path`|Path to the persistent storage|`(invalid path)`
`db.url`|URL to PostgreSQL|`postgresql://localhost:5432/ikus`
`db.user`|User Name for PostgreSQL|`ikususer`
`db.password`|Password for PostgreSQL|`123456`
`admin.password`|Password of the admin account|`123`
`jwt.website`|JWT private key for server-website communication|`the jwt private key which is longer than 255 bits`
`jwt.app`|JWT private key for server-app communication|`the jwt private key which is longer than 255 bits`
