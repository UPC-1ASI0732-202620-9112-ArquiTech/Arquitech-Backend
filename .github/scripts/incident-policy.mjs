export function decideIncidentAction({ monitoringFailed, hasOpenIncident }) {
  if (typeof monitoringFailed !== 'boolean' || typeof hasOpenIncident !== 'boolean') {
    throw new TypeError('monitoringFailed and hasOpenIncident must be boolean values');
  }
  if (monitoringFailed && !hasOpenIncident) return 'open';
  if (monitoringFailed && hasOpenIncident) return 'suppress';
  if (!monitoringFailed && hasOpenIncident) return 'recover';
  return 'noop';
}
