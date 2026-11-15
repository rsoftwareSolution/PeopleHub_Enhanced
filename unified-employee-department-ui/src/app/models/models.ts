export interface Employee { id?: number; name: string; email: string; departmentId: number; }
export interface Department { id?: number; name: string; location: string; }
export interface TokenResponse { accessToken: string; tokenType: string; }
export type Role = 'ADMIN' | 'USER';
