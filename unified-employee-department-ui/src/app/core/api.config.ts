declare global {
  interface Window { __PEOPLEHUB_CONFIG__?: { employeeApi?: string; departmentApi?: string }; }
}
export const API = {
  employee: window.__PEOPLEHUB_CONFIG__?.employeeApi ?? 'http://localhost:8081',
  department: window.__PEOPLEHUB_CONFIG__?.departmentApi ?? 'http://localhost:8082'
} as const;
