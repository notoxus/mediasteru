#!/usr/bin/env node

import { readFile } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const projectRoot = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const read = (path) => readFile(resolve(projectRoot, path), 'utf8').catch(() => '');
const expectedArgument = process.argv.find((argument) => argument.startsWith('--expected='));
const expectedVersion = expectedArgument?.slice('--expected='.length).replace(/^v/, '');
const failures = [];

function expect(label, actual, expected) {
  if (actual !== expected) failures.push(`${label}: expected ${expected}, found ${actual ?? 'missing'}`);
}

function match(text, pattern, label) {
  const value = text.match(pattern)?.[1];
  if (!value) failures.push(`${label}: value not found`);
  return value;
}

// Chỉ đọc các file của Spring Boot Engine
const [gradleKts, gradleGroovy, coreProperties, rootProperties, readme] = await Promise.all([
  read('build.gradle.kts'),
  read('build.gradle'),
  read('core/src/main/resources/version.properties'),
  read('src/main/resources/version.properties'),
  read('README.md')
]);
const properties = coreProperties || rootProperties;

let buildVersion = null;
if (gradleKts) {
    buildVersion = match(gradleKts, /^version\s*=\s*"([^"]+)"/m, 'build.gradle.kts version');
} else if (gradleGroovy) {
    buildVersion = match(gradleGroovy, /^version\s*=\s*'([^']+)'/m, 'build.gradle version');
} else {
    failures.push("No build.gradle.kts or build.gradle found");
}

const propertiesVersion = match(properties, /^version=(.+)$/m, 'version.properties');
const readmeBadge = match(readme, /<!-- VERSION_START -->([^<]+)<!-- VERSION_END -->/, 'README version badge');

for (const [label, version] of [
  ['version.properties', propertiesVersion],
  ['README.md', readmeBadge]
]) {
  expect(label, version, buildVersion);
}

if (expectedVersion) expect('Release tag', expectedVersion, buildVersion);

if (failures.length > 0) {
  console.error('Project version consistency check failed:');
  for (const failure of failures) console.error(`  - ${failure}`);
  process.exit(1);
}

console.log(`Version ${buildVersion} is synchronized across Gradle, Properties, and README.`);