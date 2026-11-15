interface PeopleHubConfig {
  employeeApi?: string;
  departmentApi?: string;
}

declare global {
  interface Window {
    __PEOPLEHUB_CONFIG__?: PeopleHubConfig;
  }
}

function normalizeApiUrl(url: string): string {
  return url.trim().replace(/\/+$/, '');
}

function getRuntimeConfig(): PeopleHubConfig {
  if (typeof window === 'undefined') {
    return {};
  }

  return window.__PEOPLEHUB_CONFIG__ ?? {};
}

const runtimeConfig = getRuntimeConfig();

export const API = {
  employee: normalizeApiUrl(
    runtimeConfig.employeeApi ?? 'http://localhost:8081'
  ),

  department: normalizeApiUrl(
    runtimeConfig.departmentApi ?? 'http://localhost:8082'
  )
} as const;
