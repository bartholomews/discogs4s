# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- `DatabaseApi.getLabel`
- `DatabaseApi.getLabelReleases`

## [0.1.2] - 2021-08-05
### Added
- All the endpoints for `UsersApi`
### Changed
- Migrated to sttp v3
- Multi-module projects setup (circe / play / core)
- Multi-client setup (for each auth flow)
- Improved types on `UserIdentity` and `UserProfile` (breaking changes)

## [0.1.1] - 2021-01-02
### Changed
- Fsclient `client` val in `DiscogsClient` is now accessible,
  so its signer can be used for *Personal access token* calls

## [0.1.0] - 2021-01-02
### Added
- Release script
### Changed
- Migrated to [`fsclient-circe v0.1.0`](https://github.com/bartholomews/fsclient)
### Fixed
- Project developer email in `build.sbt`

## [0.0.1] - 2020-05-23
### Added
- This is the first release of `discogs4s`.

[Unreleased]: https://github.com/bartholomews/discogs4s/compare/v0.1.2...HEAD
[0.1.2]: https://github.com/bartholomews/discogs4s/compare/v0.1.1...v0.1.2
[0.1.1]: https://github.com/bartholomews/discogs4s/compare/v0.1.0...v0.1.1
[0.1.0]: https://github.com/bartholomews/discogs4s/compare/v0.0.1...v0.1.0
[0.0.1]: https://github.com/bartholomews/discogs4s/releases/tag/v0.0.1
