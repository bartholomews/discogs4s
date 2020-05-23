[![Build Status](https://travis-ci.org/bartholomews/discogs4s.svg?branch=master)](https://travis-ci.org/bartholomews/discogs4s)
[![codecov](https://codecov.io/gh/bartholomews/discogs4s/branch/master/graph/badge.svg)](https://codecov.io/gh/bartholomews/discogs4s)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

# discogs4s
Early stage *Discogs* client with the *Typelevel* stack

### CI/CD Pipeline

This project is using [sbt-ci-release](https://github.com/olafurpg/sbt-ci-release) plugin:
 - Every push to master will trigger a snapshot release.  
 - In order to trigger a regular release you need to push a tag:
 
    ```bash
    ./scripts/release.sh v1.0.0
    ```
 
 - If for some reason you need to replace an older version (e.g. the release stage failed):
 
    ```bash
    TAG=v1.0.0
    git push --delete origin ${TAG} && git tag --delete ${TAG} \
    && ./scripts/release.sh ${TAG}
    ```