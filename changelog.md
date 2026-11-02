# Changelog

<!-- Maintained manually. Use feat:, fix:, perf:, refactor:, build: or docs: without scopes. Copy each commit subject verbatim into this file in the same commit. -->

## 2.1.0 - Minecraft 26.3 (Unreleased)

Fabric, Forge and NeoForge.

### Features

- feat: share client settings across video settings and mod menus
- feat: show beacon effect levels and countdowns on the HUD

### Optimizations

- refactor: organize beacon features and optimize perimeter rendering
- perf: reduce HUD countdown work and fit labels inside their icons

### Bug Fixes

- fix: display mod icons, descriptions and project links correctly
- fix: hide beacon perimeters while their beams are blocked
- fix: translate perimeter toggle notifications into English and Spanish
- fix: update resource pack metadata for Minecraft 26.3
- fix: respect server beacon ranges when detecting effect coverage

### Maintenance and Documentation

- docs: simplify player settings and version-specific issue reports
- build: add isolated client runs with optional mod menus
- build: bump the mod version to 2.1.0
- docs: refresh loader badges and compact the compatibility table
