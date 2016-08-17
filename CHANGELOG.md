# Change Log
All notable changes to this project will be documented in this file.

## [1.0.12]

### Changed

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