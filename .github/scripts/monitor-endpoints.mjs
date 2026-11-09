import { appendFileSync } from 'node:fs';
import { writeFile } from 'node:fs/promises';
import { performance } from 'node:perf_hooks';
import { pathToFileURL } from 'node:url';

const DEFAULT_TIMEOUT_MS = 10_000;
const DEFAULT_MAX_ATTEMPTS = 3;
const DEFAULT_RETRY_DELAY_MS = 5_000;

const sleep = (milliseconds) => new Promise((resolve) => setTimeout(resolve, milliseconds));

function positiveInteger(value, fallback, name) {
  if (value === undefined || value === null || value === '') return fallback;
  const parsed = Number.parseInt(value, 10);
  if (!Number.isInteger(parsed) || parsed < 1) {
    throw new Error(`${name} must be a positive integer`);
  }
  return parsed;
}

export function buildTargetsFromEnvironment(environment = process.env) {
  return [
    {
      name: 'Backend',
      url: environment.BACKEND_HEALTH_URL,
      expectedJsonStatus: 'UP',
    },
    { name: 'Frontend Web', url: environment.WEB_URL },
    { name: 'Landing Page', url: environment.LANDING_URL },
  ].filter((target) => typeof target.url === 'string' && target.url.trim() !== '');
}

function sanitizedUrl(value) {
  const url = new URL(value);
  url.username = '';
  url.password = '';
  url.hash = '';
  if (url.search) url.search = '?redacted=true';
  return url.toString();
}

async function requestWithTimeout(url, timeoutMs) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), timeoutMs);
  try {
    return await fetch(url, {
      method: 'GET',
      redirect: 'follow',
      signal: controller.signal,
      headers: {
        Accept: 'application/json,text/html;q=0.9,*/*;q=0.8',
        'User-Agent': 'ArquiTech-Availability-Monitor/1.0',
      },
    });
  } finally {
    clearTimeout(timeout);
  }
}

export async function checkTarget(target, options = {}) {
  const timeoutMs = positiveInteger(options.timeoutMs, DEFAULT_TIMEOUT_MS, 'timeoutMs');
  const maxAttempts = positiveInteger(options.maxAttempts, DEFAULT_MAX_ATTEMPTS, 'maxAttempts');
  const retryDelayMs = positiveInteger(options.retryDelayMs, DEFAULT_RETRY_DELAY_MS, 'retryDelayMs');
  const attempts = [];

  for (let attempt = 1; attempt <= maxAttempts; attempt += 1) {
    const startedAt = performance.now();
    try {
      const response = await requestWithTimeout(target.url, timeoutMs);
      const latencyMs = Math.round(performance.now() - startedAt);
      let contentMatches = true;
      let contentStatus = null;

      if (target.expectedJsonStatus) {
        const body = await response.json();
        contentStatus = body?.status ?? null;
        contentMatches = contentStatus === target.expectedJsonStatus;
      } else {
        await response.body?.cancel();
      }

      const httpMatches = response.status >= 200 && response.status < 400;
      const success = httpMatches && contentMatches;
      attempts.push({
        attempt,
        success,
        httpStatus: response.status,
        latencyMs,
        contentStatus,
        error: success ? null : contentMatches ? `unexpected HTTP ${response.status}` : `unexpected status ${contentStatus}`,
      });

      if (success) {
        return { name: target.name, url: sanitizedUrl(target.url), success: true, attempts };
      }
    } catch (error) {
      const latencyMs = Math.round(performance.now() - startedAt);
      const timedOut = error?.name === 'AbortError';
      attempts.push({
        attempt,
        success: false,
        httpStatus: null,
        latencyMs,
        contentStatus: null,
        error: timedOut ? `timeout after ${timeoutMs} ms` : String(error?.message ?? error),
      });
    }

    if (attempt < maxAttempts) await sleep(retryDelayMs);
  }

  return { name: target.name, url: sanitizedUrl(target.url), success: false, attempts };
}

export async function runMonitoring(targets, options = {}) {
  if (!Array.isArray(targets) || targets.length === 0) {
    throw new Error('At least one monitoring target must be configured');
  }
  const results = [];
  for (const target of targets) {
    results.push(await checkTarget(target, options));
  }
  return {
    checkedAt: new Date().toISOString(),
    success: results.every((result) => result.success),
    configuration: {
      timeoutMs: positiveInteger(options.timeoutMs, DEFAULT_TIMEOUT_MS, 'timeoutMs'),
      maxAttempts: positiveInteger(options.maxAttempts, DEFAULT_MAX_ATTEMPTS, 'maxAttempts'),
      retryDelayMs: positiveInteger(options.retryDelayMs, DEFAULT_RETRY_DELAY_MS, 'retryDelayMs'),
    },
    targets: results,
  };
}

function markdownSummary(result) {
  const lines = [
    '## ArquiTech availability monitoring',
    '',
    `Checked at: \`${result.checkedAt}\``,
    '',
    '| Target | Result | HTTP | Latency | Attempts |',
    '| ------ | ------ | ---- | ------- | -------- |',
  ];
  for (const target of result.targets) {
    const last = target.attempts.at(-1);
    lines.push(
      `| ${target.name} | ${target.success ? 'AVAILABLE' : 'UNAVAILABLE'} | ${last.httpStatus ?? 'N/A'} | ${last.latencyMs} ms | ${target.attempts.length} |`,
    );
  }
  return `${lines.join('\n')}\n`;
}

async function main() {
  const options = {
    timeoutMs: process.env.REQUEST_TIMEOUT_MS,
    maxAttempts: process.env.MAX_ATTEMPTS,
    retryDelayMs: process.env.RETRY_DELAY_MS,
  };
  const targets = buildTargetsFromEnvironment();
  const result = await runMonitoring(targets, options);
  const resultFile = process.env.MONITOR_RESULT_FILE || 'monitor-results.json';
  await writeFile(resultFile, `${JSON.stringify(result, null, 2)}\n`, 'utf8');

  for (const target of result.targets) {
    const last = target.attempts.at(-1);
    console.log(
      `[${target.success ? 'PASS' : 'FAIL'}] ${target.name}: HTTP ${last.httpStatus ?? 'N/A'}, ${last.latencyMs} ms, ${target.attempts.length} attempt(s)${last.error ? `, ${last.error}` : ''}`,
    );
  }

  if (process.env.GITHUB_STEP_SUMMARY) {
    appendFileSync(process.env.GITHUB_STEP_SUMMARY, markdownSummary(result), 'utf8');
  }
  if (process.env.GITHUB_OUTPUT) {
    appendFileSync(process.env.GITHUB_OUTPUT, `failed=${String(!result.success)}\nresult_file=${resultFile}\n`, 'utf8');
  }
  if (!result.success) process.exitCode = 1;
}

const invokedDirectly = process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href;
if (invokedDirectly) {
  main().catch((error) => {
    console.error(`Monitoring execution failed: ${error.message}`);
    process.exitCode = 1;
  });
}
