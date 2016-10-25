# Change Log
All notable changes to this project will be documented in this file.

## [10.0.14-SNAPSHOT]

- `include-extensions` was consulted too early, leaving the matched files out of the manifest
- Basic Authentication only worked for manifest, not for the artifacts

## [1.0.13] - 2016-09-12

- Support for fully customizable update UI, see (https://github.com/edvin/fxlauncher-custom-ui) 
- Basic Authentication support for manifest url (via https://user:pass@host/path)
- Added `--include-extensions` as a comma separated list of filename extensions to include of other resources from the build dir. By default it always includes `jar,war`.
- Fixed bug: If updating from a manifest with no timestamp (pre 1.0.11), new version was considered older, so no upgrade was performed

## [1.0.12]

### Changed

- Added `include-extensions` parameter to CreateManaifest. By default only `jar` and `war` files are included, add more extensions via this comma separated list.
- Added --accept-downgrade=<true|false> parameter to CreateManifest. Default is to not accept downgrades (server version is older than local version)
- Artifacts in subfolders gets correct path delimiter in app manifest for Windows

## [1.0.11]

### Changed

- Progress window is now closed after primaryStage is shown instead of right before app.start() is called

## [1.0.10] - 2016-05-07

### Changed

- Add optional `--cache-dir` program parameter `cacheDir` and manifest entry (https://github.com/edvin/fxlauncher/issues/9)
- Add / if missing from base url (https://github.com/edvin/fxlauncher/issues/6)

## [1.0.9] - 2016-03-14

### Added

- App manifest location can be given as command line parameter (https://github.com/edvin/fxlauncher/issues/3)

## [1.0.8] - 2016-03-02

### Added

- Support for manifest configurable parameters (https://github.com/edvin/fxlauncher/issues/2)

## [1.0.7] - 2016-02-20

### Added
- Support for platform specific resources

### Changed
- Parameters are now passed to the Application instance (https://github.com/edvin/fxlauncher/issues/1)

## [1.0.6] - 2016-02-10
- First feature complete release