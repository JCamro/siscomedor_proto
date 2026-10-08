# Bundled Inter

- Upstream: https://github.com/rsms/inter
- Download: https://raw.githubusercontent.com/rsms/inter/master/docs/font-files/InterVariable.ttf
- Upstream Git blob: `4ab79e0102bbe0ffa1ed879b13e52ac8c6487833`.
- Local resource: `res/font/inter.ttf` (879,708 bytes, unchanged upstream binary).
- SHA-256: `4989b125924991b90d05b2d16e0e388c48f7d5bb8b30539bbf9c755278d0ccaf`.
- License: SIL Open Font License 1.1; full copyright and license bundled in `assets/fonts/OFL.txt`.
- Source license: https://raw.githubusercontent.com/rsms/inter/master/LICENSE.txt

The APK loads this local resource, not a remote font provider. Binary table inspection confirms the `wght` axis range 100–900/default 400, `opsz` 14–32/default 14 and OS/2 weight 400. Android 26+ uses explicit variable-weight settings (400/500/600/700/800) and optical size 14; Android 24–25 uses the regular default instance with platform weight synthesis. The binary and license remain unchanged. Device typography validation, especially the older-API synthesis fallback, is pending.
