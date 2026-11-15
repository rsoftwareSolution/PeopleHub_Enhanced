import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpParams
} from '@angular/common/http';
import { Observable } from 'rxjs';

import { API } from './api.config';

import {
  Department,
  Employee,
  EmployeeSearchParams,
  EmployeeSearchResponse
} from '../models/models';

/**
 * Represents the employee count returned for a department.
 */
export interface DepartmentEmployeeCount {
  departmentId: number;
  employeeCount: number;
}

/**
 * Parameters supported by the department search endpoint.
 */
export interface DepartmentSearchParams {
  q?: string;
  page?: number;
  size?: number;
  sort?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  constructor(
    private readonly http: HttpClient
  ) {}

  // -----------------------------------------
  // Employee search and filtering
  // -----------------------------------------

  searchEmployees(
    params: EmployeeSearchParams
  ): Observable<EmployeeSearchResponse> {

    let queryParams = new HttpParams()
      .set('page', params.page)
      .set('size', params.size)
      .set('sort', params.sort);

    const searchText = params.q?.trim();

    if (searchText) {
      queryParams = queryParams.set('q', searchText);
    }

    if (params.departmentId !== undefined) {
      queryParams = queryParams.set(
        'departmentId',
        params.departmentId
      );
    }

    return this.http.get<EmployeeSearchResponse>(
      `${API.employee}/api/employees/search`,
      {
        params: queryParams
      }
    );
  }

  // -----------------------------------------
  // Employee department integration
  // -----------------------------------------

  employeesByDepartment(
    id: number
  ): Observable<Employee[]> {

    return this.http.get<Employee[]>(
      `${API.employee}/api/employees/department/${id}/employees`
    );
  }

  departmentEmployeeCount(
    id: number
  ): Observable<DepartmentEmployeeCount> {

    return this.http.get<DepartmentEmployeeCount>(
      `${API.employee}/api/employees/department/${id}/count`
    );
  }

  departmentCounts(): Observable<DepartmentEmployeeCount[]> {

    return this.http.get<DepartmentEmployeeCount[]>(
      `${API.employee}/api/employees/department-counts`
    );
  }

  /**
   * The exact response structure depends on the backend DTO.
   * Keep it unknown until that DTO is verified.
   */
  departmentDetails(id: number): Observable<unknown> {

    return this.http.get<unknown>(
      `${API.employee}/api/employees/department/${id}/details`
    );
  }

  /**
   * The backend response structure has not yet been verified.
   */
  departmentsWithCounts(): Observable<unknown[]> {

    return this.http.get<unknown[]>(
      `${API.department}/api/departments/with-employee-counts`
    );
  }

  // -----------------------------------------
  // Employee CRUD
  // -----------------------------------------

  employees(): Observable<Employee[]> {

    return this.http.get<Employee[]>(
      `${API.employee}/api/employees`
    );
  }

  employee(id: number): Observable<Employee> {

    return this.http.get<Employee>(
      `${API.employee}/api/employees/${id}`
    );
  }

  createEmployee(
    value: Employee
  ): Observable<Employee> {

    return this.http.post<Employee>(
      `${API.employee}/api/employees`,
      value
    );
  }

  updateEmployee(
    id: number,
    value: Employee
  ): Observable<Employee> {

    return this.http.put<Employee>(
      `${API.employee}/api/employees/${id}`,
      value
    );
  }

  deleteEmployee(
    id: number
  ): Observable<void> {

    return this.http.delete<void>(
      `${API.employee}/api/employees/${id}`
    );
  }

  // -----------------------------------------
  // Department search
  // -----------------------------------------

  searchDepartments(
    params: DepartmentSearchParams
  ): Observable<unknown> {

    let queryParams = new HttpParams();

    const searchText = params.q?.trim();

    if (searchText) {
      queryParams = queryParams.set('q', searchText);
    }

    if (params.page !== undefined) {
      queryParams = queryParams.set(
        'page',
        params.page
      );
    }

    if (params.size !== undefined) {
      queryParams = queryParams.set(
        'size',
        params.size
      );
    }

    if (params.sort) {
      queryParams = queryParams.set(
        'sort',
        params.sort
      );
    }

    return this.http.get<unknown>(
      `${API.department}/api/departments/search`,
      {
        params: queryParams
      }
    );
  }

  // -----------------------------------------
  // Department CRUD
  // -----------------------------------------

  departments(): Observable<Department[]> {

    return this.http.get<Department[]>(
      `${API.department}/api/departments`
    );
  }

  department(id: number): Observable<Department> {

    return this.http.get<Department>(
      `${API.department}/api/departments/${id}`
    );
  }

  createDepartment(
    value: Department
  ): Observable<Department> {

    return this.http.post<Department>(
      `${API.department}/api/departments`,
      value
    );
  }

  updateDepartment(
    id: number,
    value: Department
  ): Observable<Department> {

    return this.http.put<Department>(
      `${API.department}/api/departments/${id}`,
      value
    );
  }

  deleteDepartment(
    id: number
  ): Observable<void> {

    return this.http.delete<void>(
      `${API.department}/api/departments/${id}`
    );
  }

  // -----------------------------------------
  // Employee-to-Department service integration
  // -----------------------------------------

  departmentThroughEmployee(
    id: number
  ): Observable<Department> {

    return this.http.get<Department>(
      `${API.employee}/api/employees/department/${id}`
    );
  }
}
