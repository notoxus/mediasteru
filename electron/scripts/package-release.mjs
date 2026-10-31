#!/usr/bin/env node

import { createRequire } from 'node:module';
import { mkdir, readFile } from 'node:fs/promises';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const require = createRequire(import.meta.url);
const packager = require('@electron/packager');
const scriptDirectory = dirname(fileURLToPath(import.meta.url));
const electronDirectory = resolve(scriptDirectory, '..');
const repositoryRoot = resolve(electronDirectory, '..');
const args = process.argv.slice(2);
const option = (name) => args.find((arg) => arg.startsWith(`${name}=`))?.slice(name.length + 1);
const target = option('--target');
const version = option('--version');
const targetParts = target?.split('-') ?? [];
const platform = targetParts[0];
const arch = targetParts[1];

if (!['win32', 'darwin', 'linux'].includes(platform) || !['x64', 'arm64'].includes(arch)) {
  throw new Error('Pass a supported --target (win32-x64, darwin-x64, darwin-arm64, linux-x64, linux-arm64).');
}
if (!version || !/^\d+\.\d+\.\d+$/.test(version)) {
  throw new Error('Pass a release --version in X.Y.Z format.');
}

const manifest = JSON.parse(await readFile(join(repositoryRoot, 'tools-manifest.json'), 'utf8'));
const extraResource = Object.entries(manifest.tools).map(([name, tool]) => {
  const asset = tool.targets[target];
  if (!asset) throw new Error(`No ${name} release asset is defined for ${target}.`);
  return join(repositoryRoot, 'tools', asset.output);
});
const outputDirectory = join(repositoryRoot, 'dist', 'electron');
await mkdir(outputDirectory, { recursive: true });

const paths = await packager({
  dir: electronDirectory,
  out: outputDirectory,
  name: 'MediaSteru',
  executableName: 'MediaSteru',
  platform,
  arch,
  appVersion: version,
  asar: true,
  prune: true,
  overwrite: true,
  extraResource,
  ignore: [/^\/src\//, /^\/scripts\//, /^\/dist\/tests\//, /^\/tsconfig\.json$/],
});

for (const path of paths) console.log(`Packaged Electron app: ${path}`);
