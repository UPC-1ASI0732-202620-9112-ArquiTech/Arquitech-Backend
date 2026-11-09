import assert from 'node:assert/strict';
import http from 'node:http';
import test from 'node:test';

import { decideIncidentAction } from './incident-policy.mjs';
import { checkTarget } from './monitor-endpoints.mjs';

async function startServer(handler) {
  const server = http.createServer(handler);
  await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
  const address = server.address();
  return { server, url: `http://127.0.0.1:${address.port}` };
}

async function stopServer(server) {
  server.closeAllConnections?.();
  await new Promise((resolve) => server.close(resolve));
}

test('accepts a healthy Actuator response', async (t) => {
  const { server, url } = await startServer((_request, response) => {
    response.writeHead(200, { 'Content-Type': 'application/json' });
    response.end('{"status":"UP"}');
  });
  t.after(() => stopServer(server));

  const result = await checkTarget(
    { name: 'Backend', url, expectedJsonStatus: 'UP' },
    { timeoutMs: 200, maxAttempts: 1, retryDelayMs: 1 },
  );

  assert.equal(result.success, true);
  assert.equal(result.attempts[0].httpStatus, 200);
  assert.equal(result.attempts[0].contentStatus, 'UP');
});

test('records an HTTP error after the configured attempts', async (t) => {
  const { server, url } = await startServer((_request, response) => {
    response.writeHead(503);
    response.end('unavailable');
  });
  t.after(() => stopServer(server));

  const result = await checkTarget(
    { name: 'Web', url },
    { timeoutMs: 200, maxAttempts: 2, retryDelayMs: 1 },
  );

  assert.equal(result.success, false);
  assert.equal(result.attempts.length, 2);
  assert.equal(result.attempts[1].httpStatus, 503);
});

test('records a timeout without contacting production', async (t) => {
  const { server, url } = await startServer((_request, response) => {
    setTimeout(() => {
      response.writeHead(200);
      response.end('late');
    }, 100);
  });
  t.after(() => stopServer(server));

  const result = await checkTarget(
    { name: 'Landing', url },
    { timeoutMs: 20, maxAttempts: 1, retryDelayMs: 1 },
  );

  assert.equal(result.success, false);
  assert.match(result.attempts[0].error, /timeout/);
});

test('recovers when a retry receives a valid response', async (t) => {
  let requests = 0;
  const { server, url } = await startServer((_request, response) => {
    requests += 1;
    response.writeHead(requests === 1 ? 503 : 200);
    response.end(requests === 1 ? 'temporary failure' : 'recovered');
  });
  t.after(() => stopServer(server));

  const result = await checkTarget(
    { name: 'Web', url },
    { timeoutMs: 200, maxAttempts: 3, retryDelayMs: 1 },
  );

  assert.equal(result.success, true);
  assert.equal(result.attempts.length, 2);
  assert.equal(result.attempts[0].httpStatus, 503);
  assert.equal(result.attempts[1].httpStatus, 200);
});

test('incident policy opens, suppresses duplicates, recovers and remains idle', () => {
  assert.equal(decideIncidentAction({ monitoringFailed: true, hasOpenIncident: false }), 'open');
  assert.equal(decideIncidentAction({ monitoringFailed: true, hasOpenIncident: true }), 'suppress');
  assert.equal(decideIncidentAction({ monitoringFailed: false, hasOpenIncident: true }), 'recover');
  assert.equal(decideIncidentAction({ monitoringFailed: false, hasOpenIncident: false }), 'noop');
});
