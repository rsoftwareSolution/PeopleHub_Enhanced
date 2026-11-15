import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API } from './api.config';
import { Department, Employee } from '../models/models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient) {}

  searchEmployees(params: {q?: string; departmentId?: number; page?: number; size?: number; sort?: string}): Observable<any> {
    return this.http.get<any>(`${API.employee}/api/employees/search`, { params: params as any });
  }
  employeesByDepartment(id: number): Observable<Employee[]> { return this.http.get<Employee[]>(`${API.employee}/api/employees/department/${id}/employees`); }
  departmentEmployeeCount(id: number): Observable<{departmentId:number; employeeCount:number}> { return this.http.get<any>(`${API.employee}/api/employees/department/${id}/count`); }
  departmentCounts(): Observable<Array<{departmentId:number; employeeCount:number}>> { return this.http.get<any>(`${API.employee}/api/employees/department-counts`); }
  departmentDetails(id: number): Observable<any> { return this.http.get<any>(`${API.employee}/api/employees/department/${id}/details`); }
  departmentsWithCounts(): Observable<any[]> { return this.http.get<any[]>(`${API.department}/api/departments/with-employee-counts`); }

  employees(): Observable<Employee[]> { return this.http.get<Employee[]>(`${API.employee}/api/employees`); }
  employee(id: number): Observable<Employee> { return this.http.get<Employee>(`${API.employee}/api/employees/${id}`); }
  createEmployee(value: Employee): Observable<Employee> { return this.http.post<Employee>(`${API.employee}/api/employees`, value); }
  updateEmployee(id: number, value: Employee): Observable<Employee> { return this.http.put<Employee>(`${API.employee}/api/employees/${id}`, value); }
  deleteEmployee(id: number): Observable<void> { return this.http.delete<void>(`${API.employee}/api/employees/${id}`); }

  searchDepartments(params: {q?: string; page?: number; size?: number; sort?: string}): Observable<any> {
    return this.http.get<any>(`${API.department}/api/departments/search`, { params: params as any });
  }
  departments(): Observable<Department[]> { return this.http.get<Department[]>(`${API.department}/api/departments`); }
  department(id: number): Observable<Department> { return this.http.get<Department>(`${API.department}/api/departments/${id}`); }
  departmentThroughEmployee(id: number): Observable<Department> { return this.http.get<Department>(`${API.employee}/api/employees/department/${id}`); }
  createDepartment(value: Department): Observable<Department> { return this.http.post<Department>(`${API.department}/api/departments`, value); }
  updateDepartment(id: number, value: Department): Observable<Department> { return this.http.put<Department>(`${API.department}/api/departments/${id}`, value); }
  deleteDepartment(id: number): Observable<void> { return this.http.delete<void>(`${API.department}/api/departments/${id}`); }
}
