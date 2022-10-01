# Github to bookmarks

This little webapp will generate a JSON bookmarks document from a GitHub user.

This is intended to be used with [bbt](https://github.com/BoD/bbt).

## How to use

https://<span></span>server/`AUTH_TOKEN`/`GITHUB_USER_NAME`

## Docker instructions

### Building and pushing the image to Docker Hub

```
docker image rm bodlulu/github-to-bookmarks:latest
DOCKER_USERNAME=<your docker hub login> DOCKER_PASSWORD=<your docker hub password> ./gradlew dockerPushImage
```

### Running the image

```
docker pull bodlulu/github-to-bookmarks
docker run -p <PORT TO LISTEN TO>:8080 bodlulu/github-to-bookmarks
```

## Licence

Copyright (C) 2021-present Benoit 'BoD' Lubek (BoD@JRAF.org)

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not,
see http://www.gnu.org/licenses/.
