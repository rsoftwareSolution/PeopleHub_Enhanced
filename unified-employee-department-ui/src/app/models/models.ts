export interface Employee {
  id?: number;
  name: string;
  email: string;
  departmentId: number;
}

export interface Department {
  id?: number;
  name: string;
  location: string;
}

export interface TokenResponse {
  accessToken: string;
  tokenType: string;
}

export type Role = 'ADMIN' | 'USER';

export interface EmployeePage {
  content: Employee[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first?: boolean;
  last?: boolean;
}

export interface EmployeeSearchParams {
  q?: string;
  departmentId?: number;
  page: number;
  size: number;
  sort: string;
}

export type EmployeeSearchResponse =
  | Employee[]
  | EmployeePage;
