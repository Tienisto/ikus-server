# Welcome to OVGU
The server for the Welcome to OVGU app written in Vue and Spring

## Getting Started

Let's run this server on your (local) machine.

1. Install Java 17 and Node (22 or greater)
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
    
## Environment Variables

Rename if you set it via (docker) environment variables. E.g. `storage.path` will be `STORAGE_PATH`.

Otherwise, leave the keys as is: `java -jar server.jar --storage.path="/my/path"`

| Key              | Description                                      | Default                                             |
|------------------|--------------------------------------------------|-----------------------------------------------------|
| `storage.path`   | Path to the persistent storage                   | `(invalid path)`                                    |
| `db.url`         | URL to PostgreSQL                                | `postgresql://localhost:5432/ikus`                  |
| `db.user`        | User Name for PostgreSQL                         | `ikususer`                                          |
| `db.password`    | Password for PostgreSQL                          | `123456`                                            |
| `admin.password` | Password of the admin account                    | `123`                                               |
| `jwt.website`    | JWT private key for server-website communication | `the jwt private key which is longer than 255 bits` |
| `jwt.app`        | JWT private key for server-app communication     | `the jwt private key which is longer than 255 bits` |
| `routes.dev`     | Allow dev routes                                 | `false`                                             |
